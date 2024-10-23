package eu.xfsc.train.tspa.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.codec.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import eu.xfsc.train.tspa.exceptions.FileEmptyException;
import eu.xfsc.train.tspa.exceptions.FileExistsException;
import eu.xfsc.train.tspa.exceptions.PropertiesAccessException;
import eu.xfsc.train.tspa.exceptions.TSPException;
import eu.xfsc.train.tspa.exceptions.XmlValidationError;
import eu.xfsc.train.tspa.interfaces.ITrustListPublicationService;
import eu.xfsc.train.tspa.interfaces.IVCService;
import eu.xfsc.train.tspa.model.trustlist.TrustServiceStatusList;
import eu.xfsc.train.tspa.model.trustlist.tsp.TSPCustomType;
import eu.xfsc.train.tspa.model.trustlist.tsp.TrustServiceProviderListCustomType;
import eu.xfsc.train.tspa.utils.IpfsUtil;
import eu.xfsc.train.tspa.utils.TSPAUtil;
import foundation.identity.jsonld.JsonLDException;
import io.ipfs.api.IPFS;
import io.ipfs.api.NamedStreamable;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

@Service
public class TrustListPublicationServiceImpl implements ITrustListPublicationService {

	private static final Logger log = LoggerFactory.getLogger(TrustListPublicationServiceImpl.class);

	@Autowired
	ObjectMapper omTrustList;
	@Autowired
	JAXBContext jaxbContext;
	@Autowired
	IVCService ivcService;

	
	@Value("${tspa.ipfs.rcp.api}")
	private String api;
	@Value("${storage.type.trustlist}")
	private String storeType;
	@Value("${storage.path.trustlist}")
	private String mPath;

	private IPFS ipfs;

	@Override
	public void initXMLTrustList(String frameworkName, String xmlData)
			throws FileExistsException, FileNotFoundException, PropertiesAccessException {

		log.debug("Stored Type: {}", storeType);

		if (storeType.compareTo("IPFS") == 0) {
			storenewTrustListXMLIPFS(frameworkName, xmlData);
		} else if (storeType.compareTo("INTERNAL") == 0) {
			storeTrustListXMLLocal(frameworkName, xmlData);
		}
	}

	@Override
	public void initJsonTrustList(String frameworkName, String jsonData)
			throws FileExistsException, FileNotFoundException, PropertiesAccessException {

		log.debug("Stored Type: {}", storeType);

		if (storeType.compareTo("IPFS") == 0) {
			storenewTrustListJSONIPFS(frameworkName, jsonData);
		} else if (storeType.compareTo("INTERNAL") == 0) {
			storeTrustListJSONLocal(frameworkName, jsonData);
		}
	}

	@Override
	public String getTrustlist(String frameworkName) throws IOException {

		log.debug("Stored Type: {}", storeType);

		String resultTL = null;

		if (storeType.compareTo("IPFS") == 0) {
			resultTL = getTrustlistFromIPFS(frameworkName);
		} else if (storeType.compareTo("INTERNAL") == 0) {
			resultTL = getTrustlistFromLocalStore(frameworkName);
		}

		return resultTL;
	}

	@Override
	public String deleteTrustlist(String framworkname) throws IOException {

		log.debug("Stored Type: {}", storeType);

		String result = null;

		if (storeType.compareTo("IPFS") == 0) {
			result = deleteTLfromIPFS(framworkname);
		} else if (storeType.compareTo("INTERNAL") == 0) {
			result = deleteTLfromLocalStore(framworkname);
		}

		return result;
	}

	@Override
	public void tspPublish(String frameworkName, String newTsps)
			throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {

		log.debug("Stored Type: {}", storeType);

		if (storeType.compareTo("IPFS") == 0) {
			tspPublishIPFS(frameworkName, newTsps);
		} else if (storeType.compareTo("INTERNAL") == 0) {
			tspPublishLocalStore(frameworkName, newTsps);

		}
	}

