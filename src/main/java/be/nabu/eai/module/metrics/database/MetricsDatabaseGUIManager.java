package be.nabu.eai.module.metrics.database;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseJAXBGUIManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;

public class MetricsDatabaseGUIManager extends BaseJAXBGUIManager<MetricsDatabaseConfiguration, MetricsDatabaseArtifact> {

	public MetricsDatabaseGUIManager() {
		super("Metrics Database", MetricsDatabaseArtifact.class, new MetricsDatabaseManager(), MetricsDatabaseConfiguration.class);
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		return null;
	}

	@Override
	protected MetricsDatabaseArtifact newInstance(MainController controller, RepositoryEntry entry, Value<?>...values) throws IOException {
		return new MetricsDatabaseArtifact(entry.getId(), entry.getContainer(), entry.getRepository());
	}

	@Override
	public String getCategory() {
		return "Miscellaneous";
	}
	
}
