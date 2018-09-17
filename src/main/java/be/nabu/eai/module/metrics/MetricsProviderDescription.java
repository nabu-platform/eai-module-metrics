package be.nabu.eai.module.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import be.nabu.libs.metrics.core.api.ListableSinkProvider;

@XmlRootElement(name = "description")
public class MetricsProviderDescription {
	
	public static MetricsProviderDescription describe(ListableSinkProvider provider) {
		MetricsProviderDescription description = new MetricsProviderDescription();
		Map<String, List<String>> sinks = provider.getSinks();
		List<MetricsProviderArtifact> artifacts = new ArrayList<MetricsProviderArtifact>();
		for (Map.Entry<String, List<String>> entry : sinks.entrySet()) {
			MetricsProviderArtifact artifact = new MetricsProviderArtifact();
			artifact.setName(entry.getKey());
			artifact.setCategories(entry.getValue());
			artifacts.add(artifact);
		}
		description.setProviders(artifacts);
		return description;
	}
	
	private List<MetricsProviderArtifact> providers;
	
	public List<MetricsProviderArtifact> getProviders() {
		return providers;
	}
	public void setProviders(List<MetricsProviderArtifact> providers) {
		this.providers = providers;
	}

	public static class MetricsProviderArtifact {
		private String name;
		private List<String> categories;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public List<String> getCategories() {
			return categories;
		}
		public void setCategories(List<String> categories) {
			this.categories = categories;
		}
	}
}
