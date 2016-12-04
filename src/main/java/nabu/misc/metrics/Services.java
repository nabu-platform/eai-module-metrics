package nabu.misc.metrics;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.eai.module.cluster.ClusterArtifact;
import be.nabu.eai.module.metrics.MetricsREST;
import be.nabu.eai.module.metrics.beans.MetricOverview;
import be.nabu.eai.module.metrics.database.MetricsDatabaseArtifact;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.server.ServerConnection;
import be.nabu.libs.http.api.HTTPResponse;
import be.nabu.libs.http.api.client.HTTPClient;
import be.nabu.libs.http.core.DefaultHTTPRequest;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.binding.api.Window;
import be.nabu.libs.types.binding.xml.XMLBinding;
import be.nabu.libs.types.java.BeanResolver;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.mime.api.ContentPart;
import be.nabu.utils.mime.impl.FormatException;
import be.nabu.utils.mime.impl.MimeHeader;
import be.nabu.utils.mime.impl.PlainMimeEmptyPart;

@WebService
public class Services {
	
	private ExecutionContext context;
	
	@WebResult(name = "metrics")
	public MetricOverview poll(@WebParam(name = "host") String host, @WebParam(name = "since") Date since) throws IOException, FormatException, ParseException {
		ServerConnection connection = null;
		if (host != null) {
			for (ClusterArtifact cluster : EAIResourceRepository.getInstance().getArtifacts(ClusterArtifact.class)) {
				if (cluster.getConfig().getHosts() != null && cluster.getConfig().getHosts().contains(host)) {
					connection = cluster.getConnection(host);
					if (connection != null) {
						break;
					}
				}
			}
			if (connection == null) {
				throw new IllegalArgumentException("Can not find cluster object for host: " + host);
			}
		}
		if (connection != null) {
			HTTPClient client = connection.getClient();
			HTTPResponse response = client.execute(new DefaultHTTPRequest("GET", since == null ? "/metrics" : "/metrics/" + since.getTime(), new PlainMimeEmptyPart(null,
				new MimeHeader("Content-Length", "0"),
				new MimeHeader("Accept", "application/xml"),
				new MimeHeader("Accept-Encoding", "gzip"),
				new MimeHeader("Host", host)
			)), connection.getPrincipal(), false, false);
			if (response.getCode() != 200) {
				throw new IllegalStateException("The remote server returned " + response.getCode() + ": " + response.getMessage());
			}
			XMLBinding binding = new XMLBinding((ComplexType) BeanResolver.getInstance().resolve(MetricOverview.class), Charset.forName("UTF-8"));
			return TypeUtils.getAsBean(binding.unmarshal(IOUtils.toInputStream(((ContentPart) response.getContent()).getReadable()), new Window[0]), MetricOverview.class);
		}
		// we are on the current repository
		else {
			return MetricsREST.build(EAIResourceRepository.getInstance(), since);
		}
	}
	
	@WebResult(name = "lastPushed")
	public Date lastPushed(@NotNull @WebParam(name = "metricsDatabaseId") String metricsDatabaseId) {
		MetricsDatabaseArtifact database = context.getServiceContext().getResolver(MetricsDatabaseArtifact.class).resolve(metricsDatabaseId);
		if (database == null) {
			throw new IllegalArgumentException("The metrics database does not exist: " + metricsDatabaseId);
		}
		return database.getLastPushed();
	}
	
	public void persist(@NotNull @WebParam(name = "metricsDatabaseId") String metricsDatabaseId, @NotNull @WebParam(name = "metrics") MetricOverview overview) {
		MetricsDatabaseArtifact database = context.getServiceContext().getResolver(MetricsDatabaseArtifact.class).resolve(metricsDatabaseId);
		if (database == null) {
			throw new IllegalArgumentException("The metrics database does not exist: " + metricsDatabaseId);
		}
		database.persist(overview);
	}
	
	@WebResult(name = "metrics")
	public MetricOverview select(@NotNull @WebParam(name = "metricsDatabaseId") String metricsDatabaseId, @WebParam(name = "since") Date since, @WebParam(name = "until") Date until) {
		MetricsDatabaseArtifact database = context.getServiceContext().getResolver(MetricsDatabaseArtifact.class).resolve(metricsDatabaseId);
		if (database == null) {
			throw new IllegalArgumentException("The metrics database does not exist: " + metricsDatabaseId);
		}
		return database.select(since, until);
	}
	
	@WebResult(name = "metricDatabaseIds")
	public List<String> metricDatabases() {
		List<String> ids = new ArrayList<String>();
		for (MetricsDatabaseArtifact artifact : EAIResourceRepository.getInstance().getArtifacts(MetricsDatabaseArtifact.class)) {
			ids.add(artifact.getId());
		}
		return ids;
	}
}
