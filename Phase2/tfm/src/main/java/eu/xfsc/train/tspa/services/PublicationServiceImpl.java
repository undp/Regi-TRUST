package eu.xfsc.train.tspa.services;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.accept.AbstractMappingContentNegotiationStrategy;

import eu.xfsc.train.tspa.exceptions.PropertiesAccessException;
import eu.xfsc.train.tspa.interfaces.IPublicationService;
import eu.xfsc.train.tspa.utils.TSPAUtil;

@Service
public class PublicationServiceImpl implements IPublicationService {

	private static final Logger log = LoggerFactory.getLogger(PublicationServiceImpl.class);

	private String mPath = null;
	
	@Value("${storage.path.scheme}")
	private String STORAGE_SCHEME;
	@Value("${storage.path.did}")
	private String STORAGE_DID;

	// Set the path and check if store avability.
	@Override
	public void setPath(String propertiyPath) throws PropertiesAccessException {
		if(propertiyPath.compareTo("scheme")==0) {
			mPath=STORAGE_SCHEME;
		}else if (propertiyPath.compareTo("did")==0) {
			mPath=STORAGE_DID;
		}

		File store = new File(mPath);
		if (!store.exists()) {
			System.out.println("Store does not exist, creating at :" + mPath);
			log.debug("Store does not exist, creating at {}", mPath);
			store.mkdirs();
		}
	}

	// Storing and Updating the Trust service in local temp folder.
	@Override
	public void storeTrustService(String trustFramework, String data) throws IOException {
		log.debug("storeTrustService.start; got framework name:{} and data:{}",trustFramework,data);
		if (isTrustListExisting(trustFramework)) {
			log.debug("Trustframework/DID already exists. UPDATE.");
		} else {
			log.debug("Trustframework/DID doesn't exist. CREATE");
		}

		PrintWriter file = new PrintWriter(mPath + "/" + trustFramework);
		log.debug("Rewrite/update the File '{}' at Path '{}'", trustFramework, mPath);
		file.write(data);
		file.close();
	}

	// Delete the Trust service from the local temp folder.
	@Override
	public void deleteTrustService(String trustListName) throws IOException {
		if (isTrustListExisting(trustListName)) {
			File file = new File(mPath + "/" + trustListName);
			file.delete();
			log.info(" DELETE Trustscheme service '{}' from the folder '{}'", trustListName, mPath);
		} else {
			log.warn("Trustscheme service doesn't exist in the folder");
		}
	}

	// Checking the temp folder for existing service.
	private boolean isTrustListExisting(String name) {
		File folder = new File(mPath);
		File[] files = folder.listFiles();

		if (files.length == 0) {
			log.debug("Nothing found in store folder: {}", folder);
			return false;
		}

		for (File f : files) {
			if (f.isFile() && f.getName().compareTo(name) == 0) {
				log.debug("Found scheme, it exists!");
				return true;
			}
		}

		return false;
	}

}
