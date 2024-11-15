package eu.xfsc.train.tspa.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.xfsc.train.tspa.exceptions.FileEmptyException;
import eu.xfsc.train.tspa.exceptions.PropertiesAccessException;
import eu.xfsc.train.tspa.interfaces.IVCService;

@RestController
public class WellKnownController {

	private static final Logger log = LoggerFactory.getLogger(WellKnownController.class);

	@Autowired
	public IVCService mVCHandler;
	
	/**
	 *--> Get request for fetching Well-known. 
	 */
	@GetMapping(value = "/.well-known/did-configuration.json", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> getWellKnownVC() throws PropertiesAccessException {
		log.debug("---------------- Getting Well-known DID configuration from the Store-----------------");

		String wellKnownVC = null;

		try {
			wellKnownVC = mVCHandler.getWellKnown();
			return new ResponseEntity<>(wellKnownVC, HttpStatus.OK);
		} catch (FileEmptyException | IOException e) {
			log.error("Failed!, fetching the Well-known configuration because:", e);
			return new ResponseEntity<>("Failed to fetch Well-known", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
