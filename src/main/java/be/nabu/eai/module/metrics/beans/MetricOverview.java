package be.nabu.eai.module.metrics.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "overview")
public class MetricOverview {
	
	private long timestamp = new Date().getTime();
	private List<ArtifactMetrics> metrics = new ArrayList<ArtifactMetrics>();

	public List<ArtifactMetrics> getMetrics() {
		return metrics;
	}
	public void setMetrics(List<ArtifactMetrics> metrics) {
		this.metrics = metrics;
	}
	
	@XmlAttribute
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
}
