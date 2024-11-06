/*
* Copyright (C) 2015 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
