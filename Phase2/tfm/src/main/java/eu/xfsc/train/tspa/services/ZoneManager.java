package eu.xfsc.train.tspa.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;

import eu.xfsc.train.tspa.exceptions.InvalidStatusCodeException;
import eu.xfsc.train.tspa.interfaces.IZoneManager;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class ZoneManager implements IZoneManager {

	private static final Logger log = LoggerFactory.getLogger(ZoneManager.class);

	private static final String NAMES = "names";
	private static final String TRUST_LIST = "trust-list";
	private static final String SCHEMES = "schemes";
	private static final String SEP = "/";
	private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("Application/Json");

	private OkHttpClient mClient = new OkHttpClient();

	@Value("${zonemanager.Address}")
	private String CONFIG_PROPERTY_ZONEMANAGER;

	
	@Value("${zonemanager.token-server-url}")
	private String tokenServerUrl;
	@Value("${zonemanager.grant-type}")
	private String grantTypeValue;
	@Value("${zonemanager.client-id}")
	private String clientIdValue;
	@Value("${zonemanager.client-secret}")
	private String clientSecretValue;

	@Value("${zonemanager.query.status}")
	private boolean CONFIG_NS_QUERY_STATUS;

	// Functionality for Publishing PTR record in the Zone.
	@Override
	public int publishTrustSchemes(String schemeName, String service) throws IOException, InvalidStatusCodeException {

		String endpoint = buildFullPath(buildSchemeEndpointByServiceName(schemeName));
		log.debug("Zone Manger PUT Endpoint: {}", endpoint);

		@SuppressWarnings("deprecation")
		RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, service);

		Request request = buildRestPutRequest(endpoint, body);
		log.info("Request : {}", request);

		int statusCode = sendRequest(request);
		return statusCode;
	}
	// Function for the Delete the PTR record from the Zone.
	@Override
	public int deleteTrustSchemes(String schemeName) throws IOException, InvalidStatusCodeException {
		String endpoint = buildFullPath(buildSchemeEndpointByServiceName(schemeName));
		log.debug("Zonemanager DELETE Endpoint: {}", endpoint);

		Request request = buildRestDeleteRequest(endpoint);
		log.info("Request : {}", request);

		return sendRequest(request);
	}

	// Function for the publishing URI (DID) record in the Zone.
	@Override
	public int publishDIDUri(String TrustFrameWorkName, String test_list_didString)
			throws IOException, InvalidStatusCodeException {

		String endpoint = buildFullPath(buildTrustListEndpointBySchemeName(TrustFrameWorkName));
		log.info("Endpoint for DID URI: {}", endpoint);

		@SuppressWarnings("deprecation")
		RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, test_list_didString);

		Request request = buildRestPutRequest(endpoint, body);

		int statusCode = sendRequest(request);

		return statusCode;
	}

	// Function for the Deleting the URI (DID) records form the Zone.
	@Override
	public int deleteDIDUriRecords(String TrustFrameWorkName) throws IOException, InvalidStatusCodeException {
		String endpoint = buildFullPath(buildTrustListEndpointBySchemeName(TrustFrameWorkName));
		log.debug("Zonemanager DELETE Endpoint: {}", endpoint);

		Request request = buildRestDeleteRequest(endpoint);
		log.info("Request : {}", request);

		return sendRequest(request);
	}

	/*
	 * Prepare request Endpoint for the Publishing and Deleting
	 * 
	 */

	private String buildTrustListEndpointBySchemeName(String schemeName) {
		return NAMES + SEP + schemeName + SEP + TRUST_LIST;
	}

	private String buildSchemeEndpointByServiceName(String serviceName) {
		return NAMES + SEP + serviceName + SEP + SCHEMES;
	}

	private String buildFullPath(String endpoint) {
		return getNameServer() + SEP + endpoint;
	}

	/*
	 * Configure Zonemanager Token.
	 * 
	 */

	private String getNameServer() {
		if (CONFIG_PROPERTY_ZONEMANAGER == null) {
			return null;
		}
		return CONFIG_PROPERTY_ZONEMANAGER;
	}

	private String getBearerToken() {
		
		 RequestBody requestBody = new FormBody.Builder()
	                .add("grant_type", grantTypeValue)
	                .add("client_id", clientIdValue)
	                .add("client_secret", clientSecretValue)
	                .build();
		 Request request = new Request.Builder()
	                .url(tokenServerUrl)
	                .post(requestBody)
	                .build();
		 try (Response response = mClient.newCall(request).execute()) {
	            if (!response.isSuccessful()) {
	                throw new RuntimeException("Failed to obtain access token. HTTP Status Code: " + response.code());
	            }
	            
	            String responseContent = response.body().string();	            
	            JsonElement jsonElement = JsonParser.parseString(responseContent);
	            JsonObject jsonObject = jsonElement.getAsJsonObject();
	    	    String token = jsonObject.get("access_token").getAsString();
	    	    
	    	    return token;
	           
		 }
		catch ( IOException e) {
			throw new RuntimeException("Error while generating access token: " + e.getMessage(), e);
		}
		        

	}

	private Request buildRestPutRequest(String endpoint, RequestBody body) {
		return new Request.Builder().url(endpoint).addHeader("Authorization", "Bearer " + getBearerToken()).put(body)
				.build();
	}

	private Request buildRestDeleteRequest(String endpoint) {

		return new Request.Builder().url(endpoint).addHeader("Authorization", "Bearer " + getBearerToken()).delete()
				.build();
	}

	// Sending the HTTPS request to Zone Manager.
	private int sendRequest(Request request) throws InvalidStatusCodeException, IOException {
		int statusCode = 0;

		try {
			if (CONFIG_NS_QUERY_STATUS) {

				Response response = mClient.newCall(request).execute();
				log.info("Response {} :", response.toString());
				statusCode = response.code();
				log.info("Response Code: {}", statusCode);

				if (statusCode < 200 || statusCode >= 400) {
					log.error("Error during the publication or Deleteing! Wrong status code {} received!", statusCode);
					throw new InvalidStatusCodeException(response.body().string(), statusCode);
				}
			}
		} catch (IOException e) {
			throw e;
		}

		return statusCode;
	}
	
	private String encodeMap(Map<String, String> data) {
        return data.entrySet().stream()
		        .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
		        .collect(Collectors.joining("&"));
    }

    private String encode(String value)  {
        try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		
    }

}
