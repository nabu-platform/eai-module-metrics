package be.nabu.eai.module.metrics.memory;

import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.managers.base.JAXBArtifactManager;
import be.nabu.libs.resources.api.ResourceContainer;

public class MetricsMemoryManager extends JAXBArtifactManager<MetricsMemoryConfiguration, MetricsMemoryArtifact> {

	public MetricsMemoryManager() {
		super(MetricsMemoryArtifact.class);
	}

	@Override
	protected MetricsMemoryArtifact newInstance(String id, ResourceContainer<?> container, Repository repository) {
		return new MetricsMemoryArtifact(id, container, repository);
	}

}
