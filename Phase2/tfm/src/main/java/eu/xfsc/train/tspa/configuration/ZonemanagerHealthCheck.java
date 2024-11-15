package eu.xfsc.train.tspa.configuration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ZonemanagerHealthCheck implements HealthIndicator {

	
	private static final String STATUS = "/status";

	@Value("${zonemanager.Address}")
	private String ZonemanagerHealthCheckAddress;

	@Override
	public Health health() {
		
		if (isZonemanagerHealthy()) {
			Map<String , Object> zmMap = new HashMap<>();
			zmMap.put("Status", "Zonemanager is healthy");
			zmMap.put("Zonemanager-address", ZonemanagerHealthCheckAddress);
			return Health.up().withDetails(zmMap).build();
		} else {
			Map<String , Object> zmMap = new HashMap<>();
			zmMap.put("Status", "Zonemanager is not healthy");
			zmMap.put("Zonemanager-address", ZonemanagerHealthCheckAddress);
			return Health.down().withDetails(zmMap).build();
		}
	}

	private boolean isZonemanagerHealthy() {

		String urlString = ZonemanagerHealthCheckAddress + STATUS;
		HttpClient httpClient = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).GET().build();

		HttpResponse<Void> response = null;
		try {
			response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
		} catch (IOException | InterruptedException e) {
			return false;
		}
		if (response.statusCode() == 200) {
			return true;
		}
		return false;
	}

}
