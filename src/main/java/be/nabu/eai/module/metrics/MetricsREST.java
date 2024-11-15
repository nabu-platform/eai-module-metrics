/*
* Copyright (C) 2015 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
		return build(repository, since == null ? null : new Date(since), filter, true);
	}
	
	@GET
	@Path("/metrics")
	public MetricOverview getFullOverview(@QueryParam("filter") String filter) {
		return getOverview(null, filter);
	}
	
	public static MetricOverview build(EAIResourceRepository repository, Date since, String filter, boolean includeHistory) {
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
				if (sinkId.equals("lambdaExecutionTime") && artifactMetrics.getType() == null) {
					artifactMetrics.setType("lambda");
					artifactMetrics.getTags().put("class", "lambda");
				}
				else if (sinkId.equals("scriptExecutionTime") && artifactMetrics.getType() == null) {
					artifactMetrics.setType("script");
					artifactMetrics.getTags().put("class", "script");
				}
				else if (sinkId.equals("methodExecutionTime") && artifactMetrics.getType() == null) {
					artifactMetrics.setType("method");
					artifactMetrics.getTags().put("class", "method");
				}
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
