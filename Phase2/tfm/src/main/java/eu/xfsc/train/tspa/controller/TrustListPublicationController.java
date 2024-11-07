package eu.xfsc.train.tspa.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.networknt.schema.ValidationMessage;

import eu.xfsc.train.tspa.exceptions.FileEmptyException;
import eu.xfsc.train.tspa.exceptions.FileExistsException;
import eu.xfsc.train.tspa.exceptions.PropertiesAccessException;
import eu.xfsc.train.tspa.exceptions.TSPException;
import eu.xfsc.train.tspa.interfaces.ITrustListPublicationService;
import eu.xfsc.train.tspa.interfaces.IVCService;
import eu.xfsc.train.tspa.utils.TSPAUtil;
import foundation.identity.jsonld.JsonLDException;
import jakarta.xml.bind.JAXBException;

@RestController
@RequestMapping("/ttfm/api/v1")
public class TrustListPublicationController {

	private static final Logger log = LoggerFactory.getLogger(TrustListPublicationController.class);

	@Autowired
	private ITrustListPublicationService iTrustListPublicationService;

	@Autowired
	private IVCService ivcService;
	@Value("classpath:templates/validation-trustlist.xsd")
	private Resource xsdResource;
	@Value("classpath:templates/Trustlist-Schema.json")
	private Resource jsonSchemaResource;
	@Value("classpath:templates/TSPSchema.json")
	private Resource tspSchemaResource;
	@Value("classpath:templates/trust-list.xml")
	private Resource trustListTemplate;
	

	// TRUST LISTS ------------------------------------------------------------------------------------------------

	/* Test POST ednpoint. Recieves a JSON and returns it.	 */
	@PostMapping("/test")
	public ResponseEntity<String> test(@RequestBody String jsonData) throws JsonMappingException, JsonProcessingException {
		String result = iTrustListPublicationService.test(jsonData);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}



