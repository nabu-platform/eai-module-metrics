package be.nabu.eai.module.metrics.beans;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "metrics")
public class MetricOverview {
	private String id;
	private List<MetricValue> values = new ArrayList<MetricValue>();
	private List<MetricHistogram> histograms = new ArrayList<MetricHistogram>();
	private List<MetricTimer> timers = new ArrayList<MetricTimer>();
	
	public List<MetricValue> getValues() {
		return values;
	}
	public void setValues(List<MetricValue> values) {
		this.values = values;
	}
	public List<MetricHistogram> getHistograms() {
		return histograms;
	}
	public void setHistograms(List<MetricHistogram> histograms) {
		this.histograms = histograms;
	}
	public List<MetricTimer> getTimers() {
		return timers;
	}
	public void setTimers(List<MetricTimer> timers) {
		this.timers = timers;
	}
	@XmlAttribute
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}