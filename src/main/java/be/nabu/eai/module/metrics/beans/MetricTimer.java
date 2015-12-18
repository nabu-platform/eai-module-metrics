package be.nabu.eai.module.metrics.beans;

import javax.xml.bind.annotation.XmlAttribute;

public class MetricTimer extends MetricHistogram {
	private double fifteenMinuteRate, fiveMinuteRate, oneMinuteRate, meanRate;

	@XmlAttribute
	public double getFifteenMinuteRate() {
		return fifteenMinuteRate;
	}
	public void setFifteenMinuteRate(double fifteenMinuteRate) {
		this.fifteenMinuteRate = fifteenMinuteRate;
	}

	@XmlAttribute
	public double getFiveMinuteRate() {
		return fiveMinuteRate;
	}
	public void setFiveMinuteRate(double fiveMinuteRate) {
		this.fiveMinuteRate = fiveMinuteRate;
	}

	@XmlAttribute
	public double getOneMinuteRate() {
		return oneMinuteRate;
	}
	public void setOneMinuteRate(double oneMinuteRate) {
		this.oneMinuteRate = oneMinuteRate;
	}

	@XmlAttribute
	public double getMeanRate() {
		return meanRate;
	}
	public void setMeanRate(double meanRate) {
		this.meanRate = meanRate;
	}
	
}
