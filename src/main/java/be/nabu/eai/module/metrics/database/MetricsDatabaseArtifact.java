package be.nabu.eai.module.metrics.database;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

import be.nabu.eai.module.metrics.beans.ArtifactMetrics;
import be.nabu.eai.module.metrics.beans.MetricOverview;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.metrics.core.SinkStatisticsImpl;
import be.nabu.libs.metrics.core.api.SinkSnapshot;
import be.nabu.libs.metrics.core.api.SinkValue;
import be.nabu.libs.metrics.database.PartitionedSink;
import be.nabu.libs.metrics.database.PartitionedSinkProvider;
import be.nabu.libs.metrics.database.api.PartitionConfigurationProvider;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;

public class MetricsDatabaseArtifact extends JAXBArtifact<MetricsDatabaseConfiguration> {

	/**
	 * This is the last pushed for the entire database, you can not (currently) request a last pushed on a sink-by-sink basis
	 */
	private Date lastPushed;
	private PartitionedSinkProvider provider;
	
	public MetricsDatabaseArtifact(String id, ResourceContainer<?> directory, Repository repository) {
		super(id, directory, repository, "metrics-database.xml", MetricsDatabaseConfiguration.class);
	}
	
	public Map<String, List<String>> getSinks() {
		return getProvider().getSinks();
	}
	
	public MetricOverview select(Date since, Date until) {
		MetricOverview overview = new MetricOverview();
		if (until != null) {
			overview.setTimestamp(until.getTime());
		}
		PartitionedSinkProvider provider = getProvider();
		Map<String, List<String>> sinks = provider.getSinks();
		for (String id : sinks.keySet()) {
			ArtifactMetrics artifactMetrics = new ArtifactMetrics();
			artifactMetrics.setId(id);
			overview.getMetrics().add(artifactMetrics);
			for (String category : sinks.get(id)) {
				PartitionedSink sink = provider.getSink(id, category);
				artifactMetrics.getSnapshots().put(category, since == null 
					? sink.getSnapshotUntil(50, overview.getTimestamp())
					: sink.getSnapshotBetween(since.getTime() + 1, overview.getTimestamp()));
				// get the current statistics for the sink
				artifactMetrics.getStatistics().put(category, new SinkStatisticsImpl(sink.getStatistics()));
				// best effort filling in of the artifact type
				if (artifactMetrics.getArtifactType() == null) {
					artifactMetrics.setArtifactType(sink.getTag("artifactType"));
				}
			}
			// if we still don't know the type, try to resolve it (again best effort)
			if (artifactMetrics.getArtifactType() == null) {
				Artifact artifact = getRepository().resolve(id);
				if (artifact != null) {
					artifactMetrics.setArtifactType(artifact.getClass().getName());
				}
			}
		}
		return overview;
	}
	
	public void persist(MetricOverview overview) {
		PartitionedSinkProvider provider = getProvider();
		for (ArtifactMetrics metrics : overview.getMetrics()) {
			// push the gauges
			Map<String, SinkValue> gauges = metrics.getGauges();
			for (String key : gauges.keySet()) {
				SinkValue sinkValue = gauges.get(key);
				provider.getSink(metrics.getId(), key).push(sinkValue.getTimestamp(), sinkValue.getValue());
			}
			// push the snapshots
			Map<String, SinkSnapshot> snapshots = metrics.getSnapshots();
			for (String key : snapshots.keySet()) {
				SinkSnapshot sinkSnapshot = snapshots.get(key);
				PartitionedSink sink = provider.getSink(metrics.getId(), key);
				if (metrics.getArtifactType() != null && !metrics.getArtifactType().equals(sink.getTag("artifactType"))) {
					sink.setTag("artifactType", metrics.getArtifactType());
				}
				for (SinkValue value : sinkSnapshot.getValues()) {
					sink.push(value.getTimestamp(), value.getValue());
				}
			}
		}
		// update the last pushed
		lastPushed = new Date(overview.getTimestamp());
	}
	
	public Date getLastPushed() {
		if (lastPushed == null) {
			// calculate the last pushed based on the most recent data available across all sinks
			PartitionedSinkProvider provider = getProvider();
			Map<String, List<String>> sinks = provider.getSinks();
			Date lastPushed = null;
			long time = new Date().getTime();
			for (String id : sinks.keySet()) {
				for (String category : sinks.get(id)) {
					PartitionedSink sink = provider.getSink(id, category);
					SinkSnapshot snapshot = sink.getSnapshotUntil(1, time);
					if (!snapshot.getValues().isEmpty()) {
						if (lastPushed == null || snapshot.getValues().get(0).getTimestamp() > lastPushed.getTime()) {
							lastPushed = new Date(snapshot.getValues().get(0).getTimestamp());
						}
					}
				}
			}
			this.lastPushed = lastPushed;
		}
		return lastPushed;
	}

	public PartitionedSinkProvider getProvider() {
		if (provider == null) {
			try {
				synchronized(this) {
					if (provider == null) {
						URI storageURI = getConfiguration().getStorage();
						if (storageURI == null) {
							throw new IllegalArgumentException("Expecting a storage URI for a metrics database");
						}
						Resource storage = ResourceUtils.mkdir(storageURI, null);
						URI temporaryURI = getConfiguration().getTemporary();
						if (temporaryURI == null) {
							temporaryURI = new URI("tmp:/metrics");
						}
						Resource temporary = ResourceUtils.mkdir(temporaryURI, null);
						provider = new PartitionedSinkProvider(new PartitionConfigurationProvider() {
							@Override
							public long getPartitionInterval(String id, String category) {
								try {
									Long partitionInterval = getConfiguration().getPartitionInterval();
									if (partitionInterval == null) {
										// 1 hour
										partitionInterval = 1000l*60*60;
									}
									return partitionInterval;
								}
								catch (Exception e) {
									throw new RuntimeException(e);
								}
							}
							@Override
							public long getPartitionSize(String id, String category) {
								try {
									Long partitionSize = getConfiguration().getPartitionSize();
									return partitionSize == null ? 0 : partitionSize;
								}
								catch (Exception e) {
									throw new RuntimeException(e);
								}
							}
						}, (ResourceContainer<?>) storage, (ResourceContainer<?>) temporary);
					}
				}
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return provider;
	}
	
}
