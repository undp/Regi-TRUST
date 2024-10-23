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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

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
@RequestMapping("tspa/v1")
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

	/**
	 * --> Publishing initial trustlist by XMl Format. --> Enveloping trustlist in
	 * VC (Creation of VC)
	 */
	@PutMapping(value = "/init/xml/{framework-name}/trust-list", consumes = MediaType.APPLICATION_XML_VALUE)
	@PreAuthorize("hasAuthority('enrolltf')")
	public ResponseEntity<Object> createTrustListXML(@PathVariable("framework-name") String frameworkName,
			@RequestBody String trustlist) throws PropertiesAccessException, FileExistsException {

		log.debug("--------------- PUBLISH TRUSTLIST (XML) ---------------");

		List<SAXParseException> errorList = null;

		try {
			errorList = iTrustListPublicationService.isXMLValid(trustlist, xsdResource);
			if (errorList.isEmpty()) {
				log.debug("Successfully Validated!!!");
				iTrustListPublicationService.initXMLTrustList(frameworkName, trustlist);
				ivcService.createVC(frameworkName, "xml");

				return TSPAUtil.getResponseBody("Trust-list initially created and stored in XML format",
						HttpStatus.CREATED);

			} else {
				log.error("Validation failed");
				String errorString=errorList.stream()
			              .map(SAXParseException::toString)
			              .collect(Collectors.joining("\r\n", "XML validation failed:\r\n", ""));
				return new ResponseEntity<>(errorString, HttpStatus.BAD_REQUEST);
			}
		} catch (DecoderException | GeneralSecurityException | JsonLDException e) {	
			log.error("Failed!; Problem during creation Proof of {} ", frameworkName,e);
			return TSPAUtil.getResponseBody("VC for the " + frameworkName + " is not generated",
					HttpStatus.FAILED_DEPENDENCY);

		} catch (IOException | SAXException e) {
			log.error("Failed to initiate trust-list creation via xml format because:", e);
			return TSPAUtil.getResponseBody("Failed to initiate trust-list creation via xml format.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

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
	@GetMapping(value = "/{framework-name}/trust-list")
	@ResponseBody
	public ResponseEntity<Object> getTrustList(@PathVariable("framework-name") String frameworkName)
			throws FileEmptyException, PropertiesAccessException {
		log.debug("--------------- GET TRUSTLIST ---------------");

		try {
			String trustList = iTrustListPublicationService.getTrustlist(frameworkName);
			String trustListType=TSPAUtil.getContentType(trustList);
			if(trustListType.equals("json")) {
				return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(trustList);
			} else if (trustListType.equals("xml")) {
				return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(trustList);
			}
		} catch (FileNotFoundException e) {
			log.error("Failed to fetch the initial Trust-list because:", e);
			return TSPAUtil.getResponseBody("Failed to fetch the initial Trust-list:" + e.getMessage(),
					HttpStatus.NOT_FOUND);
		} catch (IOException e) {
			log.error("Failed to fetch the initial Trust-list because:", e);
			return TSPAUtil.getResponseBody("Failed to fetch the initial Trust-list:" + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>("Unexpected media type: ",HttpStatus.UNSUPPORTED_MEDIA_TYPE);
	}

	/**
	 * --> Delete request for trustlist.
	 * 
	 * @throws IOException
	 */
	@DeleteMapping(value = "/{framework-name}/trust-list")
	@PreAuthorize("hasAuthority('enrolltf')")
	public ResponseEntity<Object> deleteTrustList(@PathVariable("framework-name") String frameworkName)
			throws PropertiesAccessException, IOException {
		log.debug("--------------- DELETE TRUSTLIST ---------------");

		String responseString = iTrustListPublicationService.deleteTrustlist(frameworkName);
		ivcService.deleteVC(frameworkName);
		return TSPAUtil.getResponseBody(responseString, HttpStatus.OK);
		// return new ResponseEntity<>(responseString, HttpStatus.OK);
	}

	/**
	 * --> Get request for fetching VC of trustframework.
	 */
	@GetMapping(value = "/{framework-name}/vc/trust-list", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Object> getVC(@PathVariable("framework-name") String frameworkName)
			throws FileEmptyException, PropertiesAccessException {
		log.debug("--------------- GET VC FOR TRUSTLIST '{}' ---------------", frameworkName);

		try {
			String vcAsString = ivcService.getVCforTrustList(frameworkName);
			return new ResponseEntity<>(vcAsString, HttpStatus.OK);
		} catch (FileNotFoundException e) {
			log.error("Failed to fetch the Verifiable Credential for {} because:", frameworkName, e);
			return TSPAUtil.getResponseBody("Failed to fetch the Verifiable Credential : " + e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (IOException e) {
			log.error("Failed to fetch the Verifiable Credential for {} because:", frameworkName, e);
			return TSPAUtil.getResponseBody("Failed to fetch the Verifiable Credential : "+ e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	
	/**
	 * --> Publishing TSP for trustframework.
	 */
	@PutMapping(value = "/{framework-name}/trust-list/tsp", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('enrolltf')")
	public ResponseEntity<Object> createTSP(@PathVariable("framework-name") String frameworkName,
			@RequestBody String tspJson) throws FileEmptyException, PropertiesAccessException, TSPException {
		log.debug("CreateTSP, got :{}", frameworkName);
		Set<ValidationMessage> errors = null;

		try {
			errors=iTrustListPublicationService.isJSONValid(tspJson, tspSchemaResource);
			if (errors.isEmpty()) {
				log.debug("Successfully Validated!!!");
				iTrustListPublicationService.tspPublish(frameworkName, tspJson);
				return TSPAUtil.getResponseBody("TSP published for " + frameworkName + ".", HttpStatus.CREATED);
			} else {
				log.error("TSP validation failed");
				String errorString=errors.stream()
			              .map(ValidationMessage::toString)
			              .collect(Collectors.joining("\r\n", "TSP validation failed:\r\n", ""));
				return new ResponseEntity<>(errorString, HttpStatus.BAD_REQUEST);
			}

		} catch (FileNotFoundException e) {
			log.error("createTSP, Trustlist for {} not found :", frameworkName, e);
			return TSPAUtil.getResponseBody(e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (IOException | JAXBException e) {
			log.error("createTSP, failed to publish TSP:", e);
			return TSPAUtil.getResponseBody(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PatchMapping(value = "/{framework-name}/trust-list/tsp/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('enrolltf')")
	public ResponseEntity<Object> updateTSP(@PathVariable("framework-name") String frameworkName,
			@PathVariable("id") String uuid, @RequestBody String tspJson)
			throws FileEmptyException, PropertiesAccessException, TSPException {
		log.debug("updateTSP, got :{}", frameworkName);
		Set<ValidationMessage> errors = null;

		try {
			errors=iTrustListPublicationService.isJSONValid(tspJson, tspSchemaResource);
			
			if (errors.isEmpty()) {
				log.debug("TSP validation pass.");
				iTrustListPublicationService.tspUpdate(frameworkName, uuid, tspJson);
				return TSPAUtil.getResponseBody("TSP update for " + frameworkName + " with UUID :" + uuid, HttpStatus.OK);
			} else {
				log.error("TSP validation failed");
				String errorString=errors.stream()
			              .map(ValidationMessage::toString)
			              .collect(Collectors.joining("\r\n", "TSP validation failed:\r\n", ""));
				return new ResponseEntity<>(errorString, HttpStatus.BAD_REQUEST);
			}
		} catch (FileNotFoundException e) {
			log.error("updateTSP, Trustlist for {} not found.", frameworkName, e);
			return TSPAUtil.getResponseBody(e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (IOException | JAXBException e) {
			log.error("updateTSP, failed to update TSP.", e);
			return TSPAUtil.getResponseBody(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@DeleteMapping(value = "/{framework-name}/trust-list/tsp/{id}")
	@PreAuthorize("hasAuthority('enrolltf')")
	public ResponseEntity<Object> deleteTSP(@PathVariable("framework-name") String frameworkName,
			@PathVariable("id") String uuid) throws FileEmptyException, PropertiesAccessException, TSPException {
		try {
			iTrustListPublicationService.tspRemove(frameworkName, uuid);
			return TSPAUtil.getResponseBody("TSP removed from " + frameworkName + " for UUID: " + uuid, HttpStatus.OK);
		} catch (FileNotFoundException e) {
			log.error("deleteTSP, Trustlist for {} not found.", frameworkName, e);
			return TSPAUtil.getResponseBody(e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (IOException | JAXBException e) {
			log.error("deleteTSP, failed to delete TSP.", e);
			return TSPAUtil.getResponseBody(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
