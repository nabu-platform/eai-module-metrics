package nabu.misc.metrics;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.eai.module.cluster.ClusterArtifact;
import be.nabu.eai.module.metrics.MetricsREST;
import be.nabu.eai.module.metrics.beans.ArtifactMetrics;
import be.nabu.eai.module.metrics.beans.MetricOverview;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.ListableSinkProviderArtifact;
import be.nabu.eai.server.ServerConnection;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.http.api.HTTPResponse;
import be.nabu.libs.http.api.client.HTTPClient;
import be.nabu.libs.http.core.DefaultHTTPRequest;
import be.nabu.libs.metrics.core.SinkStatisticsImpl;
import be.nabu.libs.metrics.core.api.HistorySink;
import be.nabu.libs.metrics.core.api.Sink;
import be.nabu.libs.metrics.core.api.SinkProvider;
import be.nabu.libs.metrics.core.api.SinkSnapshot;
import be.nabu.libs.metrics.core.api.SinkValue;
import be.nabu.libs.metrics.core.api.StatisticsContainer;
import be.nabu.libs.metrics.core.api.TaggableSink;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.binding.api.Window;
import be.nabu.libs.types.binding.xml.XMLBinding;
import be.nabu.libs.types.java.BeanResolver;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.mime.api.ContentPart;
import be.nabu.utils.mime.impl.FormatException;
import be.nabu.utils.mime.impl.MimeHeader;
import be.nabu.utils.mime.impl.PlainMimeEmptyPart;

@WebService
public class Services {
	
	public static final int DEFAULT_AMOUNT = 100;
	
	private ExecutionContext context;
	
	@WebResult(name = "metrics")
	public MetricOverview poll(@WebParam(name = "host") String host, @WebParam(name = "since") Date since, @WebParam(name = "filter") String filter) throws IOException, FormatException, ParseException {
		ServerConnection connection = null;
		if (host != null) {
			for (ClusterArtifact cluster : EAIResourceRepository.getInstance().getArtifacts(ClusterArtifact.class)) {
				if (cluster.getConfig().getHosts() != null && cluster.getConfig().getHosts().contains(host)) {
					connection = cluster.getConnection(host);
					if (connection != null) {
						break;
					}
				}
			}
			if (connection == null) {
				throw new IllegalArgumentException("Can not find cluster object for host: " + host);
			}
		}
		if (connection != null) {
			HTTPClient client = connection.getClient();
			HTTPResponse response = client.execute(new DefaultHTTPRequest("GET", (since == null ? "/metrics" : "/metrics/" + since.getTime()) + (filter == null ? "" : "?filter=" + filter), new PlainMimeEmptyPart(null,
				new MimeHeader("Content-Length", "0"),
				new MimeHeader("Accept", "application/xml"),
				new MimeHeader("Accept-Encoding", "gzip"),
				new MimeHeader("Host", host)
			)), connection.getPrincipal(), false, false);
			if (response.getCode() != 200) {
				throw new IllegalStateException("The remote server returned " + response.getCode() + ": " + response.getMessage());
			}
			XMLBinding binding = new XMLBinding((ComplexType) BeanResolver.getInstance().resolve(MetricOverview.class), Charset.forName("UTF-8"));
			return TypeUtils.getAsBean(binding.unmarshal(IOUtils.toInputStream(((ContentPart) response.getContent()).getReadable()), new Window[0]), MetricOverview.class);
		}
		// we are on the current repository
		else {
			return MetricsREST.build(EAIResourceRepository.getInstance(), since, filter);
		}
	}
	
	@WebResult(name = "lastPushed")
	public Date lastPushed(@NotNull @WebParam(name = "metricsDatabaseId") String metricsDatabaseId) {
		ListableSinkProviderArtifact database = context.getServiceContext().getResolver(ListableSinkProviderArtifact.class).resolve(metricsDatabaseId);
		if (database == null) {
			throw new IllegalArgumentException("The metrics database does not exist: " + metricsDatabaseId);
		}
		return database.getLastPushed();
	}
	
