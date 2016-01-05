package be.nabu.eai.module.metrics;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import be.nabu.eai.module.metrics.beans.ArtifactMetrics;
import be.nabu.eai.module.metrics.beans.MetricOverview;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.metrics.core.MetricInstanceImpl;
import be.nabu.libs.metrics.core.SinkValueImpl;
import be.nabu.libs.metrics.core.api.CurrentValueSink;
import be.nabu.libs.metrics.core.api.HistorySink;
import be.nabu.libs.metrics.core.api.Sink;

public class MetricsREST {
	
	@Context
	private EAIResourceRepository repository;
	
	@GET
	@Path("/metrics/{since}")
	public MetricOverview getOverview(@PathParam("since") Date since) {
		MetricOverview overview = new MetricOverview();
		for (String id : repository.getMetricInstances()) {
			ArtifactMetrics artifactMetrics = null;
			for (ArtifactMetrics metrics : overview.getMetrics()) {
				if (id.equals(metrics.getId())) {
					artifactMetrics = metrics;
					break;
				}
			}
			if (artifactMetrics == null) {
				artifactMetrics = new ArtifactMetrics();
				artifactMetrics.setId(id);
				artifactMetrics.setLevel(repository.getMetricsLevel(id));
				Artifact artifact = repository.resolve(id);
				if (artifact != null) {
					artifactMetrics.setArtifactType(artifact.getClass().getName());
				}
				overview.getMetrics().add(artifactMetrics);
			}
			MetricInstanceImpl metricInstance = (MetricInstanceImpl) repository.getMetricInstance(id).getParent();
			for (String gaugeId : metricInstance.getGaugeIds()) {
				artifactMetrics.getGauges().put(gaugeId, new SinkValueImpl(new Date().getTime(), metricInstance.getGauge(gaugeId).getValue()));
			}
			for (String sinkId : metricInstance.getSinkIds()) {
				Sink sink = metricInstance.getSink(sinkId);
				if (sink instanceof HistorySink) {
					artifactMetrics.getSnapshots().put(sinkId, since == null 
						? ((HistorySink) sink).getSnapshot(50)
						: ((HistorySink) sink).getSnapshotAfter(since.getTime()));
				}
				else if (sink instanceof CurrentValueSink) {
					artifactMetrics.getGauges().put(sinkId, ((CurrentValueSink) sink).getCurrent());	
				}
			}
		}
		return overview;
	}
	
	@GET
	@Path("/metrics")
	public MetricOverview getOverview() {
		return getOverview(null);
	}
	
}
