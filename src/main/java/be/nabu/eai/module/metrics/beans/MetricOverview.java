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

package be.nabu.eai.module.metrics.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "overview")
public class MetricOverview {
	
	/**
	 * Subtract one so we are _sure_ that all the metrics are "in"
	 */
	private long timestamp = new Date().getTime() - 1;
	
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
