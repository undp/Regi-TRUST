package eu.xfsc.train.tspa.interfaces;

import java.io.IOException;
import eu.xfsc.train.tspa.exceptions.PropertiesAccessException;

public interface IPublicationService {

	public void storeTrustService(String trustListName, String trustListData) throws IOException;

	void setPath(String propertieString) throws PropertiesAccessException;

	public void deleteTrustService(String trustListName) throws IOException;
	
}
