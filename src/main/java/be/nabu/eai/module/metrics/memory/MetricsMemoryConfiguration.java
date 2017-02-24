package be.nabu.eai.module.metrics.memory;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "metricsMemory")
public class MetricsMemoryConfiguration {
	
	private Integer amount;

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	
}
