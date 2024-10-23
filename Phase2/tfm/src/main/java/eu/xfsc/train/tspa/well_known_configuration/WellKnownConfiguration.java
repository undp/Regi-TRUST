package eu.xfsc.train.tspa.well_known_configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.codec.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.xfsc.train.tspa.exceptions.PropertiesAccessException;
import eu.xfsc.train.tspa.signer.VcSignHandler;
import eu.xfsc.train.tspa.utils.VCUtil;
import foundation.identity.jsonld.JsonLDException;


@Component
public class WellKnownConfiguration implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger log = LoggerFactory.getLogger(WellKnownConfiguration.class);

	private static final String SET_SIGNATURE_PURPOSE = "well-known";
	private static final String HASH = "#";

	
	
	@Autowired
	private VcSignHandler oVcSigner;


	@Value("classpath:templates/well-knownVC.json")
	private Resource wellKnownResource;
	@Value("classpath:Vault/keyStore.txt")
	private Resource keystore;
	@Value("${well-known.issuer}")
	private String wkIssuer;
	@Value("${well-known.credentialSubject.origin}")
	private String wkCredentialSubjectOrigin;
	@Value("${storage.path.well_known}")
	private String wkPath;
	@Value("${well-known.signer.type}")
	private String well_known_signer_type;
	@Value("${well-known.signer.key}")
	private String well_known_signer_key;

	/**
	 *
	 */
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		String prettyJsonString = null;
		String vcWithProofString = null;
		// Read the tamplate of well-known VC.

		log.info("---------------------------------- Well-Known VC is genrating ----------------------------");

		try {

			JsonObject wellKnowJsonObject = VCUtil.resource_to_JsonObject(wellKnownResource);
			JsonArray vcJsonarrArray = wellKnowJsonObject.getAsJsonArray("linked_dids");
			JsonObject vcJsonObject = (JsonObject) vcJsonarrArray.get(0);

			vcJsonObject.remove("proof"); // remove the Dummy proof from the template VC.

			// Set the properties in Well-Known did configuration.
			String wellKnownCredentialSubjectID = wkIssuer;
			String issuancedateString = null;

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
			TimeZone timeZone = TimeZone.getTimeZone("CET");
			dateFormat.setTimeZone(timeZone);
			issuancedateString = dateFormat.format(new Date());

			log.info("Issuer : {}", wkIssuer);
			log.info("Credential subject ID : {} ", wellKnownCredentialSubjectID);
			log.info("Credential subject Origin :{}", wkCredentialSubjectOrigin);
			log.info("Issue Date of well-known : {}", issuancedateString);

			vcJsonObject.addProperty("issuer", wkIssuer);
			vcJsonObject.addProperty("issuanceDate", issuancedateString);
			JsonObject credentialSubjectJsonObject = vcJsonObject.getAsJsonObject("credentialSubject");

			credentialSubjectJsonObject.addProperty("id", wellKnownCredentialSubjectID);
			credentialSubjectJsonObject.addProperty("origin", wkCredentialSubjectOrigin);
			
			// Sign the updated VC and add proof in the VC.
			if (well_known_signer_type.equals(VcSignHandler.SET_SIGNER_INTERNAL)) {
				String wkVerificationMethodString = wkIssuer + HASH + well_known_signer_key;
				log.debug("wkvmethod {}",wkVerificationMethodString);
				vcWithProofString = oVcSigner
					.creatLDProofString(vcJsonObject.toString(), wkVerificationMethodString, SET_SIGNATURE_PURPOSE)
					.toString();
			}
			// Sign the updated vc using TSA which adds proof to it
			else if (well_known_signer_type.equals(VcSignHandler.SET_SIGNER_TSA)) {
				vcWithProofString = oVcSigner.TSASigner(vcJsonObject);
			}
			// creat JWT
			String jWTVCString = oVcSigner.creatJWTString(vcJsonObject.toString(), SET_SIGNATURE_PURPOSE);

			vcJsonObject.addProperty("proof", vcWithProofString);

			JsonObject vcWithJsonObject = (JsonObject) JsonParser.parseString(vcWithProofString);

			vcJsonarrArray.set(0, vcWithJsonObject);
			vcJsonarrArray.add(jWTVCString);
			;

			wellKnowJsonObject.add("linked_dids", vcJsonarrArray);

			// To so proper Indentation of Json Object.
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonElement je = JsonParser.parseString(wellKnowJsonObject.toString());
			prettyJsonString = gson.toJson(je);

			// System.out.println(prettyJsonString);

			VCUtil.vcStore(wkPath, prettyJsonString, "did-configuration");

		} catch (PropertiesAccessException | IOException | DecoderException | GeneralSecurityException
				| JsonLDException e) {
			e.printStackTrace();
		}
	}
}
