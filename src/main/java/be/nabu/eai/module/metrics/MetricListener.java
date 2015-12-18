package be.nabu.eai.module.metrics;

import java.net.URI;
import java.util.SortedMap;

import org.bouncycastle.util.Arrays;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

import be.nabu.eai.module.metrics.beans.MetricHistogram;
import be.nabu.eai.module.metrics.beans.MetricOverview;
import be.nabu.eai.module.metrics.beans.MetricTimer;
import be.nabu.eai.module.metrics.beans.MetricValue;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.server.Server;
import be.nabu.eai.server.api.ServerListener;
import be.nabu.libs.events.api.EventHandler;
import be.nabu.libs.events.api.EventSubscription;
import be.nabu.libs.http.HTTPException;
import be.nabu.libs.http.api.HTTPRequest;
import be.nabu.libs.http.api.HTTPResponse;
import be.nabu.libs.http.api.server.HTTPServer;
import be.nabu.libs.http.core.HTTPUtils;
import be.nabu.libs.http.server.HTTPServerUtils;
import be.nabu.libs.metrics.codahale.CodahaleMetricInstance;

public class MetricListener implements ServerListener {

	@Override
	public void listen(Server server, HTTPServer httpServer) {
		EventSubscription<HTTPRequest, HTTPResponse> subscription = httpServer.getDispatcher().subscribe(HTTPRequest.class, new EventHandler<HTTPRequest, HTTPResponse>() {
			@SuppressWarnings("rawtypes")
			@Override
			public HTTPResponse handle(HTTPRequest request) {
				try {
					URI uri = HTTPUtils.getURI(request, false);
					if (!uri.getPath().startsWith("/metric/")) {
						return null;
					}
					String id = uri.getPath().substring("/metric/".length());
					if (id.isEmpty()) {
						return null;
					}
					EAIResourceRepository repository = (EAIResourceRepository) server.getRepository();
					CodahaleMetricInstance metricInstance = repository.getMetricInstance(id);
					if (metricInstance == null) {
						return null;
					}
					MetricOverview overview = new MetricOverview();
					overview.setId(id);
					
					SortedMap<String, Gauge> gauges = metricInstance.getRegistry().getGauges();
					for (String name : gauges.keySet()) {
						MetricValue value = new MetricValue();
						value.setName(name.substring(id.length() + 1));
						value.setCurrentValue((Long) gauges.get(name).getValue());
						overview.getValues().add(value);
					}
					
					SortedMap<String, Histogram> histograms = metricInstance.getRegistry().getHistograms();
					for (String name : histograms.keySet()) {
						MetricHistogram histogram = new MetricHistogram();
						histogram.setName(name.substring(id.length() + 1));
						map(histograms.get(name).getSnapshot(), histogram, 100);
						overview.getHistograms().add(histogram);
					}
					
					SortedMap<String, Timer> timers = metricInstance.getRegistry().getTimers();
					for (String name : timers.keySet()) {
						MetricTimer timer = new MetricTimer();
						timer.setName(name.substring(id.length() + 1));
						map(timers.get(name).getSnapshot(), timer, 100);
						overview.getTimers().add(timer);
					}
				}
				catch (Exception e) {
					throw new HTTPException(500, e);
				}
				return null;
			}
		});
		subscription.filter(HTTPServerUtils.limitToPath("/metric"));
	}

	public static void map(Snapshot snapshot, MetricHistogram histogram, int amountOfValues) {
		histogram.setAmountOfEntries(snapshot.size());
		histogram.setMax(snapshot.getMax());
		histogram.setMin(snapshot.getMin());
		histogram.setMedian(snapshot.getMedian());
		histogram.setMean(snapshot.getMean());
		histogram.setPercentile75(snapshot.get75thPercentile());
		histogram.setPercentile95(snapshot.get95thPercentile());
		histogram.setPercentile98(snapshot.get98thPercentile());
		histogram.setPercentile99(snapshot.get99thPercentile());
		histogram.setPercentile999(snapshot.get999thPercentile());
		long[] values = snapshot.getValues();
		histogram.setLastEntries(Arrays.copyOfRange(values, values.length - amountOfValues, values.length));
	}
	
}
