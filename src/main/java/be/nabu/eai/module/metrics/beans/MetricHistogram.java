package be.nabu.eai.module.metrics.beans;

import javax.xml.bind.annotation.XmlAttribute;

public class MetricHistogram {
	private String name;
	private long amountOfEntries;
	private long [] lastEntries;
	private double median, mean, min, max, percentile75, percentile95, percentile98, percentile99, percentile999;
	
	@XmlAttribute
	public long getAmountOfEntries() {
		return amountOfEntries;
	}
	public void setAmountOfEntries(long amountOfEntries) {
		this.amountOfEntries = amountOfEntries;
	}
	public long[] getLastEntries() {
		return lastEntries;
	}
	public void setLastEntries(long[] lastEntries) {
		this.lastEntries = lastEntries;
	}
	
	@XmlAttribute
	public double getMedian() {
		return median;
	}
	public void setMedian(double median) {
		this.median = median;
	}
	
	@XmlAttribute
	public double getMean() {
		return mean;
	}
	public void setMean(double mean) {
		this.mean = mean;
	}
	
	@XmlAttribute
	public double getMin() {
		return min;
	}
	public void setMin(double min) {
		this.min = min;
	}
	
	@XmlAttribute
	public double getMax() {
		return max;
	}
	public void setMax(double max) {
		this.max = max;
	}
	
	@XmlAttribute
	public double getPercentile75() {
		return percentile75;
	}
	public void setPercentile75(double percentile75) {
		this.percentile75 = percentile75;
	}
	
	@XmlAttribute
	public double getPercentile95() {
		return percentile95;
	}
	public void setPercentile95(double percentile95) {
		this.percentile95 = percentile95;
	}
	
	@XmlAttribute
	public double getPercentile98() {
		return percentile98;
	}
	public void setPercentile98(double percentile98) {
		this.percentile98 = percentile98;
	}
	
	@XmlAttribute
	public double getPercentile99() {
		return percentile99;
	}
	public void setPercentile99(double percentile99) {
		this.percentile99 = percentile99;
	}
	
	@XmlAttribute
	public double getPercentile999() {
		return percentile999;
	}
	public void setPercentile999(double percentile999) {
		this.percentile999 = percentile999;
	}
	
	@XmlAttribute
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
