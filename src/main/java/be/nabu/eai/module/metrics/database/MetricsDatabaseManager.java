package be.nabu.eai.module.metrics.database;

import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.managers.base.JAXBArtifactManager;
import be.nabu.libs.resources.api.ResourceContainer;

public class MetricsDatabaseManager extends JAXBArtifactManager<MetricsDatabaseConfiguration, MetricsDatabaseArtifact> {

	public MetricsDatabaseManager() {
		super(MetricsDatabaseArtifact.class);
	}

	@Override
	protected MetricsDatabaseArtifact newInstance(String id, ResourceContainer<?> container, Repository repository) {
		return new MetricsDatabaseArtifact(id, container, repository);
	}

}
