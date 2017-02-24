package be.nabu.eai.module.metrics.memory;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseJAXBGUIManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;

public class MetricsMemoryGUIManager extends BaseJAXBGUIManager<MetricsMemoryConfiguration, MetricsMemoryArtifact> {

	public MetricsMemoryGUIManager() {
		super("Memory Database", MetricsMemoryArtifact.class, new MetricsMemoryManager(), MetricsMemoryConfiguration.class);
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		return null;
	}

	@Override
	protected MetricsMemoryArtifact newInstance(MainController controller, RepositoryEntry entry, Value<?>...values) throws IOException {
		return new MetricsMemoryArtifact(entry.getId(), entry.getContainer(), entry.getRepository());
	}

	@Override
	public String getCategory() {
		return "Metrics";
	}

}
