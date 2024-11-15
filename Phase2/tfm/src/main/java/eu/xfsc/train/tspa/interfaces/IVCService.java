package eu.xfsc.train.tspa.interfaces;

import java.io.IOException;
import java.security.GeneralSecurityException;
import org.apache.commons.codec.DecoderException;
import eu.xfsc.train.tspa.exceptions.FileEmptyException;
import eu.xfsc.train.tspa.exceptions.PropertiesAccessException;
import foundation.identity.jsonld.JsonLDException;

public interface IVCService {
	public String getWellKnown() throws FileEmptyException,PropertiesAccessException, IOException;

	public void createVC(String frameworkName,  String type) throws PropertiesAccessException, IOException, DecoderException, GeneralSecurityException, JsonLDException;
	
	//public void setVCstorePath(String VCstorepath) throws PropertiesAccessException;
	
	public void deleteVC(String frameworkName);

	public String getVCforTrustList(String frameworkName) throws IOException, FileEmptyException;

}
