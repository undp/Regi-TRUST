package eu.xfsc.train.tspa.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.xfsc.train.tspa.exceptions.InvalidStatusCodeException;
import eu.xfsc.train.tspa.exceptions.PropertiesAccessException;
import eu.xfsc.train.tspa.interfaces.IPublicationService;
import eu.xfsc.train.tspa.interfaces.IZoneManager;
import eu.xfsc.train.tspa.utilities.AbsDIDMethodUtil;
import eu.xfsc.train.tspa.utils.TSPAUtil;

@RestController
@RequestMapping("tspa/v1")
public class TrustFrameWorkPublishController {

	private static final Logger log = LoggerFactory.getLogger(TrustFrameWorkPublishController.class);

	private static final String TRUSTFRAMEWORK = "scheme";
	private static final String DID = "did";

	@Autowired
	public IZoneManager mZoneManager;
	@Autowired
	public IPublicationService mPublicationManager;
	@Autowired
	AbsDIDMethodUtil mDidMethodsUtil;

	@GetMapping("/hello")
	public ResponseEntity<String> hello() {
		String currentDateTime = java.time.LocalDateTime.now().toString();
		log.info("Current date and time: {}", currentDateTime);
		return new ResponseEntity<>(currentDateTime, HttpStatus.OK);
	}

	/**
	 * --> TSPA endpoint for publishing PTR records in Zone manager.
	 */
	@PutMapping("/trustframework/{framework-name}")
	@PreAuthorize("hasAuthority('enrolltf')")
	public ResponseEntity<Object> publishTrustFramework(@PathVariable("framework-name") String frameworkName, @RequestBody String data) throws InvalidStatusCodeException{

		log.debug("--------------- PUBLISH TRUST FRAMEWORK ---------------");
		

		try {
			buildManager(TRUSTFRAMEWORK);
			mZoneManager.publishTrustSchemes(frameworkName, data);
			mPublicationManager.storeTrustService(frameworkName, data);
			log.debug("Trust-framework published for {}", frameworkName);
			return TSPAUtil.getResponseBody( "Trust-framework created for "+ frameworkName, HttpStatus.CREATED);
		} catch (IOException e) {
			log.error("Failed to publish trust-framework for {} ", frameworkName,  e);
			return TSPAUtil.getResponseBody("Failed to publish trust-framework: "+e.getMessage() , HttpStatus.INTERNAL_SERVER_ERROR);
				
		}
	}

	/**
	 * --> TSPA endpoint for deleting PTR records in Zone manager.
	 */
	@DeleteMapping("/trustframework/{framework-name}")
	@PreAuthorize("hasAuthority('enrolltf')")
	public ResponseEntity<Object> deleteTrustFramework(@PathVariable("framework-name") String frameworkName) throws InvalidStatusCodeException {
		log.debug("--------------- DELETE TRUST FRAMEWORK ---------------");
		
		try {
			buildManager(TRUSTFRAMEWORK);
			mZoneManager.deleteTrustSchemes(frameworkName);
			mPublicationManager.deleteTrustService(frameworkName);
			log.debug("Trust-framework deleted for {}", frameworkName);
			return TSPAUtil.getResponseBody("Trustframework deleted for "+ frameworkName, HttpStatus.OK);
		} catch (IOException e) {
			log.error("Failed to delete Trustframework: {} because: ",frameworkName, e);
			return TSPAUtil.getResponseBody("Failed to delete Trustframework: "+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} 
	}

	/**
	 * --> TSPA endpoint for publishing URI(DID) records in Zone manager.
	 * @throws InvalidStatusCodeException 
	 */
	@PutMapping("/{framework-name}/did")
	@PreAuthorize("hasAuthority('enrolltf')")
	public ResponseEntity<Object> publishDidUri(@PathVariable("framework-name") String trustframework, @RequestBody String data) throws InvalidStatusCodeException {

		log.debug("--------------- PUBLISH DID URI (JSON) ---------------");
		
		
		try {
			JsonObject requestJsonObject = ((JsonObject) JsonParser.parseString(data));
			String didURI = requestJsonObject.get("did").toString().replaceAll("\"", "");
			if (mDidMethodsUtil.isDIDMethodValid(didURI)) {
				log.debug("Passed; valid did method");

				if (didURI.startsWith("did:web:")) {
					// Check for WellKnown Configuration
					if (mDidMethodsUtil.isWellknownValid(didURI)) {
						buildManager(DID);
						mZoneManager.publishDIDUri(trustframework, data);
						mPublicationManager.storeTrustService(trustframework, data);
						log.debug("DID published for {}", trustframework);
					} else {
						log.error("Well-known verification failed !!!");
						return TSPAUtil.getResponseBody("Well-known verification failed.", HttpStatus.BAD_REQUEST);
					}
				} else {
					buildManager(DID);
					mZoneManager.publishDIDUri(trustframework, data);
					mPublicationManager.storeTrustService(trustframework, data);
					log.debug("DID published for {}", trustframework);
				}
			} else {
				log.error("DID Method not supported, expecting methods are defined in the 'application.yml'!");
				return TSPAUtil.getResponseBody("DID Method not supported, expecting methods are defined in the 'application.yml'!", HttpStatus.BAD_REQUEST);
			}
		}  catch (IOException e) {
			log.error("Failed to publish DID URI for the trustframework {} because: {}", trustframework, e);
			return TSPAUtil.getResponseBody("Failed to publish DID URI for the trustframework " + trustframework + " because: "+ e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} 
		return TSPAUtil.getResponseBody("URI(DID) published for "+ trustframework , HttpStatus.CREATED);
	}

	/**
	 * --> TSPA endpoint for deleting URI(DID) records in Zone manager.
	 */
	@DeleteMapping("/{framework-name}/did")
	@PreAuthorize("hasAuthority('enrolltf')")
	public ResponseEntity<Object> deleteTrustListDID(@PathVariable("framework-name") String trustframework) throws InvalidStatusCodeException {
		log.debug("--------------- DELETE DID URI ---------------");
		

		try {
			buildManager(DID);
			mZoneManager.deleteDIDUriRecords(trustframework);
			mPublicationManager.deleteTrustService(trustframework);
			log.debug("DID deleted for {}", trustframework);
		} catch (IOException e) {
			log.error("Failed to delete URI record: {} because: ", trustframework, e);
			return TSPAUtil.getResponseBody("Failed to delete URI record: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} 

		return TSPAUtil.getResponseBody("DID uri record deleted for "+ trustframework, HttpStatus.OK);
	}

	private void buildManager(String proString) {

		try {
			mPublicationManager.setPath(proString);
		} catch (PropertiesAccessException e) {
			e.printStackTrace();
		}
		log.debug("zone and publication manager configured with config.properties");
	}

}
