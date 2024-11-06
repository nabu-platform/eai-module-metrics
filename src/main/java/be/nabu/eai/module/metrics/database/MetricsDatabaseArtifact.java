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
import java.util.Date;
import java.util.List;
import java.util.Map;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.ListableSinkProviderArtifact;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;
import be.nabu.libs.metrics.core.api.HistorySink;
import be.nabu.libs.metrics.core.api.ListableSinkProvider;
import be.nabu.libs.metrics.core.api.Sink;
import be.nabu.libs.metrics.core.api.SinkSnapshot;
import be.nabu.libs.metrics.database.PartitionedSinkProvider;
import be.nabu.libs.metrics.database.api.PartitionConfigurationProvider;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;

public class MetricsDatabaseArtifact extends JAXBArtifact<MetricsDatabaseConfiguration> implements ListableSinkProviderArtifact {

	/**
	 * This is the last pushed for the entire database, you can not (currently) request a last pushed on a sink-by-sink basis
	 */
	private Date lastPushed;
	private ListableSinkProvider provider;
	
	public MetricsDatabaseArtifact(String id, ResourceContainer<?> directory, Repository repository) {
		super(id, directory, repository, "metrics-database.xml", MetricsDatabaseConfiguration.class);
	}
	
	public void setLastPushed(Date lastPushed) {
		this.lastPushed = lastPushed;
	}
	
	public Date getLastPushed() {
		if (lastPushed == null) {
			this.lastPushed = calculateLastPushed(this);
		}
		return lastPushed;
	}

	public static Date calculateLastPushed(ListableSinkProviderArtifact provider) {
		// calculate the last pushed based on the most recent data available across all sinks
		Map<String, List<String>> sinks = provider.getSinks();
		Date lastPushed = null;
		long time = new Date().getTime();
		for (String id : sinks.keySet()) {
			for (String category : sinks.get(id)) {
				HistorySink sink = (HistorySink) provider.getSink(id, category);
				SinkSnapshot snapshot = sink.getSnapshotUntil(1, time);
				if (!snapshot.getValues().isEmpty()) {
					if (lastPushed == null || snapshot.getValues().get(0).getTimestamp() > lastPushed.getTime()) {
						lastPushed = new Date(snapshot.getValues().get(0).getTimestamp());
					}
				}
			}
		}
		return lastPushed;
	}

	public ListableSinkProvider getProvider() {
		if (provider == null) {
			try {
				synchronized(this) {
					if (provider == null) {
						URI storageURI = getConfiguration().getStorage();
						Resource storage;
						if (storageURI == null) {
							storage = ResourceUtils.mkdirs(getDirectory(), EAIResourceRepository.PUBLIC);
						}
						else {
							storage = ResourceUtils.mkdir(storageURI, null);
						}
						Resource temporary;
						URI temporaryURI = getConfiguration().getTemporary();
						if (temporaryURI == null) {
							temporary = ResourceUtils.mkdirs(getDirectory(), EAIResourceRepository.PRIVATE);
						}
						else {
							temporary = ResourceUtils.mkdir(temporaryURI, null);
						}
						provider = new PartitionedSinkProvider(new PartitionConfigurationProvider() {
							@Override
							public long getPartitionInterval(String id, String category) {
								try {
									Long partitionInterval = getConfiguration().getPartitionInterval();
									if (partitionInterval == null) {
										// 1 hour
										partitionInterval = 1000l*60*60;
									}
									return partitionInterval;
								}
								catch (Exception e) {
									throw new RuntimeException(e);
								}
							}
							@Override
							public long getPartitionSize(String id, String category) {
								try {
									Long partitionSize = getConfiguration().getPartitionSize();
									return partitionSize == null ? 0 : partitionSize;
								}
								catch (Exception e) {
									throw new RuntimeException(e);
								}
							}
						}, (ResourceContainer<?>) storage, (ResourceContainer<?>) temporary);
					}
				}
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return provider;
	}

	@Override
	public Map<String, List<String>> getSinks() {
		return getProvider().getSinks();
	}
	
	@Override
	public Sink getSink(String id, String category) {
		return getProvider().getSink(id, category);
	}
	
}
