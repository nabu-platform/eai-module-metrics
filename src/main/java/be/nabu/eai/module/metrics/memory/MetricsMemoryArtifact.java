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
