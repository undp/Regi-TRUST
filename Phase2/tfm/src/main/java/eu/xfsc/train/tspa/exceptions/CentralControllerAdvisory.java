package eu.xfsc.train.tspa.exceptions;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Central exception handler.
 */
@ControllerAdvice
public class CentralControllerAdvisory extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(CentralControllerAdvisory.class);

	@ExceptionHandler({ PropertiesAccessException.class })
	protected ResponseEntity<Object> handleConfigurationException(PropertiesAccessException ex) {
		log.error("PropertiesAccessException, Configuration error : {}", ex.getMessage());
		return new ResponseEntity<>(Map.of("status", HttpStatus.PRECONDITION_FAILED.value(), "error",
				"Configuration error : " + ex.getMessage()), HttpStatus.PRECONDITION_FAILED);
	}

	@ExceptionHandler({ FileEmptyException.class })
	protected ResponseEntity<Object> handleEmptyFileException(FileEmptyException ex) {

		log.error("FileEmptyException, File empty error : {}", ex.getMessage());
		return new ResponseEntity<>(
				Map.of("status", HttpStatus.NOT_FOUND.value(), "error", "File empty error : " + ex.getMessage()),
				HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler({ FileExistsException.class })
	protected ResponseEntity<Object> handleFileExistsException(FileExistsException ex) {

		log.error("FileExistsException, File Exist error :", ex.getMessage());
		return new ResponseEntity<>(
				Map.of("status", HttpStatus.BAD_REQUEST.value(), "error", "File already exist  : " + ex.getMessage()),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({ TSPException.class })
	protected ResponseEntity<Object> handleTSPException(TSPException ex) {
		log.error("TSPException, {}", ex.getMessage());
		return new ResponseEntity<>(Map.of("status", HttpStatus.BAD_REQUEST.value(), "error", ex.getMessage()),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({ InvalidStatusCodeException.class })
	protected ResponseEntity<Object> handleZMException(InvalidStatusCodeException ex) {
		log.error("InvalidStatusCodeException, {}", ex.getMessage());
		return new ResponseEntity<>(
				Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.value(), "error", ex.getCustomErrorMessage()),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler({ InvalidBearerTokenException.class })
	protected ResponseEntity<Object> handleAuthExceptions(InvalidBearerTokenException ex) {

		log.error("InvalidBearerTokenException, {}", ex.getMessage());
		return new ResponseEntity<>(Map.of("status", HttpStatus.UNAUTHORIZED.value(), "error", ex.getMessage()),
				HttpStatus.UNAUTHORIZED);
	}

}
