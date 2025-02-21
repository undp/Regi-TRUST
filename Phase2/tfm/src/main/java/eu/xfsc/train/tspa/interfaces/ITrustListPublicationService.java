package eu.xfsc.train.tspa.interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.networknt.schema.ValidationMessage;

import eu.xfsc.train.tspa.exceptions.FileEmptyException;
import eu.xfsc.train.tspa.exceptions.FileExistsException;
import eu.xfsc.train.tspa.exceptions.PropertiesAccessException;
import jakarta.xml.bind.JAXBException;

public interface ITrustListPublicationService {
	
	
	public List<SAXParseException> isXMLValid(String xmlData,Resource schema) throws SAXException, IOException;

	public Set<ValidationMessage> isJSONValid(String jsonData,Resource schema) throws FileNotFoundException, IOException;

	public String initXMLTrustList(String frameworkName, String xml)
			throws FileExistsException, FileNotFoundException, PropertiesAccessException, JAXBException, FileEmptyException, IOException;
	
	public void initJsonTrustList(String frameworkName, String xml)
			throws FileExistsException, FileNotFoundException, PropertiesAccessException;
			
	public String deleteTrustlist(String framworkname) throws IOException;
	
	//TSP
	// public void tspPublish(String frameworkName, String tspJson) throws FileEmptyException, PropertiesAccessException, IOException, JAXBException;
	
	// public void tspRemove(String frameworkName, String uuid) throws FileEmptyException, PropertiesAccessException, IOException, JAXBException;
	
	// public void tspUpdate(String frameworkName, String uuid, String tspJson) throws FileEmptyException, PropertiesAccessException, IOException, JAXBException;
	
	// From here, implemented methods for Regitrust.

	// TRUST LIST
	public String updateFrameworkInformation(String frameworkName, String FrameworkInformation) throws FileEmptyException, PropertiesAccessException, IOException, JAXBException;
	public String getFullXMLTrustlist(String frameworkName, String version) throws IOException, JAXBException;
	public String getSimplifiedTLfromDB(String frameworkName, String version) throws IOException, FileEmptyException, PropertiesAccessException;
	public String getTrustListVersions(String frameworkName) throws IOException;
	public String addTSPToTrustList(String frameworkName, String tspJson) throws FileEmptyException, PropertiesAccessException, IOException;

	// TSP
	public String getTSPVersions(String frameworkName, String tspId) throws IOException;
	public String test(String data) throws JsonMappingException, JsonProcessingException;
	public String getSingleTSP(String frameworkName, String tspId, String version) throws FileEmptyException, IOException;
	public String updateTSP(String frameworkName, String tspId, String tspJson) throws FileEmptyException, PropertiesAccessException, IOException;
}
