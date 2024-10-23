package eu.xfsc.train.tspa.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.codec.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import eu.xfsc.train.tspa.exceptions.FileEmptyException;
import eu.xfsc.train.tspa.exceptions.PropertiesAccessException;
import eu.xfsc.train.tspa.interfaces.IVCService;
import eu.xfsc.train.tspa.signer.VcSignHandler;
import eu.xfsc.train.tspa.utils.Hashingalgo;
import eu.xfsc.train.tspa.utils.TSPAUtil;
import eu.xfsc.train.tspa.utils.VCUtil;
import foundation.identity.jsonld.JsonLDException;

@Service
public class VCServiceImpl implements IVCService {

	// Logger for logging messages
	private static final Logger log = LoggerFactory.getLogger(VCServiceImpl.class);

	// Constant for configuration keys
	private static final String HASH = "#";
	private static final String ISSUER_LISTS = "issuer-lists";
	private static final String SET_SIGNATURE_PURPOSE = "vc";
	private static final String TRUST_LIST_ROUTE = "/trust-list";


	
	@Autowired
	private VcSignHandler oProofSigner;

	@Value("classpath:templates/VC.json")
	private Resource vcResource;
	@Value("${storage.path.vc}")
	private String vcPath;
	@Value("${trustlist.vc.issuer}")
	private String trustList_Issuer;
	@Value("${storage.path.well-known}")
	private String wkPath;
	@Value("${trustlist.vc.hashAlgo}")
	private String vcHashingAlgo;
	@Value("${request.get.mapping}")
	private String requestMappingString;
	@Value("${trustlist.vc.signer.type}")
	private String vcsigner_type;
	@Value("${trustlist.vc.signer.key}")
	private String vcsigner_key;
	
	
	// --> Set the Storage path and creating directory in local store.
	
	private void setVCstorePath() throws PropertiesAccessException {
		//vcPath = TSPAUtil.getPropertieStringFromConfigFile(vcConfig, VCstorepath);

		File store = new File(vcPath);
		if (!store.exists()) {
			System.out.println("Store does not exist, creating at :" + vcPath);
			log.debug("Store does not exist, creating at {}", vcPath);
			store.mkdirs();
		}

	}

	// --> Creation of VC and Store in local store.
	@Override
	public void createVC(String trustFrameworkName, String type)
			throws PropertiesAccessException, IOException, DecoderException, GeneralSecurityException, JsonLDException {
		
		setVCstorePath();
		String vcWithProofString = "";

		// Create payload.
		JsonObject payload = creatPayload(trustFrameworkName, type);
		log.debug("vcsignertype: {} ", vcsigner_type);
		if (vcsigner_type.equals(VcSignHandler.SET_SIGNER_INTERNAL)) {
		// Creation of the proof.
			String verificationMethod = trustList_Issuer +  HASH + vcsigner_key;
			vcWithProofString = oProofSigner
					.creatLDProofString(payload.toString(), verificationMethod, SET_SIGNATURE_PURPOSE).toString();
			log.debug("vcgen: {} ", verificationMethod);
			
		}
		// Creation of Proof with External Signer
		else if (vcsigner_type.equals(VcSignHandler.SET_SIGNER_TSA)) {
			vcWithProofString = oProofSigner.TSASigner(payload);
			log.debug("vcgen: {} ", vcWithProofString);
			
			
		}

		// Store this VC in local store.
		VCUtil.vcStore(vcPath, vcWithProofString, trustFrameworkName);

	}

	//--> Method for delete VC from local store.
	@Override
	public void deleteVC(String frameworkName) {
		setVCstorePath();
		File vCFile = TSPAUtil.FindFileFromPath(vcPath, frameworkName);

		if (!(vCFile == null)) {
			log.info("Delete; Verifiable Credential for {} ", frameworkName);
			vCFile.delete();
		}

	}

