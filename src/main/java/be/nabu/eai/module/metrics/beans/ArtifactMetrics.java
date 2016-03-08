package be.nabu.eai.module.metrics.beans;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import be.nabu.libs.metrics.core.api.SinkSnapshot;
import be.nabu.libs.metrics.core.api.SinkStatistics;
import be.nabu.libs.metrics.core.api.SinkValue;

@XmlType(propOrder = { "id", "type", "since", "until", "snapshots", "gauges", "statistics", "tags" })
public class ArtifactMetrics {
	
	private String id, type;
	private Map<String, SinkSnapshot> snapshots;
	private Map<String, SinkValue> gauges;
	private Map<String, SinkStatistics> statistics;
	private Map<String, String> tags;
	private Date since, until;
	
	@XmlAttribute
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	@XmlAttribute
	public String getType() {
		return type;
	}
	public void setType(String artifactType) {
		this.type = artifactType;
	}
	
	@XmlAttribute
	public Date getSince() {
		return since;
	}
	public void setSince(Date since) {
		this.since = since;
	}
	
	@XmlAttribute
	public Date getUntil() {
		return until;
	}
	public void setUntil(Date until) {
		this.until = until;
	}
	
	public Map<String, SinkSnapshot> getSnapshots() {
		if (snapshots == null) {
			snapshots = new HashMap<String, SinkSnapshot>();
		}
		return snapshots;
	}
	public void setSnapshots(Map<String, SinkSnapshot> snapshots) {
		this.snapshots = snapshots;
	}
	
	public Map<String, SinkValue> getGauges() {
		if (gauges == null) {
			gauges = new HashMap<String, SinkValue>();
		}
		return gauges;
	}
	public void setGauges(Map<String, SinkValue> gauges) {
		this.gauges = gauges;
	}
	
	public Map<String, SinkStatistics> getStatistics() {
		if (statistics == null) {
			statistics = new HashMap<String, SinkStatistics>();
		}
		return statistics;
	}
	public void setStatistics(Map<String, SinkStatistics> statistics) {
		this.statistics = statistics;
	}
	
	public Map<String, String> getTags() {
		if (tags == null) {
			tags = new HashMap<String, String>();
		}
		return tags;
	}
	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}
	
}
