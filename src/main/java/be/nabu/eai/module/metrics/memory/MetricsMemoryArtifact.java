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

package be.nabu.eai.module.metrics.memory;

import java.util.Date;
import java.util.List;
import java.util.Map;

import be.nabu.eai.module.metrics.database.MetricsDatabaseArtifact;
import be.nabu.eai.repository.api.ListableSinkProviderArtifact;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;
import be.nabu.libs.metrics.core.api.ListableSinkProvider;
import be.nabu.libs.metrics.core.api.Sink;
import be.nabu.libs.metrics.core.sinks.LimitedHistorySinkProvider;
import be.nabu.libs.resources.api.ResourceContainer;

public class MetricsMemoryArtifact extends JAXBArtifact<MetricsMemoryConfiguration> implements ListableSinkProviderArtifact {

	private ListableSinkProvider provider;
	private Date lastPushed;
	
	public MetricsMemoryArtifact(String id, ResourceContainer<?> directory, Repository repository) {
		super(id, directory, repository, "metrics-memory.xml", MetricsMemoryConfiguration.class);
	}

	@Override
	public Sink getSink(String id, String category) {
		return getProvider().getSink(id, category);
	}

	@Override
	public Map<String, List<String>> getSinks() {
		return getProvider().getSinks();
	}

	private ListableSinkProvider getProvider() {
		if (provider == null) {
			synchronized(this) {
				if (provider == null) {
					provider = new LimitedHistorySinkProvider(getConfig().getAmount() == null ? 1000 : getConfig().getAmount());
				}
			}
		}
		return provider;
	}

	@Override
	public void setLastPushed(Date lastPushed) {
		if (lastPushed == null) {
			this.lastPushed = MetricsDatabaseArtifact.calculateLastPushed(this);
		}
		this.lastPushed = lastPushed;
	}

	@Override
	public Date getLastPushed() {
		return lastPushed;
	}

}
