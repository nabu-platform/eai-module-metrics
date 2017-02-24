package be.nabu.eai.module.metrics;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import nabu.misc.metrics.Services;
import be.nabu.eai.module.metrics.beans.ArtifactMetrics;
import be.nabu.eai.module.metrics.beans.MetricOverview;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.metrics.core.MetricInstanceImpl;
import be.nabu.libs.metrics.core.SinkStatisticsImpl;
import be.nabu.libs.metrics.core.SinkValueImpl;
import be.nabu.libs.metrics.core.api.CurrentValueSink;
import be.nabu.libs.metrics.core.api.HistorySink;
import be.nabu.libs.metrics.core.api.Sink;
import be.nabu.libs.metrics.core.api.StatisticsContainer;
import be.nabu.libs.services.api.DefinedService;

public class MetricsREST {
	
	@Context
	private EAIResourceRepository repository;
	
	@GET
	@Path("/metrics/{since}")
	public MetricOverview getOverview(@PathParam("since") Long since, @QueryParam("filter") String filter) {
		return build(repository, since == null ? null : new Date(since), filter);
	}
	
	@GET
	@Path("/metrics")
	public MetricOverview getFullOverview(@QueryParam("filter") String filter) {
		return getOverview(null, filter);
	}
	
	public static MetricOverview build(EAIResourceRepository repository, Date since, String filter) {
		MetricOverview overview = new MetricOverview();
		for (String id : repository.getMetricInstances()) {
			if (filter != null && !id.matches(filter)) {
				continue;
			}
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
				artifactMetrics.setSince(since);
				artifactMetrics.setUntil(new Date(overview.getTimestamp()));
				Artifact artifact = repository.resolve(id);
				if (artifact != null) {
					artifactMetrics.setType(getType(artifact));
					artifactMetrics.getTags().put("class", artifact.getClass().getName());
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
						? ((HistorySink) sink).getSnapshotUntil(Services.DEFAULT_AMOUNT, overview.getTimestamp())
						: ((HistorySink) sink).getSnapshotBetween(since.getTime() + 1, overview.getTimestamp()));
				}
				else if (sink instanceof CurrentValueSink) {
					artifactMetrics.getGauges().put(sinkId, ((CurrentValueSink) sink).getCurrent());	
				}
				if (sink instanceof StatisticsContainer) {
					artifactMetrics.getStatistics().put(sinkId, new SinkStatisticsImpl(((StatisticsContainer) sink).getStatistics()));
				}
			}
		}
		return overview;
	}
	
	public static String getType(Artifact artifact) {
		// group services
		if (DefinedService.class.isAssignableFrom(artifact.getClass())) {
			return DefinedService.class.getName();
		}
		else {
			return artifact.getClass().getName();
		}
	}
	
}
