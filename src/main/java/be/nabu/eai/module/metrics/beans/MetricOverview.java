package be.nabu.eai.module.metrics.beans;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "overview")
public class MetricOverview {
	private List<ArtifactMetrics> metrics = new ArrayList<ArtifactMetrics>();

	public List<ArtifactMetrics> getMetrics() {
		return metrics;
	}
	public void setMetrics(List<ArtifactMetrics> metrics) {
		this.metrics = metrics;
	}
}
