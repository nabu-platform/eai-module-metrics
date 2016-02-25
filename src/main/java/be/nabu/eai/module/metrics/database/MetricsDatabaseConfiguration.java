package be.nabu.eai.module.metrics.database;

import java.net.URI;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "metricsDatabase")
public class MetricsDatabaseConfiguration {
	private URI temporary, storage;
	private Long partitionInterval, partitionSize;
	
	public URI getTemporary() {
		return temporary;
	}
	public void setTemporary(URI temporary) {
		this.temporary = temporary;
	}
	public URI getStorage() {
		return storage;
	}
	public void setStorage(URI storage) {
		this.storage = storage;
	}
	public Long getPartitionInterval() {
		return partitionInterval;
	}
	public void setPartitionInterval(Long partitionInterval) {
		this.partitionInterval = partitionInterval;
	}
	public Long getPartitionSize() {
		return partitionSize;
	}
	public void setPartitionSize(Long partitionSize) {
		this.partitionSize = partitionSize;
	}
	
}
