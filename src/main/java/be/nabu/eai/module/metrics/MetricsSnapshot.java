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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import nabu.misc.metrics.Services;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.metrics.core.SinkStatisticsImpl;
import be.nabu.libs.metrics.core.api.CurrentValueSink;
import be.nabu.libs.metrics.core.api.HistorySink;
import be.nabu.libs.metrics.core.api.ListableSinkProvider;
import be.nabu.libs.metrics.core.api.Sink;
import be.nabu.libs.metrics.core.api.SinkSnapshot;
import be.nabu.libs.metrics.core.api.SinkStatistics;
import be.nabu.libs.metrics.core.api.SinkValue;
import be.nabu.libs.metrics.core.api.StatisticsContainer;
import be.nabu.libs.services.api.DefinedService;

// note that gauges are generally automatically historized
// if that is the case, they will build up a history sink and do not need to be added as a separate concept to the metrics
@XmlRootElement(name = "metrics")
public class MetricsSnapshot {
	
	public static MetricsSnapshot build(ListableSinkProvider provider, String id, String category, Date until, Date since) {
		return build(provider, id, category, since, until, null);
	}
	
	public static MetricsSnapshot build(ListableSinkProvider provider, String id, String category, Date until, int amount) {
		return build(provider, id, category, null, until, amount);
	}
	
	private static String getType(String id) {
		Artifact artifact = EAIResourceRepository.getInstance().resolve(id);
		if (artifact != null) {
			// group services
			if (DefinedService.class.isAssignableFrom(artifact.getClass())) {
				return DefinedService.class.getName();
			}
			else {
				return artifact.getClass().getName();
			}
		}
		return null;
	}	
	
	private static MetricsSnapshot build(ListableSinkProvider provider, String id, String category, Date since, Date until, Integer amount) {
		if (until == null) {
			// do minus 1 to make sure all results are in
			until = new Date(new Date().getTime() - 1);
		}
		MetricsSnapshot snapshot = new MetricsSnapshot();
		snapshot.setSince(since);
		snapshot.setUntil(until);
		snapshot.setAmount(amount);
		Map<String, List<String>> sinks = provider.getSinks();
		List<ArtifactMetrics> artifacts = new ArrayList<ArtifactMetrics>();
		for (Map.Entry<String, List<String>> entry : sinks.entrySet()) {
			// filter on id
			if (id != null && !entry.getKey().equals(id) && !entry.getKey().startsWith(id + ".")) {
				continue;
			}
			ArtifactMetrics artifact = new ArtifactMetrics();
			artifact.setId(entry.getKey());
			if (entry.getValue().contains("lambdaExecutionTime")) {
				artifact.setType("lambda");
			}
			else if (entry.getValue().contains("scriptExecutionTime")) {
				artifact.setType("script");
			}
			else if (entry.getValue().contains("methodExecutionTime")) {
				artifact.setType("method");
			}
			else {
				artifact.setType(getType(entry.getKey()));
			}
			List<ArtifactMetricsSink> results = new ArrayList<ArtifactMetricsSink>();
			for (String sinkId : entry.getValue()) {
				// filter on category
				if (category != null && !sinkId.equals(category)) {
					continue;
				}
				ArtifactMetricsSink result = new ArtifactMetricsSink();
				result.setName(sinkId);
				Sink sink = provider.getSink(entry.getKey(), sinkId);
				// you can specifically not request the history to prevent overhead
				if (sink instanceof HistorySink && (amount == null || amount != 0)) {
					SinkSnapshot values;
					if (since == null) {
						values = ((HistorySink) sink).getSnapshotUntil(amount == null ? Services.DEFAULT_AMOUNT : amount, until.getTime());
					}
					else {
						values = ((HistorySink) sink).getSnapshotBetween(since.getTime(), until.getTime());
					}
					if (!values.getValues().isEmpty()) {
						result.setHistory(values.getValues());
						result.setLatest(values.getValues().get(values.getValues().size() - 1));
					}
				}
				if (sink instanceof CurrentValueSink && result.getLatest() == null) {
					result.setLatest(((CurrentValueSink) sink).getCurrent());
				}
				if (sink instanceof StatisticsContainer) {
					result.setStatistics(new SinkStatisticsImpl(((StatisticsContainer) sink).getStatistics()));
				}
				// only add it if it has any values at all, otherwise too much clutter
				if (result.getHistory() != null || result.getStatistics() != null || result.getLatest() != null) {
					results.add(result);
				}
			}
			artifact.setSinks(results);
			if (artifact.getSinks() != null && !artifact.getSinks().isEmpty()) {
				artifacts.add(artifact);
			}
		}
		snapshot.setMetrics(artifacts);
		return snapshot;
	}
	
	private Date since, until;
	private Integer amount;
	private List<ArtifactMetrics> metrics = new ArrayList<ArtifactMetrics>();
	
	public Date getSince() {
		return since;
	}
	public void setSince(Date since) {
		this.since = since;
	}
	public Date getUntil() {
		return until;
	}
	public void setUntil(Date until) {
		this.until = until;
	}
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	public List<ArtifactMetrics> getMetrics() {
		return metrics;
	}
	public void setMetrics(List<ArtifactMetrics> metrics) {
		this.metrics = metrics;
	}

	// a single sink for an artifact
	public static class ArtifactMetricsSink {
		private String name;
		// gauges only have this, historic sinks have their last value pushed here
		private SinkValue latest;
		// historic sinks have this
		private List<SinkValue> history;
		// statistics for this sink (if any)
		private SinkStatistics statistics;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public SinkValue getLatest() {
			return latest;
		}
		public void setLatest(SinkValue latest) {
			this.latest = latest;
		}
		public List<SinkValue> getHistory() {
			return history;
		}
		public void setHistory(List<SinkValue> history) {
			this.history = history;
		}
		public SinkStatistics getStatistics() {
			return statistics;
		}
		public void setStatistics(SinkStatistics statistics) {
			this.statistics = statistics;
		}
	}
	
	public static class ArtifactMetrics {
		// the id of the artifact
		private String id;
		// the type of the artifact (class)
		private String type;
		// the sinks for this artifact
		private List<ArtifactMetricsSink> sinks;
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public List<ArtifactMetricsSink> getSinks() {
			return sinks;
		}
		public void setSinks(List<ArtifactMetricsSink> sinks) {
			this.sinks = sinks;
		}
	}
}
