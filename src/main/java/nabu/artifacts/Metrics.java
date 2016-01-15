package nabu.artifacts;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Date;

import javax.jws.WebParam;
import javax.jws.WebService;

import be.nabu.eai.module.cluster.ClusterArtifact;
import be.nabu.eai.module.metrics.MetricsREST;
import be.nabu.eai.module.metrics.beans.MetricOverview;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.server.ServerConnection;
import be.nabu.libs.http.api.HTTPResponse;
import be.nabu.libs.http.api.client.HTTPClient;
import be.nabu.libs.http.core.DefaultHTTPRequest;
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
public class Metrics {
	
	public MetricOverview metrics(@WebParam(name = "host") String host, @WebParam(name = "since") Date since) throws IOException, FormatException, ParseException {
		ServerConnection connection = null;
		if (host != null) {
			for (ClusterArtifact cluster : EAIResourceRepository.getInstance().getArtifacts(ClusterArtifact.class)) {
				connection = cluster.getConnection(host);
				if (connection != null) {
					break;
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
				new MimeHeader("Accept", "gzip"),
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
}