	/**
	 * --> Publish (create and store) an initial trustlist in XML format. The Trustlist XML is taken from resource template.
	 * @throws JAXBException 
	 * @throws FileEmptyException 
	 */
	@PostMapping(value = "/regitrust/trustlist/{framework-name}", consumes = MediaType.APPLICATION_JSON_VALUE)
	// @PreAuthorize("hasAuthority('enrolltf')")
	public ResponseEntity<Object> createTrustListXML(@PathVariable("framework-name") String frameworkName,
			@RequestBody String schemesObject) throws PropertiesAccessException, FileExistsException, FileEmptyException, JAXBException {

		log.debug("debug--------------- PUBLISH TRUSTLIST (from XML template) ---------------");
		log.debug("schemes received: {}", schemesObject);

		List<SAXParseException> errorList = null;

		try {
			String trustListStr = trustListTemplate.getFilename();
			trustListStr = new String(trustListTemplate.getInputStream().readAllBytes());
			errorList = iTrustListPublicationService.isXMLValid(trustListStr, xsdResource);
			if (errorList.isEmpty()) {
				log.debug("Successfully Validated!!!");
				iTrustListPublicationService.initXMLTrustList(frameworkName, trustListStr);
				return TSPAUtil.getResponseBody("Trust-list initially created and stored in XML format",
						HttpStatus.CREATED);

			} else {
				log.error("Validation failed");
				String errorString=errorList.stream()
			              .map(SAXParseException::toString)
			              .collect(Collectors.joining("\r\n", "XML validation failed:\r\n", ""));
				return new ResponseEntity<>(errorString, HttpStatus.BAD_REQUEST);
			}

		} catch (IOException | SAXException e) {
			log.error("Failed to initiate trust-list creation via xml format because:", e);
			return TSPAUtil.getResponseBody("Failed to initiate trust-list creation via xml format.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * --> Updates a trustlist's scheme information only'. Triggers a new version of the trustlist.
	 * @throws JAXBException 
	 * @throws FileEmptyException 
	 */
	@PutMapping(value = "/regitrust/trustlist/{framework-name}", consumes = MediaType.APPLICATION_JSON_VALUE)
	// @PreAuthorize("hasAuthority('enrolltf')")
	// TO DO: expect a json body with the scheme information to update, validate it.
	public ResponseEntity<Object> updateSchemeInformationInTrustList(@PathVariable("framework-name") String frameworkName,
			@RequestBody String schemesObject) throws PropertiesAccessException, FileExistsException, FileEmptyException, JAXBException {

		log.debug("debug--------------- UPDATE SCHEME INFORMATION IN TRUSTLIST ---------------");

		// List<SAXParseException> errorList = null;

		try {
			// TO DO: validate json body against JSON schema
			// Boolean isValid = iTrustListPublicationService.isJSONValid(schemesObject, jsonSchemaResource);
			Boolean isValid = true;
			if (isValid) {
				log.debug("Successfully Validated!!!");
				String newVersion = iTrustListPublicationService.updateTLversion(frameworkName);
				return TSPAUtil.getResponseBody("Trustlist version updated to " + newVersion,
						HttpStatus.CREATED);

			} else {
				log.error("Validation failed");
				// String errorString=errorList.stream()
			    //           .map(SAXParseException::toString)
			    //           .collect(Collectors.joining("\r\n", "XML validation failed:\r\n", ""));
				return new ResponseEntity<>("errorString", HttpStatus.BAD_REQUEST);
			}

		} catch (IOException e) {
			log.error("Failed to update trustlist: ", e);
			return TSPAUtil.getResponseBody("Failed to update trustlist. A trustlist for this framework was not found",
					HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * --> Fetches a simmplified trustlist
	 * @throws JAXBException 
	 * @throws FileEmptyException 
	 * @throws IllegalArgumentException 
	 */
	@GetMapping(value = "/regitrust/trustlist/{framework-name}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getSimplifiedTrustList(
	        @PathVariable("framework-name") String frameworkName,
	        @RequestParam(value = "version", required = false) String version) {
	    log.debug("debug--------------- GET SIMPLIFIED TRUSTLIST ---------------");
	    log.debug("Requested framework name: {}, version: {}", frameworkName, version);

	    try {
			if (version != null && !version.matches("\\d+")) {
				throw new IllegalArgumentException("Version must be a number.");
			}
			String trustList = iTrustListPublicationService.getSimplifiedTLfromDB(frameworkName, version);
	        return ResponseEntity.ok(trustList);
	    } catch (FileEmptyException e) {
	        log.error("Failed to fetch simplified trustlist: ", e);
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(e.getMessage());
	    } catch (IOException e) {
	        log.error("Failed to fetch simplified trustlist: ", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Failed to fetch simplified trustlist.");
		} catch (IllegalArgumentException e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(e.getMessage());
	    }
	}

	/**
	 * --> Fecth a full trustlist in XML format. Supports versioning.
	 * @param frameworkName
	 * @param trustlist
	 * @return
	 * @throws PropertiesAccessException
	 * @throws FileExistsException
	 */
	@GetMapping(value = "/regitrust/trustlist/xml/{framework-name}", produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<Object> getTrustListXML(@PathVariable("framework-name") String frameworkName,
			@RequestParam(value = "version", required = false) String version) {
		log.debug("debug--------------- GET TRUSTLIST (XML) ---------------");
		log.debug("Requested framework name: {}, version: {}", frameworkName, version);

		try {
			if (version != null && !version.matches("\\d+")) {
				throw new IllegalArgumentException("Version must be a number.");
			}
			String trustListXML = iTrustListPublicationService.getFullXMLTrustlist(frameworkName, version);
			return ResponseEntity.ok(trustListXML);
		} catch (IllegalArgumentException e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(e.getMessage());
		} catch (FileNotFoundException e) {
			log.error("Failed to fetch trustlist in XML format: ", e);
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("Trustlist for framework " + frameworkName + " not found.");
		} catch (IOException | JAXBException e) {
			log.error("Failed to fetch trustlist in XML format: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to fetch trustlist in XML format. " + e.getMessage());
		}	
	}	

	/*
	 * --> Fetch a list of versions of a trustlist
	 * @param frameworkName
	 * @return
	 * @throws IOException 
	 */
	@GetMapping(value = "/regitrust/trustlist/history/{framework-name}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getTrustListVersions(@PathVariable("framework-name") String frameworkName) {
		log.debug("--------------- GET TRUSTLIST VERSIONS ---------------");
		try {
			String trustListVersions = iTrustListPublicationService.getTrustListVersions(frameworkName);
			return ResponseEntity.ok(trustListVersions);
		} catch (IOException e) {
			log.error("Failed to fetch trustlist versions: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch trustlist versions. " + e.getMessage());
		}
	}

	// TRUST SERVICE PROVIDERS ------------------------------------------------------------------------------------------------
	/* --> Add TSP to Trustlist
	 * @param frameworkName
	 */
	@PostMapping(value = "/regitrust/tsp/{framework-name}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> addTSPToTrustList(@PathVariable("framework-name") String frameworkName,
			@RequestBody String tspJson) throws FileEmptyException, PropertiesAccessException, TSPException, IOException {
		
			try {
				String addedTSP = iTrustListPublicationService.addTSPToTrustList(frameworkName, tspJson);
				return TSPAUtil.getResponseBody(addedTSP, HttpStatus.CREATED);
			} catch (PropertiesAccessException e) {
				log.error("Failed to add TSP to trustlist: ", e);
				return TSPAUtil.getResponseBody(e.getMessage(), HttpStatus.BAD_REQUEST);
			} catch (IOException e) {
				log.error("Failed to add TSP to trustlist: ", e);
				return TSPAUtil.getResponseBody("Failed to add TSP to trustlist. " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
	}

	// GXFS implementation ------------------------------------------------------------------------------------------------
	/**
	 * --> Publishing initial trustlist by JSON Format. --> Enveloping trustlist in
	 * VC (Creation of VC)
	 */
	@PutMapping(value = "/init/json/{framework-name}/trust-list", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('enrolltf')")
	public ResponseEntity<Object> createTrustListJSON(@PathVariable("framework-name") String frameworkName,
			@RequestBody String trustlist) throws PropertiesAccessException, FileExistsException {

		log.debug("--------------- PUBLISH TRUSTLIST (JSON) ---------------");

		Set<ValidationMessage> errors = null;

		try {
			errors = iTrustListPublicationService.isJSONValid(trustlist, jsonSchemaResource);
			if (errors.isEmpty()) {
				log.debug("Successfully Validated!!!");
				iTrustListPublicationService.initJsonTrustList(frameworkName, trustlist);
				ivcService.createVC(frameworkName, "json");
				return TSPAUtil.getResponseBody("Trust-list initially created and stored in JSON format",
						HttpStatus.CREATED);
			} else {
				log.error("Json validation failed");
				
				String errorString=errors.stream()
	              .map(ValidationMessage::toString)
	              .collect(Collectors.joining("\r\n", "JSON validation failed:\r\n", ""));
				return new ResponseEntity<>(errorString, HttpStatus.BAD_REQUEST);
			}
		} catch (DecoderException | GeneralSecurityException | JsonLDException e) {
			log.error("Failed!; Problem during creation Proof of {} because:", frameworkName, e);
			return TSPAUtil.getResponseBody("VC for the " + frameworkName + " is not generated",
					HttpStatus.FAILED_DEPENDENCY);
		} catch (IOException e) {
			log.error("Failed to initiate trust-list creation via JSON format because:", e);
			return TSPAUtil.getResponseBody("Failed to initiate trust-list creation via json format.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * --> Get request for fetching trustlist.
	 */
	// @GetMapping(value = "/{framework-name}/trust-list")
	// @ResponseBody
	// public ResponseEntity<Object> getTrustList(@PathVariable("framework-name") String frameworkName)
	// 		throws FileEmptyException, PropertiesAccessException {
	// 	log.debug("--------------- GET TRUSTLIST ---------------");

	// 	try {
	// 		String trustList = iTrustListPublicationService.getFullXMLTrustlist(frameworkName);
	// 		String trustListType=TSPAUtil.getContentType(trustList);
	// 		if(trustListType.equals("json")) {
	// 			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(trustList);
	// 		} else if (trustListType.equals("xml")) {
	// 			return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(trustList);
	// 		}
	// 	} catch (FileNotFoundException e) {
	// 		log.error("Failed to fetch the initial Trust-list because:", e);
	// 		return TSPAUtil.getResponseBody("Failed to fetch the initial Trust-list:" + e.getMessage(),
	// 				HttpStatus.NOT_FOUND);
	// 	} catch (IOException e) {
	// 		log.error("Failed to fetch the initial Trust-list because:", e);
	// 		return TSPAUtil.getResponseBody("Failed to fetch the initial Trust-list:" + e.getMessage(),
	// 				HttpStatus.INTERNAL_SERVER_ERROR);
	// 	}
	// 	return new ResponseEntity<>("Unexpected media type: ",HttpStatus.UNSUPPORTED_MEDIA_TYPE);
	// }

	// /**
	//  * --> Delete request for trustlist.
	//  * 
	//  * @throws IOException
	//  */
	// @DeleteMapping(value = "/{framework-name}/trust-list")
	// @PreAuthorize("hasAuthority('enrolltf')")
	// public ResponseEntity<Object> deleteTrustList(@PathVariable("framework-name") String frameworkName)
	// 		throws PropertiesAccessException, IOException {
	// 	log.debug("--------------- DELETE TRUSTLIST ---------------");

	// 	String responseString = iTrustListPublicationService.deleteTrustlist(frameworkName);
	// 	ivcService.deleteVC(frameworkName);
	// 	return TSPAUtil.getResponseBody(responseString, HttpStatus.OK);
	// 	// return new ResponseEntity<>(responseString, HttpStatus.OK);
	// }

	// /**
	//  * --> Get request for fetching VC of trustframework.
	//  */
	// @GetMapping(value = "/{framework-name}/vc/trust-list", produces = MediaType.APPLICATION_JSON_VALUE)
	// @ResponseBody
	// public ResponseEntity<Object> getVC(@PathVariable("framework-name") String frameworkName)
	// 		throws FileEmptyException, PropertiesAccessException {
	// 	log.debug("--------------- GET VC FOR TRUSTLIST '{}' ---------------", frameworkName);

	// 	try {
	// 		String vcAsString = ivcService.getVCforTrustList(frameworkName);
	// 		return new ResponseEntity<>(vcAsString, HttpStatus.OK);
	// 	} catch (FileNotFoundException e) {
	// 		log.error("Failed to fetch the Verifiable Credential for {} because:", frameworkName, e);
	// 		return TSPAUtil.getResponseBody("Failed to fetch the Verifiable Credential : " + e.getMessage(), HttpStatus.NOT_FOUND);
	// 	} catch (IOException e) {
	// 		log.error("Failed to fetch the Verifiable Credential for {} because:", frameworkName, e);
	// 		return TSPAUtil.getResponseBody("Failed to fetch the Verifiable Credential : "+ e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	// 	}
	// }

	
	// /**
	//  * --> Publishing TSP for trustframework.
	//  */
	// @PutMapping(value = "/{framework-name}/trust-list/tsp", consumes = MediaType.APPLICATION_JSON_VALUE)
	// @PreAuthorize("hasAuthority('enrolltf')")
	// public ResponseEntity<Object> createTSP(@PathVariable("framework-name") String frameworkName,
	// 		@RequestBody String tspJson) throws FileEmptyException, PropertiesAccessException, TSPException {
	// 	log.debug("CreateTSP, got :{}", frameworkName);
	// 	Set<ValidationMessage> errors = null;

	// 	try {
	// 		errors=iTrustListPublicationService.isJSONValid(tspJson, tspSchemaResource);
	// 		if (errors.isEmpty()) {
	// 			log.debug("Successfully Validated!!!");
	// 			iTrustListPublicationService.tspPublish(frameworkName, tspJson);
	// 			return TSPAUtil.getResponseBody("TSP published for " + frameworkName + ".", HttpStatus.CREATED);
	// 		} else {
	// 			log.error("TSP validation failed");
	// 			String errorString=errors.stream()
	// 		              .map(ValidationMessage::toString)
	// 		              .collect(Collectors.joining("\r\n", "TSP validation failed:\r\n", ""));
	// 			return new ResponseEntity<>(errorString, HttpStatus.BAD_REQUEST);
	// 		}

	// 	} catch (FileNotFoundException e) {
	// 		log.error("createTSP, Trustlist for {} not found :", frameworkName, e);
	// 		return TSPAUtil.getResponseBody(e.getMessage(), HttpStatus.NOT_FOUND);
	// 	} catch (IOException | JAXBException e) {
	// 		log.error("createTSP, failed to publish TSP:", e);
	// 		return TSPAUtil.getResponseBody(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	// 	}

	// }

	// @PatchMapping(value = "/{framework-name}/trust-list/tsp/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	// @PreAuthorize("hasAuthority('enrolltf')")
	// public ResponseEntity<Object> updateTSP(@PathVariable("framework-name") String frameworkName,
	// 		@PathVariable("id") String uuid, @RequestBody String tspJson)
	// 		throws FileEmptyException, PropertiesAccessException, TSPException {
	// 	log.debug("updateTSP, got :{}", frameworkName);
	// 	Set<ValidationMessage> errors = null;

	// 	try {
	// 		errors=iTrustListPublicationService.isJSONValid(tspJson, tspSchemaResource);
			
	// 		if (errors.isEmpty()) {
	// 			log.debug("TSP validation pass.");
	// 			iTrustListPublicationService.tspUpdate(frameworkName, uuid, tspJson);
	// 			return TSPAUtil.getResponseBody("TSP update for " + frameworkName + " with UUID :" + uuid, HttpStatus.OK);
	// 		} else {
	// 			log.error("TSP validation failed");
	// 			String errorString=errors.stream()
	// 		              .map(ValidationMessage::toString)
	// 		              .collect(Collectors.joining("\r\n", "TSP validation failed:\r\n", ""));
	// 			return new ResponseEntity<>(errorString, HttpStatus.BAD_REQUEST);
	// 		}
	// 	} catch (FileNotFoundException e) {
	// 		log.error("updateTSP, Trustlist for {} not found.", frameworkName, e);
	// 		return TSPAUtil.getResponseBody(e.getMessage(), HttpStatus.NOT_FOUND);
	// 	} catch (IOException | JAXBException e) {
	// 		log.error("updateTSP, failed to update TSP.", e);
	// 		return TSPAUtil.getResponseBody(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	// 	}

	// }

	// @DeleteMapping(value = "/{framework-name}/trust-list/tsp/{id}")
	// @PreAuthorize("hasAuthority('enrolltf')")
	// public ResponseEntity<Object> deleteTSP(@PathVariable("framework-name") String frameworkName,
	// 		@PathVariable("id") String uuid) throws FileEmptyException, PropertiesAccessException, TSPException {
	// 	try {
	// 		iTrustListPublicationService.tspRemove(frameworkName, uuid);
	// 		return TSPAUtil.getResponseBody("TSP removed from " + frameworkName + " for UUID: " + uuid, HttpStatus.OK);
	// 	} catch (FileNotFoundException e) {
	// 		log.error("deleteTSP, Trustlist for {} not found.", frameworkName, e);
	// 		return TSPAUtil.getResponseBody(e.getMessage(), HttpStatus.NOT_FOUND);
	// 	} catch (IOException | JAXBException e) {
	// 		log.error("deleteTSP, failed to delete TSP.", e);
	// 		return TSPAUtil.getResponseBody(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	// 	}
	// }

}
