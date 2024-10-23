package eu.xfsc.train.tspa.interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.networknt.schema.ValidationMessage;

import eu.xfsc.train.tspa.exceptions.FileEmptyException;
import eu.xfsc.train.tspa.exceptions.FileExistsException;
import eu.xfsc.train.tspa.exceptions.PropertiesAccessException;
import jakarta.xml.bind.JAXBException;

public interface ITrustListPublicationService {
	
	
	public List<SAXParseException> isXMLValid(String xmlData,Resource schema) throws SAXException, IOException;

	public Set<ValidationMessage> isJSONValid(String jsonData,Resource schema) throws FileNotFoundException, IOException;

	public void initXMLTrustList(String frameworkName, String xml)
			throws FileExistsException, FileNotFoundException, PropertiesAccessException;
	
	public void initJsonTrustList(String frameworkName, String xml)
			throws FileExistsException, FileNotFoundException, PropertiesAccessException;

	public String getTrustlist(String frameworkName) throws IOException;

	public String deleteTrustlist(String framworkname) throws IOException;

	//TSP
	public void tspPublish(String frameworkName, String tspJson) throws FileEmptyException, PropertiesAccessException, IOException, JAXBException;

	public void tspRemove(String frameworkName, String uuid) throws FileEmptyException, PropertiesAccessException, IOException, JAXBException;

	public void tspUpdate(String frameworkName, String uuid, String tspJson) throws FileEmptyException, PropertiesAccessException, IOException, JAXBException;

}
