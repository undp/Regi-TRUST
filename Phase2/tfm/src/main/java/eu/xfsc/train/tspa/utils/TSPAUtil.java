package eu.xfsc.train.tspa.utils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.common.io.Files;

@Component
public class TSPAUtil {

	private static final Logger log = LoggerFactory.getLogger(TSPAUtil.class);

	private static final String XML_STRING = "xml";
	private static final String JSON_STRING = "json";
	//private static final String WELL_KNOWN_ISUUER_STRING = "well-known.issuer";
	//private static final String TRUSTLIST_ISUUER_STRING = "list.trustlist.vc.issuer";
	
	
	
	
	/**
	 * @param mConfig: Desired configuration file object
	 * @param key: Key value from the configuration file
	 * @return value of the key as String.
	 * @throws PropertiesAccessException: throws exception whenever properties are missing in the mConfig file.
	 */
	/*public static String getPropertieStringFromConfigFile(PropertiesConfiguration mConfig, String key)
			throws PropertiesAccessException {

		if (mConfig == null)
			return "file not found";

		String propertiy = mConfig.getString(key);

		if (propertiy == null || propertiy.isEmpty()) {
			log.error("Property  '{}' is missing in '{}' Config file", key, mConfig.getFileName());
			throw new PropertiesAccessException("property " + key + "  is missing in " + mConfig.getFileName());
		}
		if (key.compareTo(WELL_KNOWN_ISUUER_STRING)== 0 || key.compareTo(TRUSTLIST_ISUUER_STRING)==0) {
			if (!propertiy.startsWith("did:")) {
				log.error("Issuer is not DID");
				throw new PropertiesAccessException("Issuer should be DID");
			}
		}
		return propertiy;

	}*/

	/**
	 * --> Find xml and json file from the Store. 
	 * 
	 * @param path : Store path
	 * @param fileName: name of the file you want to find from path(Either ".xml" or ".json" only)
	 * @return file
	 */
	public static File FindFileFromPath(String path, String fileName) {

		File folder = new File(path);
		File[] files = folder.listFiles();

		if (files.length == 0) {
			log.info("Store folder '{}' is Empty.", folder);
			return null;
		}

		for (File f : files) {

			String storedFilenamewithoutExtension = Files.getNameWithoutExtension(f.getName());

			if (f.isFile() && storedFilenamewithoutExtension.compareTo(fileName) == 0) {
				if (getFileExtension(f.getName()) != null) {
					log.info("Found file, it exists!");
					return f;
				}
			}
		}

		log.info("File not found in store {}", folder);
		return null;

	}

	//--> Method for checking the file is exist with same name.
	public static boolean isFileExisting(String mPath, String name) {
		File folder = new File(mPath);
		File[] files = folder.listFiles();

		if (files.length == 0) {
			log.info("Nothing found in store folder: {}", folder);
			return false;
		}

		for (File f : files) {
			if ((f.isFile() && f.getName().compareTo(name + ".xml") == 0)
					|| (f.isFile() && f.getName().compareTo(name + ".json") == 0)) {
				log.info("Found file, it exists!");
				return true;
			}
		}

		return false;
	}

	public static String getFileExtension(String fileName) {
		String extension = Files.getFileExtension(fileName);
		if (extension != null && extension.compareTo(XML_STRING) == 0) {
			return XML_STRING;
		} else if (extension != null && extension.compareTo(JSON_STRING) == 0) {
			return JSON_STRING;
		}
		return null;
	}
	
	public static String getContentType(String content) {
		
		OptionalInt charInt = content.chars().filter(c -> c == '<' || c == '{').findFirst();
		if (charInt.isPresent()) {
			if (charInt.getAsInt() == '<') {
				log.debug("getContentType, trust-list type:{}",XML_STRING);
				return XML_STRING;
			} else if (charInt.getAsInt() == '{') {
				log.debug("getContentType, trust-list type:{}",JSON_STRING);
				return JSON_STRING;
			}
		}
		return null;
	}
	
	public static ResponseEntity<Object> getResponseBody(String message,HttpStatus status){
		return new ResponseEntity<>(Map.of("status",status.value(),"message",message),status);
	}
	
}