	public void persist(@NotNull @WebParam(name = "metricsDatabaseId") String metricsDatabaseId, @NotNull @WebParam(name = "metrics") MetricOverview overview) {
		ListableSinkProviderArtifact provider = context.getServiceContext().getResolver(ListableSinkProviderArtifact.class).resolve(metricsDatabaseId);
		if (provider == null) {
			throw new IllegalArgumentException("The metrics database does not exist: " + metricsDatabaseId);
		}
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
				Sink sink = provider.getSink(metrics.getId(), key);
				if (sink instanceof TaggableSink) {
					if (metrics.getType() != null && !metrics.getType().equals(((TaggableSink) sink).getTag("type"))) {
						((TaggableSink) sink).setTag("type", metrics.getType());
					}
					for (String tag : metrics.getTags().keySet()) {
						((TaggableSink) sink).setTag(tag, metrics.getTags().get(tag));
					}
				}
				for (SinkValue value : sinkSnapshot.getValues()) {
					sink.push(value.getTimestamp(), value.getValue());
				}
			}
		}
		// update the last pushed
		provider.setLastPushed(new Date(overview.getTimestamp()));
	}
	
	@WebResult(name = "metrics")
	public MetricOverview select(@NotNull @WebParam(name = "metricsDatabaseId") String metricsDatabaseId, @WebParam(name = "since") Date since, @WebParam(name = "until") Date until) {
		ListableSinkProviderArtifact provider = context.getServiceContext().getResolver(ListableSinkProviderArtifact.class).resolve(metricsDatabaseId);
		if (provider == null) {
			throw new IllegalArgumentException("The metrics database does not exist: " + metricsDatabaseId);
		}
		MetricOverview overview = new MetricOverview();
		if (until != null) {
			overview.setTimestamp(until.getTime());
		}
		Map<String, List<String>> sinks = provider.getSinks();
		for (String id : sinks.keySet()) {
			boolean hasData = false;
			ArtifactMetrics artifactMetrics = new ArtifactMetrics();
			artifactMetrics.setId(id);
			for (String category : sinks.get(id)) {
				HistorySink sink = (HistorySink) provider.getSink(id, category);
				artifactMetrics.getSnapshots().put(category, since == null 
					? sink.getSnapshotUntil(DEFAULT_AMOUNT, overview.getTimestamp())
					: sink.getSnapshotBetween(since.getTime() + 1, overview.getTimestamp()));
				// get the current statistics for the sink
				if (sink instanceof StatisticsContainer) {
					artifactMetrics.getStatistics().put(category, new SinkStatisticsImpl(((StatisticsContainer) sink).getStatistics()));
				}
				if (sink instanceof TaggableSink && ((TaggableSink) sink).getTags() != null) {
					// best effort filling in of the artifact type
					if (artifactMetrics.getType() == null) {
						artifactMetrics.setType(((TaggableSink) sink).getTag("type"));
					}
					for (String tag : ((TaggableSink) sink).getTags()) {
						artifactMetrics.getTags().put(tag, ((TaggableSink) sink).getTag(tag));
					}
				}
				artifactMetrics.setSince(since);
				artifactMetrics.setUntil(until);
				hasData |= artifactMetrics.getSnapshots().get(category).getValues().size() > 0;
			}
			// if we still don't know the type, try to resolve it (again best effort)
			if (artifactMetrics.getType() == null) {
				Artifact artifact = provider.getRepository().resolve(id);
				if (artifact != null) {
					artifactMetrics.setType(MetricsREST.getType(artifact));
				}
			}
			if (hasData) {
				overview.getMetrics().add(artifactMetrics);
			}
		}
		return overview;
	}
	
	@WebResult(name = "metricDatabaseIds")
	public List<String> metricDatabases() {
		List<String> ids = new ArrayList<String>();
		for (ListableSinkProviderArtifact artifact : EAIResourceRepository.getInstance().getArtifacts(ListableSinkProviderArtifact.class)) {
			ids.add(artifact.getId());
		}
		return ids;
	}
	
	@WebResult(name = "snapshot")
	public List<SinkValue> snapshotBetween(@WebParam(name = "metricsDatabaseId") String metricsDatabaseId, @NotNull @WebParam(name = "sinkId") String id, @NotNull @WebParam(name = "category") String category, @NotNull @WebParam(name = "since") Date since, @WebParam(name = "until") Date until) {
		SinkProvider provider = metricsDatabaseId == null ? EAIResourceRepository.getInstance() : context.getServiceContext().getResolver(ListableSinkProviderArtifact.class).resolve(metricsDatabaseId);
		if (provider == null) {
			throw new IllegalArgumentException("The metrics database does not exist: " + metricsDatabaseId);
		}
		Sink sink = provider.getSink(id, category);
		if (sink instanceof HistorySink) {
			return ((HistorySink) sink).getSnapshotBetween(since.getTime(), until == null ? new Date().getTime() : until.getTime()).getValues();
		}
		return null;
	}
	
	@WebResult(name = "snapshot")
	public List<SinkValue> snapshotUntil(@WebParam(name = "metricsDatabaseId") String metricsDatabaseId, @NotNull @WebParam(name = "sinkId") String id, @NotNull @WebParam(name = "category") String category, @WebParam(name = "amount") Integer amount, @WebParam(name = "until") Date until) {
		SinkProvider provider = metricsDatabaseId == null ? EAIResourceRepository.getInstance() : context.getServiceContext().getResolver(ListableSinkProviderArtifact.class).resolve(metricsDatabaseId);
		if (provider == null) {
			throw new IllegalArgumentException("The metrics database does not exist: " + metricsDatabaseId);
		}
		Sink sink = provider.getSink(id, category);
		if (sink instanceof HistorySink) {
			return ((HistorySink) sink).getSnapshotUntil(amount == null ? DEFAULT_AMOUNT : amount, until == null ? new Date().getTime() : until.getTime()).getValues();
		}
		return null;
	}
	
}
