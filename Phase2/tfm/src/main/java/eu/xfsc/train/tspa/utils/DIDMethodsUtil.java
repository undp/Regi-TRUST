package eu.xfsc.train.tspa.utils;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.xfsc.train.tspa.configuration.DIDMethodConfig;
import eu.xfsc.train.tspa.exceptions.PropertiesAccessException;
import eu.xfsc.train.tspa.utilities.AbsDIDMethodUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
public class DIDMethodsUtil extends AbsDIDMethodUtil {

	private static final Logger log = LoggerFactory.getLogger(DIDMethodsUtil.class);

	private static final String WELL_KNOWN_PATH = "/.well-known/did-configuration.json";
	private static final String HTTPS = "https://";

	@Autowired
	private DIDMethodConfig didMethodConfig;

	private OkHttpClient mClient = new OkHttpClient();

	// --> Well-known configuration verification method for the did:web.
	@Override
	public boolean isWellknownValid(String did) {
		log.debug("isWellknownValid; verification start for {}", did);

		String getDomainName = did.substring(8);
		String endpoint = HTTPS + getDomainName + WELL_KNOWN_PATH;
		log.debug("isWellknownValid; Well-Known endpoint: {}", endpoint);

		Request request = buildRestDeleteRequest(endpoint);
		log.debug("isWellknownValid; Request: {}", request);

		int responseCode = sendRequest(request);

		if (responseCode == 200) {
			log.info("isWellknownValid; {} is valid", did);
			return true;
		}

		return false;

	}

	// --> Method for checking Publishing(URI record) did is from "application.yml"
	@Override
	public boolean isDIDMethodValid(String didMethod) throws PropertiesAccessException {

		if (didMethod != null) {
			if (didMethodConfig.getDidMethods().isEmpty()) {
				log.error("isDIDMethodValid; method list empty");
				throw new PropertiesAccessException("DID method is not configured in the 'application.yml'.");
			}
			for (String method : didMethodConfig.getDidMethods()) {
				if (didMethod.startsWith(method))
					return true;
			}
		}
		return false;
	}

	private Request buildRestDeleteRequest(String endpoint) {

		return new Request.Builder().url(endpoint).get().build();
	}

	private int sendRequest(Request request) {
		int statusCode = 0;

		try {

			Response response = mClient.newCall(request).execute();
			statusCode = response.code();
			log.debug("Response Code: {}", statusCode);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return statusCode;
	}
}