	@Override
	public void tspUpdate(String frameworkName, String uuid, String tsp)
			throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {

		log.debug("Stored Type: {}", storeType);

		if (storeType.compareTo("IPFS") == 0) {
			tspUpdationIPFS(frameworkName, uuid, tsp);
		} else if (storeType.compareTo("INTERNAL") == 0) {
			tspUpdationLocalStore(frameworkName, uuid, tsp);

		}
	}

	@Override
	public void tspRemove(String frameworkName, String uuid)
			throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {
		log.debug("Stored Type: {}", storeType);

		if (storeType.compareTo("IPFS") == 0) {
			tspDeleteIPFS(frameworkName, uuid);
		} else if (storeType.compareTo("INTERNAL") == 0) {
			tspDeleteLocalStore(frameworkName, uuid);

		}

	}

	// --> Method for XML validation.
	@Override
	public List<SAXParseException> isXMLValid(String validationData, Resource schemaXsd)
			throws SAXException, IOException {
		XmlValidationError xsdErrorHandler = new XmlValidationError();
		Validator validator = initValidator(schemaXsd);
		validator.setErrorHandler(xsdErrorHandler);
		try {
			validator.validate(new StreamSource(new java.io.StringReader(validationData)));

		} catch (SAXParseException e) {
			log.error("Validation falied !!");
			e.getStackTrace();
		}
		xsdErrorHandler.getExceptions().forEach(e -> log.error(String.format("Line number: %s, Column number: %s. %s",
				e.getLineNumber(), e.getColumnNumber(), e.getMessage())));
		return xsdErrorHandler.getExceptions();
	}

	// --> Method for JSON validation.
	@Override
	public Set<ValidationMessage> isJSONValid(String jsonData, Resource schemajson)
			throws FileNotFoundException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Set<ValidationMessage> errorSet = null;

		JsonSchema schema = getJSONschema(schemajson);
		JsonNode dataJsonNode = mapper.readTree(jsonData);

		errorSet = schema.validate(dataJsonNode);

