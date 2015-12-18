package be.nabu.eai.module.metrics.beans;

import javax.xml.bind.annotation.XmlAttribute;

public class MetricValue {
	private String name;
	private long currentValue;
	
	@XmlAttribute
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@XmlAttribute
	public long getCurrentValue() {
		return currentValue;
	}
	public void setCurrentValue(long currentValue) {
		this.currentValue = currentValue;
	}
}