	//--> Method for fetching Well-known Configuration from store.
	@Override
	public String getWellKnown() throws FileEmptyException, PropertiesAccessException, IOException {
		
		// Set the properties file.
		//String wellKnownPathString = TSPAUtil.getPropertieStringFromConfigFile(mConfig, WELL_KNOWN_STORE_PATH_STRING);

		File wellKnownFile = new File(wkPath + "/" + "did-configuration.json");

		String wKString = new String(Files.readAllBytes(wellKnownFile.toPath()));

		if (wKString == null || wKString.isEmpty()) {
			log.error("Well-knownConfiguration; did-configuration file is Empty!!!");
			throw new FileEmptyException("did-configuration.json");
		}

		return wKString;

	}

	//--> Method for fetching VC from store based on Trustframework name.  
	@Override
	public String getVCforTrustList(String frameworkName) throws FileEmptyException, IOException {
		
		setVCstorePath();
		File vcFile = TSPAUtil.FindFileFromPath(vcPath, frameworkName);

		if (vcFile != null) {
			String vcAsString = new String(Files.readAllBytes(vcFile.toPath()));
			if (vcAsString == null || vcAsString.isEmpty()) {
				log.error("VC for Trustlist '{}' is Empty!!!", frameworkName);
				throw new FileEmptyException("Verifiable Credential for " + frameworkName);
			}
			return vcAsString;
		} else {
			log.warn("VC for '{}' not available in store!", frameworkName);
			throw new FileNotFoundException("VC for "+frameworkName +" not available in store!");
		}
	}

	//--> Load template from resource and preparing payload. 
	private JsonObject creatPayload(String trustFrameworkName, String type)
			throws PropertiesAccessException, IOException {

		//Read the file from resource and concert in to json.
		JsonObject PayloadJsonObject = VCUtil.resource_to_JsonObject(vcResource);
	
		PayloadJsonObject.remove("proof");  // remove dummy proof

		String issuancedateString = null;
		String expirationDateString = null;

		//Set issuer from the VCConfig.properties
		//String issuer = trustList_Issuer;
		String id = trustList_Issuer + HASH + ISSUER_LISTS;

		log.debug("Trust-list Issuer, Issuer: {}", trustList_Issuer);
		log.debug("Trust-List, Id: {},", id);
		PayloadJsonObject.addProperty("issuer", trustList_Issuer);
		PayloadJsonObject.addProperty("id", id);

		//Date and time formating & set the time zone
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		TimeZone timeZone = TimeZone.getTimeZone("CET");
		dateFormat.setTimeZone(timeZone);
		Date issuanceDate = new Date();
		issuancedateString = dateFormat.format(issuanceDate);
		

		
		PayloadJsonObject.addProperty("issuanceDate", issuancedateString);
		

		//Credential subject
		JsonObject credentialSub = PayloadJsonObject.getAsJsonObject("credentialSubject");

		if (type != null && type.compareTo("xml") == 0) {
			credentialSub.addProperty("trustlisttype", "XML based Trust-lists");
		} else if (type != null && type.compareTo("json") == 0) {
			credentialSub.addProperty("trustlisttype", "JSON based Trust-lists");
		}
		
		//Preparing endpoint for the Get Trust-list
		String serviceEndpoint = endpointTrustlist(trustFrameworkName);
		credentialSub.addProperty("trustlistURI", serviceEndpoint);

		//Read Trust-list from Get request.
		log.debug("Attempting to get Trust list from url: {}", serviceEndpoint);
		byte[] trustlistAsByte = VCUtil.readBytefromURL(serviceEndpoint);
		

		//String algoName = TSPAUtil.getPropertieStringFromConfigFile(vcConfig, VC_TRUSTLIST__HASHING_ALGO);
		
		//Create hashing for the trust-list
		String hashOfTrustlist = Hashingalgo.TrustlistHashing(trustlistAsByte, vcHashingAlgo);
		credentialSub.addProperty("hash", hashOfTrustlist);
		
		PayloadJsonObject.add("credentialSubject", credentialSub);

		return PayloadJsonObject;

	}
	
	private String endpointTrustlist(String framworkname) {
		return requestMappingString + framworkname + TRUST_LIST_ROUTE;
	}


}