		errorSet.forEach(e -> log.error(e.getMessage()));
		return errorSet;
	}

	// --> Method for set location path and creating directory
	private void setPropertiesRule() throws PropertiesAccessException {
		log.debug("Local store; Configuration.");

		File store = new File(mPath);
		if (!store.exists()) {
			System.out.println("Store does not exist, creating at :" + mPath);
			log.debug("Store does not exist, creating at {}", mPath);
			store.mkdirs();
		}
	}

	// --> Private Method for set the configuration of the IPFS
	private void setDirectoryIPFS() throws PropertiesAccessException, IOException {
		log.debug("IPFS store; Configuration.");

		api = api.trim();
		if (api == null || api.isEmpty()) {
			log.error("Property  'tspa.ipfs.rcp.api' is missing in 'application.properties' Config file");
			throw new PropertiesAccessException("property 'tspa.ipfs.rcp.api'  is missing in application.properties");
		}
		log.debug("IPFS, RCP API Server listening on: {}", api);

		ipfs = IpfsUtil.getInstance(api).ipfs; // Setup Singleton Object of the IPFS host;

		log.debug("Path; IPFS Store path :{}", mPath);

		ipfs.files.mkdir(mPath, true); // Create directory if not exist.
	}

	// --> Method for store XML trust-list in local store.
	private void storeTrustListXMLLocal(String frameworkname, String xmlTrustList)
			throws FileExistsException, FileNotFoundException, PropertiesAccessException {
		setPropertiesRule();
		if (TSPAUtil.isFileExisting(mPath, frameworkname)) {
			throw new FileExistsException("Local store; Trustlist is already Existing for " + frameworkname);
		}

		PrintWriter file = new PrintWriter(mPath + "/" + frameworkname + ".xml");
		file.write(xmlTrustList);
		file.close();

		log.info("Local store; New XML trust-list is created with {}", frameworkname);
	}

	// --> Method for store XML trust-list in IPFS store.
	private void storenewTrustListXMLIPFS(String frameworkname, String xmlTrustList)
			throws PropertiesAccessException, FileExistsException {
		try {
			setDirectoryIPFS();

			String fileName = frameworkname + ".xml";
			String filepath = mPath + fileName;

			if (isTrustListAvailableIPFS(mPath, frameworkname) != null) {
				throw new FileExistsException("IPFS store; Trustlist is already Existing for " + frameworkname);
			}
			ipfs.files.rm(filepath, true, true);
			NamedStreamable.ByteArrayWrapper ns = new NamedStreamable.ByteArrayWrapper(fileName,
					xmlTrustList.getBytes());
			ipfs.files.write(filepath, ns, true, true);
			log.info("IPFS store; New XML trust-list is created with {} in Store.", frameworkname);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// --> Method for store JSON trust-list in local store.
	private void storeTrustListJSONLocal(String frameworkname, String jsonTrustList)
			throws FileExistsException, FileNotFoundException, PropertiesAccessException {
		setPropertiesRule();
		if (TSPAUtil.isFileExisting(mPath, frameworkname)) {
			throw new FileExistsException("Local store; Trustlist is already Existing for " + frameworkname);
		}

		PrintWriter file = new PrintWriter(mPath + "/" + frameworkname + ".json");
		file.write(jsonTrustList);
		file.close();

		log.info("Local store; New JSON trust-list is created with {} in store", frameworkname);

	}

	// --> Method for store JSON trust-list in IPFS store.
	private void storenewTrustListJSONIPFS(String frameworkname, String jsonTrustList)
			throws PropertiesAccessException, FileExistsException {
		try {
			setDirectoryIPFS();

			String fileName = frameworkname + ".json";
			String filepath = mPath + fileName;

			if (isTrustListAvailableIPFS(mPath, frameworkname) != null) {
				throw new FileExistsException("IPFS store; Trustlist is already Existing for " + frameworkname);
			}
			ipfs.files.rm(filepath, true, true);
			NamedStreamable.ByteArrayWrapper ns = new NamedStreamable.ByteArrayWrapper(fileName,
					jsonTrustList.getBytes());
			ipfs.files.write(filepath, ns, true, true);
			log.info("IPFS store; New JSON trust-list is created with {} in Store.", frameworkname);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// --> Method for fetching trust-list from local store.
	private String getTrustlistFromLocalStore(String frameworkName)
			throws IOException, FileEmptyException, PropertiesAccessException {
		setPropertiesRule();
		File trustlistFile = TSPAUtil.FindFileFromPath(mPath, frameworkName);

		if (trustlistFile != null) {
			String trustListAsString = new String(Files.readAllBytes(trustlistFile.toPath()), StandardCharsets.UTF_8);
			if (trustListAsString == null || trustListAsString.isEmpty()) {
				log.error("Local store; Trustlist {} file is Empty!!!", frameworkName);
				throw new FileEmptyException("Local store; Trust-list for " + frameworkName);
			}
			log.info("Local store; fetching trustlist {} from {}", frameworkName, mPath);
			return trustListAsString;
		} else {
			throw new FileNotFoundException(
					"Trustlist for " + frameworkName + " not found in local store at path " + mPath);
		}
	}

	// --> Method for getting trust-list from IPFS store.
	private String getTrustlistFromIPFS(String frameworkName) throws IOException {

		setDirectoryIPFS();
		String filename = isTrustListAvailableIPFS(mPath, frameworkName);
		if (filename == null) {
			throw new FileNotFoundException(
					"Trustlist for " + frameworkName + " not found in IPFS store at path " + mPath);
		}

		String filePath = mPath + filename;

		byte[] data = ipfs.files.read(filePath);
		if (data == null)
			throw new FileEmptyException("IPFS store; Trust-list for " + frameworkName);
		log.info("IPFS store; Fetching trustlist {} from {}", frameworkName, filePath);
		return new String(data, StandardCharsets.UTF_8);
	}

	// --> Method for deleting trust-list from local store.
	private String deleteTLfromLocalStore(String framworkname) throws PropertiesAccessException {
		setPropertiesRule();

		File trustlistFile = TSPAUtil.FindFileFromPath(mPath, framworkname);

		if (trustlistFile != null) {
			trustlistFile.delete();
			log.info("Local store, Deleting trustlist from path '{}'", framworkname, mPath);
			return "Successfully! Trust-list: '" + framworkname + "' deleted from local store.";
		} else {
			log.warn("Local store; Trust-list '{}' not avaliable in store!", framworkname);
			return "Trust list not available in local store.";
		}
	}

	// --> Method for the deleting trust-list from IPFS
	private String deleteTLfromIPFS(String framworkname) throws IOException {

		setDirectoryIPFS();
		String fileName = isTrustListAvailableIPFS(mPath, framworkname);
		if (fileName != null) {
			String filePath = mPath + fileName;
			ipfs.files.rm(filePath, true, true);
			log.info("IPFS store; remove trustlist {} from path {}.", framworkname, filePath);
			return "Successfully! Trust-list: '" + framworkname + "' deleted from IPFS store.";
		} else {
			log.info("IPFS store; Trust-list '{}' not avaliable in store!", framworkname);
			return "Trust list not available in IPFS store.";
		}
	}

	private Validator initValidator(Resource xsdPath) throws SAXException, IOException {
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = factory.newSchema(new StreamSource(xsdPath.getFile()));
		return schema.newValidator();
	}

	private JsonSchema getJSONschema(Resource jsonSchemaResource) throws FileNotFoundException, IOException {
		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
		return factory.getSchema(new FileInputStream(jsonSchemaResource.getFile()));
	}

	// --> method for checking the availability of trust-list in IPFS store.
	@SuppressWarnings("rawtypes")
	private String isTrustListAvailableIPFS(String path, String framwork) throws IOException, PropertiesAccessException {

		List<Map> ls = ipfs.files.ls(path);
		if (ls == null)
			return null;
		for (Map entry : ls) {
			if (entry.get("Name").toString().compareTo(framwork + ".xml") == 0
					|| entry.get("Name").toString().compareTo(framwork + ".json") == 0) {
				log.debug("IPFS store; File found with name '{}'", entry.get("Name").toString());
				return entry.get("Name").toString();
			}
		}
		return null;
	}

	// TSP--------------------------------------------------------------------------------------------------------

	// --> Set module for Jakarta annotations.
	private void setConfgurationObjectMapper() {
		log.debug("Set the properties for the Object mapper");
		JakartaXmlBindAnnotationModule module = new JakartaXmlBindAnnotationModule();
		this.omTrustList.registerModule(module);
	}

	// --> Enable some of the configuration for the root-level.(During the
	// (de-)serialization for the entire Trustlist )
	private void EnableRootElementConfig() {
		log.debug("Enable root-level (de-)serialization");
		omTrustList.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		omTrustList.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		//omTrustList.setSerializationInclusion(Include.NON_NULL);
	}

	// --> Disable some configuration for the root-level.(During the deserialization
	// of the TSPs array)
	private void UnableRootElementConfig() {
		log.debug("disable root-level (de-)serialization");
		omTrustList.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		omTrustList.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
	}

	// --> TSP publishing for the INTERNAL store.
	private void tspPublishLocalStore(String framework, String tsps)
			throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {

		setConfgurationObjectMapper();

		TrustServiceStatusList trustListPojo = null;
		List<TSPCustomType> tspList = new ArrayList<TSPCustomType>();

		JsonObject tspjsonObjct = (JsonObject) JsonParser.parseString(tsps);
		JsonElement tspElement = tspjsonObjct.get("TrustServiceProvider");
		if (tspElement.isJsonObject()) {
			tspList = handleSingleTSP(tspElement.toString());
		} else {
			tspList = ArrayToTSPCustomTypeList(tspElement.toString());
		}

		String existedTL = getTrustlistFromLocalStore(framework);
		String tlType = TSPAUtil.getContentType(existedTL);
		File existedTLFile = TSPAUtil.FindFileFromPath(mPath, framework);

		if (tlType.equals("xml")) {
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			trustListPojo = (TrustServiceStatusList) unmarshaller
					.unmarshal(new StreamSource(new StringReader(existedTL)));
			trustListPojo = addTSPinExistedTL(trustListPojo, tspList);
			PrintWriter file = new PrintWriter(existedTLFile);
			marshaller.marshal(trustListPojo, file);
			file.close();
		} else if (tlType.equals("json")) {
			EnableRootElementConfig();
			trustListPojo = omTrustList.readValue(existedTL, TrustServiceStatusList.class);
			trustListPojo = addTSPinExistedTL(trustListPojo, tspList);
			omTrustList.writerWithDefaultPrettyPrinter().writeValue(existedTLFile, trustListPojo);
		}

		// Updating VC after updating Trustlist.
		try {
			ivcService.createVC(framework, tlType);
		} catch (PropertiesAccessException | DecoderException | GeneralSecurityException | JsonLDException e) {
			e.printStackTrace();
		}

	}

	// --> TSP publishing for the IPFS store.
	private void tspPublishIPFS(String framework, String tsps)
			throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {

		setConfgurationObjectMapper();
		//setDirectoryIPFS(PATH_STORE_TRUSTLIST);

		TrustServiceStatusList trustListPojo = null;
		List<TSPCustomType> tspList = new ArrayList<TSPCustomType>();

		JsonObject tspjsonObjct = (JsonObject) JsonParser.parseString(tsps);
		JsonElement tspElement = tspjsonObjct.get("TrustServiceProvider");
		if (tspElement.isJsonObject()) {
			tspList = handleSingleTSP(tspElement.toString());
		} else {
			tspList = ArrayToTSPCustomTypeList(tspElement.toString());
		}

		String existedTL = getTrustlistFromIPFS(framework);
		String tlType = TSPAUtil.getContentType(existedTL);
		// File existedTLFile = TSPAUtil.FindFileFromPath(mPath, framework);

		if (tlType.equals("xml")) {
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			trustListPojo = (TrustServiceStatusList) unmarshaller
					.unmarshal(new StreamSource(new StringReader(existedTL)));
			trustListPojo = addTSPinExistedTL(trustListPojo, tspList);
			StringWriter sw = new StringWriter();
			marshaller.marshal(trustListPojo, sw);
			String newTrustlistAsString = sw.toString();
			ipfsWriter(framework, newTrustlistAsString, "xml");

		} else if (tlType.equals("json")) {
			EnableRootElementConfig();
			trustListPojo = omTrustList.readValue(existedTL, TrustServiceStatusList.class);
			trustListPojo = addTSPinExistedTL(trustListPojo, tspList);
			String newTrustlistAsString = omTrustList.writerWithDefaultPrettyPrinter()
					.writeValueAsString(trustListPojo);
			ipfsWriter(framework, newTrustlistAsString, "json");
		}

		// Updating VC after updating Trustlist.
		try {
			ivcService.createVC(framework, tlType);
		} catch (PropertiesAccessException | DecoderException | GeneralSecurityException | JsonLDException e) {
			e.printStackTrace();
		}

	}

	private List<TSPCustomType> handleSingleTSP(String tspJsonObj) {
		UnableRootElementConfig();
		List<TSPCustomType> tspList = new ArrayList<TSPCustomType>();
		TSPCustomType singleTsp = null;

		try {
			singleTsp = omTrustList.readValue(tspJsonObj, TSPCustomType.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		tspList.add(singleTsp);
		return tspList;
	}

	// Convert JsonArray of the Trust provider
	private List<TSPCustomType> ArrayToTSPCustomTypeList(String tspsArray) {
		UnableRootElementConfig();

		List<TSPCustomType> tspList = null;

		TypeReference<List<TSPCustomType>> listTypeReference = new TypeReference<List<TSPCustomType>>() {
		};
		try {
			tspList = omTrustList.readValue(tspsArray, listTypeReference);
			for (int i=0; i<tspList.size();i++) {
				for(int j= i+1; j<tspList.size();j++) {
					TSPCustomType tempTSP1 = tspList.get(i);
					TSPCustomType tempTSP2 = tspList.get(j);
					if(tempTSP1.getUUID().equals(tempTSP2.getUUID())) {
						throw new TSPException("TSP can't publish: UUID "+ tempTSP1.getUUID() +" passed multiple times in TSP array.");
					}
				}
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return tspList;
	}

	// Add TSP in existing Trustlist.
	private TrustServiceStatusList addTSPinExistedTL(TrustServiceStatusList trustlist, List<TSPCustomType> tsps)
			throws TSPException {

		TrustServiceProviderListCustomType trustServiceProviderListCustomType = new TrustServiceProviderListCustomType();

		// Checking UUID. matching
		if (trustlist.getTrustServiceProviderList() == null) {
			trustServiceProviderListCustomType.setTrustServiceProvider(tsps);

		} else {
			List<TSPCustomType> existingCustomTypesList = trustlist.getTrustServiceProviderList()
					.getTrustServiceProvider();
			if (existingCustomTypesList != null) {
				for (TSPCustomType single : tsps) {
					for (TSPCustomType tspexist : existingCustomTypesList) {
						if (tspexist.getUUID().equals(single.getUUID()) ) {
							throw new TSPException(
									"TSP can't publish : TSP with UUID " + single.getUUID() + " already exists.");
						}
					}
				}
				existingCustomTypesList.addAll(tsps);
				trustServiceProviderListCustomType.setTrustServiceProvider(existingCustomTypesList);
			} else {
				trustServiceProviderListCustomType.setTrustServiceProvider(tsps);
			}
		}
		trustlist.setTrustServiceProviderList(trustServiceProviderListCustomType);
		return trustlist;
	}

	// --> TSP delete from INTERNAL.
	private void tspDeleteLocalStore(String framework, String uuid)
			throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {

		setConfgurationObjectMapper();

		TrustServiceStatusList trustListPojo = null;

		String existedTL = getTrustlistFromLocalStore(framework);
		String tlType = TSPAUtil.getContentType(existedTL);
		File existedTLFile = TSPAUtil.FindFileFromPath(mPath, framework);

		if (tlType.equals("xml")) {
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			trustListPojo = (TrustServiceStatusList) unmarshaller
					.unmarshal(new StreamSource(new StringReader(existedTL)));
			trustListPojo = deleteTSPinExistedTL(trustListPojo, uuid);
			// marshaller.marshal(trustListPojo, existedTLFile);
			PrintWriter file = new PrintWriter(existedTLFile);
			marshaller.marshal(trustListPojo, file);
			file.close();
		} else if (tlType.equals("json")) {
			EnableRootElementConfig();
			trustListPojo = omTrustList.readValue(existedTL, TrustServiceStatusList.class);
			trustListPojo = deleteTSPinExistedTL(trustListPojo, uuid);
			omTrustList.writerWithDefaultPrettyPrinter().writeValue(existedTLFile, trustListPojo);
		}

		// Updating VC
		try {
			ivcService.createVC(framework, tlType);
		} catch (PropertiesAccessException | DecoderException | GeneralSecurityException | JsonLDException e) {
			e.printStackTrace();
		}

	}

	// -->TSP delete from IPFS.
	private void tspDeleteIPFS(String framework, String uuid)
			throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {

		setConfgurationObjectMapper();

		TrustServiceStatusList trustListPojo = null;

		String existedTL = getTrustlistFromIPFS(framework);
		String tlType = TSPAUtil.getContentType(existedTL);

		if (tlType.equals("xml")) {
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			trustListPojo = (TrustServiceStatusList) unmarshaller
					.unmarshal(new StreamSource(new StringReader(existedTL)));
			trustListPojo = deleteTSPinExistedTL(trustListPojo, uuid);
			StringWriter sw = new StringWriter();
			marshaller.marshal(trustListPojo, sw);
			String newTrustlistAsString = sw.toString();
			ipfsWriter(framework, newTrustlistAsString, "xml");
		} else if (tlType.equals("json")) {
			EnableRootElementConfig();
			trustListPojo = omTrustList.readValue(existedTL, TrustServiceStatusList.class);
			trustListPojo = deleteTSPinExistedTL(trustListPojo, uuid);
			String newTrustlistAsString = omTrustList.writerWithDefaultPrettyPrinter()
					.writeValueAsString(trustListPojo);
			ipfsWriter(framework, newTrustlistAsString, "json");
		}

		// Updating VC
		try {
			ivcService.createVC(framework, tlType);
		} catch (PropertiesAccessException | DecoderException | GeneralSecurityException | JsonLDException e) {
			e.printStackTrace();
		}

	}

	private TrustServiceStatusList deleteTSPinExistedTL(TrustServiceStatusList trustlist, String uuid) {
		TrustServiceStatusList newTrustList = deleteTsp(trustlist, uuid);
		if (newTrustList == null) {
			throw new TSPException("TSP can't deleted : Trust-list without single TSP");
		}
		return newTrustList;

	}

	private TrustServiceStatusList deleteTsp(TrustServiceStatusList trustlist, String uuid) {
		TrustServiceProviderListCustomType trustServiceProviderListCustomType = new TrustServiceProviderListCustomType();

		// Checking UUID. matching
		if (trustlist.getTrustServiceProviderList() == null) {

			return null;

		} else {
			List<TSPCustomType> existingCustomTypesList = trustlist.getTrustServiceProviderList()
					.getTrustServiceProvider();
			if (existingCustomTypesList != null) {
				for (TSPCustomType tsp : existingCustomTypesList) {
					if (uuid.equals(tsp.getUUID() )) {
						existingCustomTypesList.remove(tsp);
						trustServiceProviderListCustomType.setTrustServiceProvider(existingCustomTypesList);
						trustlist.setTrustServiceProviderList(trustServiceProviderListCustomType);
						return trustlist;
					}
				}
				throw new TSPException("TSP oprations can't perform : Unable to find TSP with UUID :" + uuid);
			}
			return null;
		}
	}

	// --> TSP update from INTERNAL
	private void tspUpdationLocalStore(String framework, String uuid, String newTSP)
			throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {

		setConfgurationObjectMapper();
		TrustServiceStatusList trustListPojo = null;
		List<TSPCustomType> tspList = new ArrayList<TSPCustomType>();

		JsonObject tspjsonObjct = (JsonObject) JsonParser.parseString(newTSP);
		JsonElement tspElement = tspjsonObjct.get("TrustServiceProvider");
		if (tspElement.isJsonObject()) {
			tspList = handleSingleTSP(tspElement.toString());
		} else {
			throw new TSPException("UpdatingTSP; 'TrustServiceProvider' should be JsonObject");
		}

		if (!uuid.equals(tspList.get(0).getUUID())) {
			throw new TSPException("TSP update failed, UUID should be " + uuid + " in updated TSP");
		}

		String existedTL = getTrustlistFromLocalStore(framework);
		String tlType = TSPAUtil.getContentType(existedTL);
		File existedTLFile = TSPAUtil.FindFileFromPath(mPath, framework);

		if (tlType.equals("xml")) {
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			// Existed TL.
			trustListPojo = (TrustServiceStatusList) unmarshaller
					.unmarshal(new StreamSource(new StringReader(existedTL)));

			// Requested TSP delete.
			trustListPojo = deleteTsp(trustListPojo, uuid);

			// Update new tsp.
			trustListPojo = addTSPinExistedTL(trustListPojo, tspList);

			PrintWriter file = new PrintWriter(existedTLFile);
			marshaller.marshal(trustListPojo, file);
			file.close();
		} else if (tlType.equals("json")) {
			EnableRootElementConfig();
			trustListPojo = omTrustList.readValue(existedTL, TrustServiceStatusList.class);
			trustListPojo = deleteTsp(trustListPojo, uuid);
			trustListPojo = addTSPinExistedTL(trustListPojo, tspList);
			omTrustList.writerWithDefaultPrettyPrinter().writeValue(existedTLFile, trustListPojo);
		}

		// Updating VC
		try {
			ivcService.createVC(framework, tlType);
		} catch (PropertiesAccessException | DecoderException | GeneralSecurityException | JsonLDException e) {
			e.printStackTrace();
		}

	}

	// --> TSP update from IPFS
	private void tspUpdationIPFS(String framework, String uuid, String newTSP)
			throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {

		setConfgurationObjectMapper();
		TrustServiceStatusList trustListPojo = null;
		List<TSPCustomType> tspList = new ArrayList<TSPCustomType>();

		JsonObject tspjsonObjct = (JsonObject) JsonParser.parseString(newTSP);
		JsonElement tspElement = tspjsonObjct.get("TrustServiceProvider");
		if (tspElement.isJsonObject()) {
			tspList = handleSingleTSP(tspElement.toString());
		} else {
			throw new TSPException("UpdatingTSP; 'TrustServiceProvider' should be JsonObject");
		}

		if (!uuid.equals(tspList.get(0).getUUID())) {
			throw new TSPException("TSP update failed, UUID should be " + uuid + " in updated TSP");
		}

		String existedTL = getTrustlistFromIPFS(framework);
		String tlType = TSPAUtil.getContentType(existedTL);

		if (tlType.equals("xml")) {
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			// Existed TL.
			trustListPojo = (TrustServiceStatusList) unmarshaller
					.unmarshal(new StreamSource(new StringReader(existedTL)));

			// Requested TSP delete.
			trustListPojo = deleteTsp(trustListPojo, uuid);

			// Update new tsp.
			trustListPojo = addTSPinExistedTL(trustListPojo, tspList);

			StringWriter sw = new StringWriter();
			marshaller.marshal(trustListPojo, sw);
			String newTrustlistAsString = sw.toString();
			ipfsWriter(framework, newTrustlistAsString, "xml");

		} else if (tlType.equals("json")) {
			EnableRootElementConfig();
			trustListPojo = omTrustList.readValue(existedTL, TrustServiceStatusList.class);
			trustListPojo = deleteTsp(trustListPojo, uuid);
			trustListPojo = addTSPinExistedTL(trustListPojo, tspList);
			String newTrustlistAsString = omTrustList.writerWithDefaultPrettyPrinter()
					.writeValueAsString(trustListPojo);
			ipfsWriter(framework, newTrustlistAsString, "json");
		}

		// Updating VC
		try {
			ivcService.createVC(framework, tlType);
		} catch (PropertiesAccessException | DecoderException | GeneralSecurityException | JsonLDException e) {
			e.printStackTrace();
		}

	}

	private void ipfsWriter(String frameworkname, String content, String type)
			throws PropertiesAccessException, IOException {
		setDirectoryIPFS();

		String fileName = frameworkname + "." + type;
		String filepath = mPath + fileName;

		// System.out.println("here");

		ipfs.files.rm(filepath, true, true);
		NamedStreamable.ByteArrayWrapper ns = new NamedStreamable.ByteArrayWrapper(fileName, content.getBytes());
		ipfs.files.write(filepath, ns, true, true);

	}
}