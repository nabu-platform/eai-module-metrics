package be.nabu.eai.module.metrics.beans;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import be.nabu.libs.metrics.core.api.SinkSnapshot;
import be.nabu.libs.metrics.core.api.SinkValue;

@XmlType(propOrder = { "id", "artifactType", "since", "level", "snapshots", "gauges" })
public class ArtifactMetrics {
	
	private String id, artifactType;
	private Map<String, SinkSnapshot> snapshots;
	private Map<String, SinkValue> gauges;
	private Date since;
	private int level;
	
	@XmlAttribute
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	@XmlAttribute
	public String getArtifactType() {
		return artifactType;
	}
	public void setArtifactType(String artifactType) {
		this.artifactType = artifactType;
	}
	
	@XmlAttribute
	public Date getSince() {
		return since;
	}
	public void setSince(Date since) {
		this.since = since;
	}
	
	@XmlAttribute
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
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
}
