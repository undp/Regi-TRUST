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
// import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

// import org.apache.commons.codec.DecoderException;
// import org.bouncycastle.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.core.type.TypeReference;
// import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
// import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;
// import com.google.gson.JsonElement;
// import com.google.gson.JsonObject;
// import com.google.gson.JsonParser;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import eu.xfsc.train.tspa.exceptions.FileEmptyException;
import eu.xfsc.train.tspa.exceptions.FileExistsException;
import eu.xfsc.train.tspa.exceptions.PropertiesAccessException;
// import eu.xfsc.train.tspa.exceptions.TSPException;
import eu.xfsc.train.tspa.exceptions.XmlValidationError;
import eu.xfsc.train.tspa.interfaces.ITrustListPublicationService;
import eu.xfsc.train.tspa.interfaces.IVCService;
import eu.xfsc.train.tspa.model.trustlist.NameType;
import eu.xfsc.train.tspa.model.trustlist.TrustServiceStatusList;
import eu.xfsc.train.tspa.model.trustlist.TrustServiceStatusSimplifiedList;
import eu.xfsc.train.tspa.model.trustlist.tsp.TSPAddessType;
import eu.xfsc.train.tspa.model.trustlist.tsp.TSPCustomType;
// import eu.xfsc.train.tspa.model.trustlist.tsp.TSPCustomType;
import eu.xfsc.train.tspa.model.trustlist.tsp.TSPIdListType;
import eu.xfsc.train.tspa.model.trustlist.tsp.TSPInformationType;
import eu.xfsc.train.tspa.model.trustlist.tsp.TSPSimplified;
import eu.xfsc.train.tspa.model.trustlist.tsp.TrustServiceProviderListCustomType;
// import eu.xfsc.train.tspa.model.trustlist.tsp.TrustServiceProviderListCustomType;
import eu.xfsc.train.tspa.utils.IpfsUtil;
import eu.xfsc.train.tspa.utils.TSPAUtil;
// import foundation.identity.jsonld.JsonLDException;
import io.ipfs.api.IPFS;
import io.ipfs.api.NamedStreamable;
// import jakarta.el.ELException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
// import jakarta.xml.bind.Unmarshaller;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import com.mongodb.client.model.Collation;

@Service
public class TrustListPublicationServiceImpl implements ITrustListPublicationService {

	private static final Logger log = LoggerFactory.getLogger(TrustListPublicationServiceImpl.class);

	@Autowired
	ObjectMapper omTrustList;
	@Autowired
	JAXBContext jaxbContext;
	@Autowired
	IVCService ivcService;
	@Autowired
	private MongoTemplate mongoTemplate;

	
	@Value("${tspa.ipfs.rcp.api}")
	private String api;
	@Value("${storage.type.trustlist}")
	private String storeType;
	@Value("${storage.path.trustlist}")
	private String mPath;
	@Value("${spring.data.mongodb.database}")
	private String databaseName;
	@Value("${spring.data.mongodb.collection-trustlists}")
	private String collectionNameTrustlist;
	@Value("${spring.data.mongodb.collection-tsps}")
	private String collectionNameTsps;
	private IPFS ipfs;


	// TRUST LISTS ------------------------------------------------------------------------------------------------

	@Override
	public String test(String jsonObj) throws JsonMappingException, JsonProcessingException {

		try {
			TSPInformationType tspInformation = omTrustList.readValue(jsonObj, TSPInformationType.class);
			return omTrustList.writeValueAsString(tspInformation);
		} catch (JsonMappingException e) {
			log.error("Error mapping JSON to TSPAddessType", e);
			throw e;
		} catch (JsonProcessingException e) {
			log.error("Error processing JSON", e);
			throw e;
		}
	}

	@Override
	public String initXMLTrustList(String frameworkName, String xmlData)
			throws FileExistsException, PropertiesAccessException, JAXBException, FileEmptyException, IOException {
				setPropertiesRule();
				String trustListToStore = null;
				if (TSPAUtil.isFileExisting(mPath, frameworkName)) {
					throw new FileExistsException("A Trustlist xml file for this trust framework has already been created. " + frameworkName);
				}
				TrustServiceStatusSimplifiedList trustList = (TrustServiceStatusSimplifiedList) jaxbContext.createUnmarshaller()
				.unmarshal(new StringReader(xmlData));
				
				// Update Framework name, version and update date before storing
				NameType frameworkNameType = new NameType(frameworkName);
				trustList.getFrameworkInformation().setFrameworkName(frameworkNameType);
				trustList.getFrameworkInformation().setTslVersion("1");
				trustList.getFrameworkInformation().setListIssueDateTime(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
	
				// Marshal the updated object back to XML
				StringWriter writer = new StringWriter();
				jaxbContext.createMarshaller().marshal(trustList, writer);
				// update the trustlist to store with the updated trustlist TrustServiceStatusList
				trustListToStore = writer.toString();
			
				// write TL in local store and DB
				String tlUrl = storeTLInLocalStoreAndDB(frameworkName, trustListToStore, trustList);
				log.info("New XML trust list is created in local store  and DB. Framework name: {}", frameworkName);
				return tlUrl;
			}

	// --> Stores XML data into the local store and Trust List pojo into the DB
	private String storeTLInLocalStoreAndDB(String frameworkName, String fullTLxml, TrustServiceStatusSimplifiedList simplifiedTLpojo)
		throws FileExistsException, PropertiesAccessException, JAXBException, FileEmptyException, IOException {
			log.info("New XML trust list is created in local store  and DB. Framework name: {}", frameworkName);

			String tlUrl = mPath + "/" + frameworkName + ".xml";

			if (fullTLxml != null) {
			// write the full TL in local store
				PrintWriter file = new PrintWriter(tlUrl);
				file.write(fullTLxml);
				file.close();
			}

			if (simplifiedTLpojo != null) {
			// write the simplified TL in DB
			MongoDatabase db = mongoTemplate.getMongoDatabaseFactory().getMongoDatabase(databaseName);
			MongoCollection<Document> collection = db.getCollection(collectionNameTrustlist);
	
			String trustListJson = omTrustList.writeValueAsString(simplifiedTLpojo);
			Document trustListDocument = Document.parse(trustListJson);
				collection.insertOne(trustListDocument);		
		}
		return tlUrl;
	}
	
		// --> Updates FrameworkInformation only in local store and DB. (Modifies version and issuance date fields in the TL)
	@Override
	public String updateFrameworkInformation(String framework, String FrameworkInformation) 
		throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {
		setConfgurationObjectMapper();
	
		TrustServiceStatusSimplifiedList trustListPojo = null;
		String currentTrustList = getSimplifiedTLfromDB(framework, null);
		trustListPojo = omTrustList.readValue(currentTrustList, TrustServiceStatusSimplifiedList.class);

		if (trustListPojo.getFrameworkInformation().getTslVersion().equals("0")) {
			NameType frameworkName = new NameType(framework);
			trustListPojo.getFrameworkInformation().setFrameworkName(frameworkName);
		}

		// get the current version
		String currentVersion = trustListPojo.getFrameworkInformation().getTslVersion();
		TrustServiceStatusSimplifiedList newFrameworkInformation = omTrustList.readValue(FrameworkInformation, TrustServiceStatusSimplifiedList.class);
		newFrameworkInformation.getFrameworkInformation().setTslVersion(currentVersion);
		if (newFrameworkInformation.getTspSimplifiedList() == null) {
			newFrameworkInformation.setTspSimplifiedList(new TSPIdListType());
		}
		if (trustListPojo.getTspSimplifiedList() == null) {
			trustListPojo.setTspSimplifiedList(new TSPIdListType());
		}
		newFrameworkInformation.getTspSimplifiedList().setTspSimplified(trustListPojo.getTspSimplifiedList().getTspSimplified());
		trustListPojo.setFrameworkInformation(newFrameworkInformation.getFrameworkInformation());
		trustListPojo.getFrameworkInformation().getFrameworkName().setName(framework);
		trustListPojo = updateTLVersionRelatedFields(trustListPojo);
		log.debug("Trustlist version updated to {}", trustListPojo.getFrameworkInformation().getTslVersion());

		String newTLxml = buildXMLfromSimplifiedTL(trustListPojo);
		storeTLInLocalStoreAndDB(framework, newTLxml, trustListPojo);
	return trustListPojo.getFrameworkInformation().getTslVersion();


	 }
 
	// --> Method for fetching trustlist XML content from local store.
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
					"Trustlist for " + frameworkName + " not found in local store.");
		}
	}

	// --> Method for fetching a simplified trustlist in JSON (to string) format from DB.
	@Override
	public String getSimplifiedTLfromDB(String frameworkName, String version)
			throws IOException, FileEmptyException {
		try {
			MongoDatabase db = mongoTemplate.getMongoDatabaseFactory().getMongoDatabase(databaseName);
			MongoCollection<Document> collection = db.getCollection(collectionNameTrustlist);

			// First, check if the framework exists
			Document frameworkQuery = new Document("FrameworkInformation.FrameworkName.Name", frameworkName);
			long frameworkCount = collection.countDocuments(frameworkQuery);

			if (frameworkCount == 0) {
				throw new FileEmptyException("FRAMEWORK_NOT_FOUND. No trustlist found for framework " + frameworkName);
			}

			// If framework exists, proceed with version query
			Document query = new Document("FrameworkInformation.FrameworkName.Name", frameworkName);
			Document sort = new Document("FrameworkInformation.TSLVersion", -1);

			if (version != null) {
				query.append("FrameworkInformation.TSLVersion", version);
			}

			Document result = collection.find(query)
				.sort(sort)
				.collation(Collation.builder()
					.locale("en")
					.numericOrdering(true)
					.build())
				.first();

			if (result == null) {
				throw new FileEmptyException("VERSION_NOT_FOUND. Trust framework " + frameworkName + " does not have version " + version);
			}

			result.remove("_id");
			return result.toJson();
		} catch (Exception e) {
			log.error("Error fetching simplified trustlist from database for framework '{}' and version '{}'", 
					  frameworkName, version, e);
			throw e;
		}
	}

	@Override
	public String getFullXMLTrustlist(String frameworkName, String version) throws IOException, JAXBException {

		log.debug("Stored Type: {}", storeType);

		String resultTL = null;
		if (version == null) {
			resultTL = getTrustlistFromLocalStore(frameworkName);
		} else {
			// --> Fetch a specific version of the trustlist in String format (representation of the TL in JSON)
			resultTL = getSimplifiedTLfromDB(frameworkName, version);
			// --> Convert simplified TL to pojo
			TrustServiceStatusSimplifiedList simplifiedTLpojo = omTrustList.readValue(resultTL, TrustServiceStatusSimplifiedList.class);
			resultTL = buildXMLfromSimplifiedTL(simplifiedTLpojo);
		}
		return resultTL;
	}

	// --> Builds a full XML TL on the fly from a simplified TL pojo to be stored in the local store or delivered to the client
	@SuppressWarnings("null")
	public String buildXMLfromSimplifiedTL(TrustServiceStatusSimplifiedList simplifiedTLpojo) throws JAXBException, FileEmptyException, IOException {
		// get the mongo collection for TSPs
		MongoDatabase db = mongoTemplate.getMongoDatabaseFactory().getMongoDatabase(databaseName);
		MongoCollection<Document> tspCollection = db.getCollection(collectionNameTsps);

		TSPIdListType simplifiedTSPsList = simplifiedTLpojo.getTspSimplifiedList();
		TrustServiceProviderListCustomType detailedTSPsList = new TrustServiceProviderListCustomType();
		if (simplifiedTSPsList != null) {
			// iterate over simplifiedTSPsList and get detailed TSPs from DB
			if (simplifiedTSPsList.getTspSimplified() != null) {
				for (TSPSimplified tsp : simplifiedTSPsList.getTspSimplified()) {
					// query the DB for the TSP according to its TSPID and TSPVersion
				Document query = new Document("TSPID", tsp.getTspID());
					query.append("TSPVersion", tsp.getTspVersion());
					Document result = tspCollection.find(query).first();
					// Remove the "_id" field from the result before converting to TSPCustomType
					result.remove("_id");
				TSPCustomType detailedTsp = omTrustList.readValue(result.toJson(), TSPCustomType.class);
				// add detailedTsp to detailedTSPsList
				if (detailedTSPsList.getTrustServiceProvider() == null) {
					detailedTSPsList.setTrustServiceProvider(new ArrayList<>());
				}
					detailedTSPsList.getTrustServiceProvider().add(detailedTsp);
				}
			} else {
				log.warn("No simplified TSPs found in this trustlist");
			}
		} else {
			log.warn("No simplified TSPs found in this trustlist");
		}
		
		// make fullTLPojo with simplifiedTLpojo and detailedTSPsList
		TrustServiceStatusList fullTLPojo = new TrustServiceStatusList();
		fullTLPojo.setFrameworkInformation(simplifiedTLpojo.getFrameworkInformation());
		fullTLPojo.setTrustServiceProviderList(detailedTSPsList);	

		// convert fullTLPojo to XML
		StringWriter writer = new StringWriter();
		jaxbContext.createMarshaller().marshal(fullTLPojo, writer);
		return writer.toString();
	}	

	// --> Updates only the Version and Issuance Date fields of in the simplified TL
	public TrustServiceStatusSimplifiedList updateTLVersionRelatedFields(TrustServiceStatusSimplifiedList simplifiedTL) {
		int newVersion = Integer.parseInt(simplifiedTL.getFrameworkInformation().getTslVersion()) + 1;
		simplifiedTL.getFrameworkInformation().setTslVersion(String.valueOf(newVersion));
		simplifiedTL.getFrameworkInformation().setListIssueDateTime(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		return simplifiedTL;
	}

	// --> Method for fetching a list of versions of a trustlist
	@Override
	public String getTrustListVersions(String frameworkName) throws IOException {
		try {
			MongoDatabase db = mongoTemplate.getMongoDatabaseFactory().getMongoDatabase(databaseName);
			MongoCollection<Document> collection = db.getCollection(collectionNameTrustlist);    
			
			// Query to fetch all versions of a trustlist
			Document query = new Document("FrameworkInformation.FrameworkName.Name", frameworkName);
			List<Document> results = collection.find(query)
				.sort(new Document("FrameworkInformation.TSLVersion", -1))
				.into(new ArrayList<>());

				// if no lists were found with this framework name, return an empty list
			if (results.isEmpty()) {
				return "[]";
			}
			
			// Create response object
			Map<String, Object> response = new HashMap<>();
			response.put("trustFrameworkName", frameworkName);
			
			List<Map<String, String>> versions = new ArrayList<>();
			for (Document doc : results) {
				Document frameworkInfo = (Document) doc.get("FrameworkInformation");
				Map<String, String> version = new HashMap<>();
				version.put("TSLVersion", frameworkInfo.getString("TSLVersion"));
				version.put("ListIssueDateTime", frameworkInfo.getString("ListIssueDateTime"));
				versions.add(version);
			}
			response.put("trustListVersions", versions);
			
			// Convert to JSON string
			return omTrustList.writeValueAsString(response);
			
		} catch (Exception e) {
			log.error("Error fetching trust list versions for framework '{}'", frameworkName, e);
			throw new RuntimeException(e.getMessage());
		}
	}

	// TRUST SERVICE PROVIDERS ------------------------------------------------------------------------------------------------

	// --> Adds a TSP to the trustlist. 
	// 1- Update TSP in DB. 2- Add it as simplified TSP to the TL. 3- update TL version. 4- create new entry in TL DB. 5- finally update full XML file in local store
	@Override
	public String addTSPToTrustList(String frameworkName, String tspJson) 
	        throws FileEmptyException, PropertiesAccessException, DataAccessException, IOException {
	    // Verify framework exists by getting trustlist
	    getSimplifiedTLfromDB(frameworkName, null);

	    try {
	        JsonNode tspNode = omTrustList.readTree(tspJson);
	        final String tspId = tspNode.get("TSPID").asText();
			// By now we don't do validation of the BODY, just check if TSPID is present
			// To Do: validate the TSP body in the controller against the schema (?)
			if (tspId == null) {
				throw new PropertiesAccessException("TSPID field was not found in the TSP JSON");
			} 	        
	        MongoDatabase db = mongoTemplate.getMongoDatabaseFactory().getMongoDatabase(databaseName);
	        MongoCollection<Document> tspCollection = db.getCollection(collectionNameTsps);
	        
	        // Check in the DB if TSP exists
			Document existingTsp = null;
			Document query = new Document("TSPID", tspId);
			existingTsp = tspCollection.find(query)
				.sort(new Document("TSPVersion", -1))
				.first();

	        Document tspDoc = Document.parse(tspJson);
	        
			String newVersionStr = null;
	        if (existingTsp == null) { // create new TSP
				newVersionStr = "1";
				tspDoc.put("TSPVersion", newVersionStr);
	        } else { // TSP exists, throw an error
				throw new PropertiesAccessException("TSP " + tspId + " already exists.");
				// String currentVersionStr = existingTsp.get("TSPVersion") != null ? existingTsp.get("TSPVersion").toString() : "1";
				// int currentVersion = Integer.parseInt(currentVersionStr); 
				// tspDoc.put("TSPVersion", String.valueOf(currentVersion + 1));
				// newVersionStr = String.valueOf(currentVersion + 1);
				// log.info("Incrementing version for TSP {} from {} to {}", tspId, currentVersion, newVersionStr);

	        }
			tspDoc.put("LastUpdate", java.time.LocalDateTime.now().toString());
			String tspName = tspNode.path("TSPInformation").path("TSPName").path("Name").asText();
			tspDoc.put("TSPName", tspName);
			// step 1- update TSP in TSP collection in the DB
	        tspCollection.insertOne(tspDoc);

			// Step 2-  add it as simplified TSP to the TL
			TSPSimplified simplifiedTsp = new TSPSimplified();
			simplifiedTsp.setTspID(tspDoc.getString("TSPID"));
			simplifiedTsp.setLastUpdate(tspDoc.getString("LastUpdate"));
			simplifiedTsp.setTspVersion(newVersionStr);
			simplifiedTsp.setTspName(tspName);
			
			TrustServiceStatusSimplifiedList simplifiedTLpojo = omTrustList.readValue(getSimplifiedTLfromDB(frameworkName, null), TrustServiceStatusSimplifiedList.class);
			TSPIdListType tspSimplifiedList = null;
			if (simplifiedTLpojo.getTspSimplifiedList() != null) { // update existing TSPs in the TL 
				tspSimplifiedList = simplifiedTLpojo.getTspSimplifiedList();
				// check if TSP already exists in the TL
				if(tspSimplifiedList.getTspSimplified() != null) {
					if (tspSimplifiedList.getTspSimplified().stream().anyMatch(tsp -> tsp.getTspID().equals(tspId))) {
						// throw an error
						throw new PropertiesAccessException("TSP " + tspId + " already exists in the trustlist.");
					} 
				}
				// add simplifiedTsp to the TL
				if (tspSimplifiedList.getTspSimplified() != null) {
					tspSimplifiedList.getTspSimplified().add(simplifiedTsp);
				} else {
					List<TSPSimplified> listOfSimplifiedTSPs = new ArrayList<>();
					listOfSimplifiedTSPs.add(simplifiedTsp);
					tspSimplifiedList.setTspSimplified(listOfSimplifiedTSPs);
				}
			} else { // first TSP in the TL
				tspSimplifiedList = new TSPIdListType();
				List<TSPSimplified> listOfSimplifiedTSPs = new ArrayList<>();
				// add simplifiedTsp to listOfSimplifiedTSPs
				listOfSimplifiedTSPs.add(simplifiedTsp);
				tspSimplifiedList.setTspSimplified(listOfSimplifiedTSPs);
			}
			simplifiedTLpojo.setTspSimplifiedList(tspSimplifiedList);

			// Step 3- update TL version
			simplifiedTLpojo = updateTLVersionRelatedFields(simplifiedTLpojo);

			// Steps 4- and 5- create new entry in TL DB and write new full XML to local store
			String fullXML = buildXMLfromSimplifiedTL(simplifiedTLpojo);
			storeTLInLocalStoreAndDB(frameworkName, fullXML, simplifiedTLpojo);
	        
	        return String.format("TSP %s successfully created", 
	            tspId);
	            
	    } catch (Exception e) {
	        log.error("Error adding/updating TSP for framework {}", frameworkName, e);
	        throw new PropertiesAccessException("Failed to create TSP: " + e.getMessage());
	    }
	}

	// --> Returns a list of versions of a specific TSP
	@Override
	public String getTSPVersions(String frameworkName, String tspId) throws IOException {
		// check if framework exists throw an exception if not
		getSimplifiedTLfromDB(frameworkName, null); 
		// check if TSP exists throw an exception if not
		Document query = new Document("TSPID", tspId);
		MongoDatabase db = mongoTemplate.getMongoDatabaseFactory().getMongoDatabase(databaseName);
		MongoCollection<Document> tspCollection = db.getCollection(collectionNameTsps);
		Document result = tspCollection.find(query).first();
		if (result == null) {
			throw new FileEmptyException("TSP " + tspId + " not found in the trustlist.");
		}
		// fetch all versions of the TSP
		List<Document> versions = tspCollection.find(query).sort(new Document("TSPVersion", -1)).into(new ArrayList<>());
		Map<String, Object> response = new HashMap<>();
		response.put("tspId", tspId);
		Document resultDoc = (Document) result; // Cast result to Document
		Document tspInformation = (Document) resultDoc.get("TSPInformation"); // Get TSPInformation Document
		Document tspName = (Document) tspInformation.get("TSPName"); // Get TSPName Document
		String name = tspName.getString("Name"); // Get the Name field
		response.put("tspName", name);
		List<Map<String, Object>> versionList = new ArrayList<>();
		for (Document version : versions) {
			Map<String, Object> versionInfo = new HashMap<>();
			versionInfo.put("TSPVersion", version.getString("TSPVersion"));
			versionInfo.put("LastUpdate", version.getString("LastUpdate"));
			versionList.add(versionInfo);
		}
		response.put("versions", versionList);
		return omTrustList.writeValueAsString(response);
	}

	// --> Returns a specific TSP with optional version parameter
	@Override
	public String getSingleTSP(String frameworkName, String tspId, String version) throws FileEmptyException, IOException {
		// check if framework exists throw an exception if not
		getSimplifiedTLfromDB(frameworkName, null); 
		
		Document query = new Document("TSPID", tspId);
		MongoDatabase db = mongoTemplate.getMongoDatabaseFactory().getMongoDatabase(databaseName);
		MongoCollection<Document> tspCollection = db.getCollection(collectionNameTsps);
		
		Document result;
		if (version != null) {
			// If version is provided, fetch that specific version
			query.append("TSPVersion", version);
			result = tspCollection.find(query).first();
			if (result == null) {
				throw new FileEmptyException("Version " + version + " for TSP " + tspId + " not found.");
			}
		} else {
			// If no version provided, fetch the latest version by sorting in descending order
			result = tspCollection.find(query)
				.sort(new Document("TSPVersion", -1))
				.first();
			if (result == null) {
				throw new FileEmptyException("TSP " + tspId + " not found in the trustlist.");
			}
		}
		
		// Remove the "_id" field before returning
		result.remove("_id");
		return result.toJson();
	}

	// --> Updates a specific TSP
	@Override
	public String updateTSP(String frameworkName, String tspId, String tspJson) throws FileEmptyException, PropertiesAccessException, IOException {
		// check if framework exists throw an exception if not
		String simplifiedTLString = getSimplifiedTLfromDB(frameworkName, null); 
		TrustServiceStatusSimplifiedList currentSimplifiedTLpojo = omTrustList.readValue(simplifiedTLString, TrustServiceStatusSimplifiedList.class);
		// check if TSP exists throw an exception if not
		Document query = new Document("TSPID", tspId);
		MongoDatabase db = mongoTemplate.getMongoDatabaseFactory().getMongoDatabase(databaseName);
		MongoCollection<Document> tspCollection = db.getCollection(collectionNameTsps);
		Document tspToUpdate = tspCollection.find(query).sort(new Document("TSPVersion", -1)).first();
		if (tspToUpdate == null) {
			throw new FileEmptyException("TSP " + tspId + " not found in the trustlist for framework " + frameworkName + ".");
		}
		try {			
			// 1. Add the received TSP to the DB (updating its version and lastUpdate first)
			String udpateDate = java.time.LocalDateTime.now().toString();
			String newTSPVersionStr = String.valueOf(Integer.parseInt(tspToUpdate.getString("TSPVersion")) + 1);
			Document newTSP = Document.parse(tspJson);
			newTSP.put("TSPVersion", newTSPVersionStr);
			newTSP.put("LastUpdate", udpateDate);
			tspCollection.insertOne(newTSP);

			// 2. Update the simplified TL in the DB (updating first simplifiedTsp with the new version and lastUpdate)
			// find the simplifiedTsp in the currentSimplifiedTLpojo by its TSPID
			TSPIdListType tspSimplifiedList = currentSimplifiedTLpojo.getTspSimplifiedList();
			TSPSimplified simplifiedTspToUpdate = tspSimplifiedList.getTspSimplified().stream()
				.filter(tsp -> tsp.getTspID().equals(tspId))
				.findFirst()
				.orElse(null);
			simplifiedTspToUpdate.setTspVersion(newTSPVersionStr);
			simplifiedTspToUpdate.setLastUpdate(udpateDate);
			// replace the simplifiedTsp in the currentSimplifiedTLpojo
			tspSimplifiedList.getTspSimplified().set(tspSimplifiedList.getTspSimplified().indexOf(simplifiedTspToUpdate), simplifiedTspToUpdate);
			// update the currentSimplifiedTLpojo's TL version
			currentSimplifiedTLpojo.setTspSimplifiedList(tspSimplifiedList);
			currentSimplifiedTLpojo = updateTLVersionRelatedFields(currentSimplifiedTLpojo);

			// 3. Update the full XML file in the local store and the DB
			String fullXML = buildXMLfromSimplifiedTL(currentSimplifiedTLpojo);
			storeTLInLocalStoreAndDB(frameworkName, fullXML, currentSimplifiedTLpojo);

		} catch (Exception e) {
			log.error("Error updating TSP for framework {}", frameworkName, e);
			throw new PropertiesAccessException("Failed to update TSP: " + e.getMessage());
		}
		return String.format("TSP with ID %s updated successfully", tspId);
	}


// GXFS IMPLEMENTATION ------------------------------------------------------------------------------------------------

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

	// @Override
	// public void tspPublish(String frameworkName, String newTsps)
	// 		throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {

	// 	log.debug("Stored Type: {}", storeType);

	// 	if (storeType.compareTo("IPFS") == 0) {
	// 		tspPublishIPFS(frameworkName, newTsps);
	// 	} else if (storeType.compareTo("INTERNAL") == 0) {
	// 		tspPublishLocalStore(frameworkName, newTsps);

	// 	}
	// }

	// @Override
	// public void tspUpdate(String frameworkName, String uuid, String tsp)
	// 		throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {

	// 	log.debug("Stored Type: {}", storeType);

	// 	if (storeType.compareTo("IPFS") == 0) {
	// 		tspUpdationIPFS(frameworkName, uuid, tsp);
	// 	} else if (storeType.compareTo("INTERNAL") == 0) {
	// 		tspUpdationLocalStore(frameworkName, uuid, tsp);

	// 	}
	// }

	// @Override
	// public void tspRemove(String frameworkName, String uuid)
	// 		throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {
	// 	log.debug("Stored Type: {}", storeType);

	// 	if (storeType.compareTo("IPFS") == 0) {
	// 		tspDeleteIPFS(frameworkName, uuid);
	// 	} else if (storeType.compareTo("INTERNAL") == 0) {
	// 		tspDeleteLocalStore(frameworkName, uuid);

	// 	}

	// }

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
		Locale.setDefault(Locale.ENGLISH); // Set the default locale to English
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

	// // --> Method for getting trust-list from IPFS store.
	// private String getTrustlistFromIPFS(String frameworkName) throws IOException {

	// 	setDirectoryIPFS();
	// 	String filename = isTrustListAvailableIPFS(mPath, frameworkName);
	// 	if (filename == null) {
	// 		throw new FileNotFoundException(
	// 				"Trustlist for " + frameworkName + " not found in IPFS store at path " + mPath);
	// 	}

	// 	String filePath = mPath + filename;

	// 	byte[] data = ipfs.files.read(filePath);
	// 	if (data == null)
	// 		throw new FileEmptyException("IPFS store; Trust-list for " + frameworkName);
	// 	log.info("IPFS store; Fetching trustlist {} from {}", frameworkName, filePath);
	// 	return new String(data, StandardCharsets.UTF_8);
	// }

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

	// // --> Enable some of the configuration for the root-level.(During the
	// // (de-)serialization for the entire Trustlist )
	// private void EnableRootElementConfig() {
	// 	log.debug("Enable root-level (de-)serialization");
	// 	omTrustList.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
	// 	omTrustList.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
	// 	//omTrustList.setSerializationInclusion(Include.NON_NULL);
	// }

	// // --> Disable some configuration for the root-level.(During the deserialization
	// // of the TSPs array)
	// private void UnableRootElementConfig() {
	// 	log.debug("disable root-level (de-)serialization");
	// 	omTrustList.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
	// 	omTrustList.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
	// }

	// // --> TSP publishing for the INTERNAL store.
	// private void tspPublishLocalStore(String framework, String tsps)
	// 		throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {

	// 	setConfgurationObjectMapper();

	// 	TrustServiceStatusList trustListPojo = null;
	// 	List<TSPCustomType> tspList = new ArrayList<TSPCustomType>();

	// 	JsonObject tspjsonObjct = (JsonObject) JsonParser.parseString(tsps);
	// 	JsonElement tspElement = tspjsonObjct.get("TrustServiceProvider");
	// 	if (tspElement.isJsonObject()) {
	// 		tspList = handleSingleTSP(tspElement.toString());
	// 	} else {
	// 		tspList = ArrayToTSPCustomTypeList(tspElement.toString());
	// 	}

	// 	String existedTL = getTrustlistFromLocalStore(framework);
	// 	String tlType = TSPAUtil.getContentType(existedTL);
	// 	File existedTLFile = TSPAUtil.FindFileFromPath(mPath, framework);

	// 	if (tlType.equals("xml")) {
	// 		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
	// 		Marshaller marshaller = jaxbContext.createMarshaller();
	// 		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	// 		trustListPojo = (TrustServiceStatusList) unmarshaller
	// 				.unmarshal(new StreamSource(new StringReader(existedTL)));
	// 		trustListPojo = addTSPinExistedTL(trustListPojo, tspList);
	// 		PrintWriter file = new PrintWriter(existedTLFile);
	// 		marshaller.marshal(trustListPojo, file);
	// 		file.close();
	// 	} else if (tlType.equals("json")) {
	// 		EnableRootElementConfig();
	// 		trustListPojo = omTrustList.readValue(existedTL, TrustServiceStatusList.class);
	// 		trustListPojo = addTSPinExistedTL(trustListPojo, tspList);
	// 		omTrustList.writerWithDefaultPrettyPrinter().writeValue(existedTLFile, trustListPojo);
	// 	}

	// 	// // get trustlist POJO (from DB)
	// 	// String currentTrustList = getSimplifiedTLfromDB(framework, null);
	// 	// // convert it into POJO
	// 	// TrustServiceStatusList currentTrustListPojo = omTrustList.readValue(currentTrustList, TrustServiceStatusList.class);


	// 	// Updating VC after updating Trustlist.
	// 	try {
	// 		ivcService.createVC(framework, tlType);
	// 	} catch (PropertiesAccessException | DecoderException | GeneralSecurityException | JsonLDException e) {
	// 		e.printStackTrace();
	// 	}

	// }

	// // --> TSP publishing for the IPFS store.
	// private void tspPublishIPFS(String framework, String tsps)
	// 		throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {

	// 	setConfgurationObjectMapper();
	// 	//setDirectoryIPFS(PATH_STORE_TRUSTLIST);

	// 	TrustServiceStatusList trustListPojo = null;
	// 	List<TSPCustomType> tspList = new ArrayList<TSPCustomType>();

	// 	JsonObject tspjsonObjct = (JsonObject) JsonParser.parseString(tsps);
	// 	JsonElement tspElement = tspjsonObjct.get("TrustServiceProvider");
	// 	if (tspElement.isJsonObject()) {
	// 		tspList = handleSingleTSP(tspElement.toString());
	// 	} else {
	// 		tspList = ArrayToTSPCustomTypeList(tspElement.toString());
	// 	}

	// 	String existedTL = getTrustlistFromIPFS(framework);
	// 	String tlType = TSPAUtil.getContentType(existedTL);
	// 	// File existedTLFile = TSPAUtil.FindFileFromPath(mPath, framework);

	// 	if (tlType.equals("xml")) {
	// 		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
	// 		Marshaller marshaller = jaxbContext.createMarshaller();
	// 		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	// 		trustListPojo = (TrustServiceStatusList) unmarshaller
	// 				.unmarshal(new StreamSource(new StringReader(existedTL)));
	// 		trustListPojo = addTSPinExistedTL(trustListPojo, tspList);
	// 		StringWriter sw = new StringWriter();
	// 		marshaller.marshal(trustListPojo, sw);
	// 		String newTrustlistAsString = sw.toString();
	// 		ipfsWriter(framework, newTrustlistAsString, "xml");

	// 	} else if (tlType.equals("json")) {
	// 		EnableRootElementConfig();
	// 		trustListPojo = omTrustList.readValue(existedTL, TrustServiceStatusList.class);
	// 		trustListPojo = addTSPinExistedTL(trustListPojo, tspList);
	// 		String newTrustlistAsString = omTrustList.writerWithDefaultPrettyPrinter()
	// 				.writeValueAsString(trustListPojo);
	// 		ipfsWriter(framework, newTrustlistAsString, "json");
	// 	}

	// 	// Updating VC after updating Trustlist.
	// 	try {
	// 		ivcService.createVC(framework, tlType);
	// 	} catch (PropertiesAccessException | DecoderException | GeneralSecurityException | JsonLDException e) {
	// 		e.printStackTrace();
	// 	}

	// }

	// private List<TSPCustomType> handleSingleTSP(String tspJsonObj) {
	// 	UnableRootElementConfig();
	// 	List<TSPCustomType> tspList = new ArrayList<TSPCustomType>();
	// 	TSPCustomType singleTsp = null;

	// 	try {
	// 		singleTsp = omTrustList.readValue(tspJsonObj, TSPCustomType.class);
	// 	} catch (JsonProcessingException e) {
	// 		e.printStackTrace();
	// 	}
	// 	tspList.add(singleTsp);
	// 	return tspList;
	// }

	// // Convert JsonArray of the Trust provider
	// private List<TSPCustomType> ArrayToTSPCustomTypeList(String tspsArray) {
	// 	UnableRootElementConfig();

	// 	List<TSPCustomType> tspList = null;

	// 	TypeReference<List<TSPCustomType>> listTypeReference = new TypeReference<List<TSPCustomType>>() {
	// 	};
	// 	try {
	// 		tspList = omTrustList.readValue(tspsArray, listTypeReference);
	// 		for (int i=0; i<tspList.size();i++) {
	// 			for(int j= i+1; j<tspList.size();j++) {
	// 				TSPCustomType tempTSP1 = tspList.get(i);
	// 				TSPCustomType tempTSP2 = tspList.get(j);
	// 				if(tempTSP1.getTspID().equals(tempTSP2.getTspID())) {
	// 					throw new TSPException("TSP can't publish: UUID "+ tempTSP1.getTspID() +" passed multiple times in TSP array.");
	// 				}
	// 			}
	// 		}
	// 	} catch (JsonProcessingException e) {
	// 		e.printStackTrace();
	// 	}
		
	// 	return tspList;
	// }

	// // Add TSP in existing Trustlist.
	// private TrustServiceStatusList addTSPinExistedTL(TrustServiceStatusList trustlist, List<TSPCustomType> tsps)
	// 		throws TSPException {

	// 	TrustServiceProviderListCustomType trustServiceProviderListCustomType = new TrustServiceProviderListCustomType();

	// 	// Checking UUID. matching
	// 	if (trustlist.getTrustServiceProviderList() == null) {
	// 		trustServiceProviderListCustomType.setTrustServiceProvider(tsps);

	// 	} else {
	// 		List<TSPCustomType> existingCustomTypesList = trustlist.getTrustServiceProviderList()
	// 				.getTrustServiceProvider();
	// 		if (existingCustomTypesList != null) {
	// 			for (TSPCustomType single : tsps) {
	// 				for (TSPCustomType tspexist : existingCustomTypesList) {
	// 					if (tspexist.getTspID().equals(single.getTspID()) ) {
	// 						throw new TSPException(
	// 								"TSP can't publish : TSP with UUID " + single.getTspID() + " already exists.");
	// 					}
	// 				}
	// 			}
	// 			existingCustomTypesList.addAll(tsps);
	// 			trustServiceProviderListCustomType.setTrustServiceProvider(existingCustomTypesList);
	// 		} else {
	// 			trustServiceProviderListCustomType.setTrustServiceProvider(tsps);
	// 		}
	// 	}
	// 	trustlist.setTrustServiceProviderList(trustServiceProviderListCustomType);
	// 	return trustlist;
	// }

	// // --> TSP delete from INTERNAL.
	// private void tspDeleteLocalStore(String framework, String uuid)
	// 		throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {

	// 	setConfgurationObjectMapper();

	// 	TrustServiceStatusList trustListPojo = null;

	// 	String existedTL = getTrustlistFromLocalStore(framework);
	// 	String tlType = TSPAUtil.getContentType(existedTL);
	// 	File existedTLFile = TSPAUtil.FindFileFromPath(mPath, framework);

	// 	if (tlType.equals("xml")) {
	// 		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
	// 		Marshaller marshaller = jaxbContext.createMarshaller();
	// 		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	// 		trustListPojo = (TrustServiceStatusList) unmarshaller
	// 				.unmarshal(new StreamSource(new StringReader(existedTL)));
	// 		trustListPojo = deleteTSPinExistedTL(trustListPojo, uuid);
	// 		// marshaller.marshal(trustListPojo, existedTLFile);
	// 		PrintWriter file = new PrintWriter(existedTLFile);
	// 		marshaller.marshal(trustListPojo, file);
	// 		file.close();
	// 	} else if (tlType.equals("json")) {
	// 		EnableRootElementConfig();
	// 		trustListPojo = omTrustList.readValue(existedTL, TrustServiceStatusList.class);
	// 		trustListPojo = deleteTSPinExistedTL(trustListPojo, uuid);
	// 		omTrustList.writerWithDefaultPrettyPrinter().writeValue(existedTLFile, trustListPojo);
	// 	}

	// 	// Updating VC
	// 	try {
	// 		ivcService.createVC(framework, tlType);
	// 	} catch (PropertiesAccessException | DecoderException | GeneralSecurityException | JsonLDException e) {
	// 		e.printStackTrace();
	// 	}

	// }

	// // -->TSP delete from IPFS.
	// private void tspDeleteIPFS(String framework, String uuid)
	// 		throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {

	// 	setConfgurationObjectMapper();

	// 	TrustServiceStatusList trustListPojo = null;

	// 	String existedTL = getTrustlistFromIPFS(framework);
	// 	String tlType = TSPAUtil.getContentType(existedTL);

	// 	if (tlType.equals("xml")) {
	// 		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
	// 		Marshaller marshaller = jaxbContext.createMarshaller();
	// 		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	// 		trustListPojo = (TrustServiceStatusList) unmarshaller
	// 				.unmarshal(new StreamSource(new StringReader(existedTL)));
	// 		trustListPojo = deleteTSPinExistedTL(trustListPojo, uuid);
	// 		StringWriter sw = new StringWriter();
			
	// 		marshaller.marshal(trustListPojo, sw);
	// 		String newTrustlistAsString = sw.toString();
	// 		ipfsWriter(framework, newTrustlistAsString, "xml");
	// 	} else if (tlType.equals("json")) {
	// 		EnableRootElementConfig();
	// 		trustListPojo = omTrustList.readValue(existedTL, TrustServiceStatusList.class);
	// 		trustListPojo = deleteTSPinExistedTL(trustListPojo, uuid);
	// 		String newTrustlistAsString = omTrustList.writerWithDefaultPrettyPrinter()
	// 				.writeValueAsString(trustListPojo);
	// 		ipfsWriter(framework, newTrustlistAsString, "json");
	// 	}

	// 	// Updating VC
	// 	try {
	// 		ivcService.createVC(framework, tlType);
	// 	} catch (PropertiesAccessException | DecoderException | GeneralSecurityException | JsonLDException e) {
	// 		e.printStackTrace();
	// 	}

	// }

	// private TrustServiceStatusList deleteTSPinExistedTL(TrustServiceStatusList trustlist, String uuid) {
	// 	TrustServiceStatusList newTrustList = deleteTsp(trustlist, uuid);
	// 	if (newTrustList == null) {
	// 		throw new TSPException("TSP can't deleted : Trust-list without single TSP");
	// 	}
	// 	return newTrustList;

	// }

	// private TrustServiceStatusList deleteTsp(TrustServiceStatusList trustlist, String uuid) {
	// 	TrustServiceProviderListCustomType trustServiceProviderListCustomType = new TrustServiceProviderListCustomType();

	// 	// Checking UUID. matching
	// 	if (trustlist.getTrustServiceProviderList() == null) {

	// 		return null;

	// 	} else {
	// 		List<TSPCustomType> existingCustomTypesList = trustlist.getTrustServiceProviderList()
	// 				.getTrustServiceProvider();
	// 		if (existingCustomTypesList != null) {
	// 			for (TSPCustomType tsp : existingCustomTypesList) {
	// 				if (uuid.equals(tsp.getTspID() )) {
	// 					existingCustomTypesList.remove(tsp);
	// 					trustServiceProviderListCustomType.setTrustServiceProvider(existingCustomTypesList);
	// 					trustlist.setTrustServiceProviderList(trustServiceProviderListCustomType);
	// 					return trustlist;
	// 				}
	// 			}
	// 			throw new TSPException("TSP oprations can't perform : Unable to find TSP with UUID :" + uuid);
	// 		}
	// 		return null;
	// 	}
	// }

	// --> TSP update from INTERNAL
	// private void tspUpdationLocalStore(String framework, String uuid, String newTSP)
	// 		throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {

	// 	setConfgurationObjectMapper();
	// 	TrustServiceStatusList trustListPojo = null;
	// 	List<TSPCustomType> tspList = new ArrayList<TSPCustomType>();

	// 	JsonObject tspjsonObjct = (JsonObject) JsonParser.parseString(newTSP);
	// 	JsonElement tspElement = tspjsonObjct.get("TrustServiceProvider");
	// 	if (tspElement.isJsonObject()) {
	// 		tspList = handleSingleTSP(tspElement.toString());
	// 	} else {
	// 		throw new TSPException("UpdatingTSP; 'TrustServiceProvider' should be JsonObject");
	// 	}

	// 	if (!uuid.equals(tspList.get(0).getTspID())) {
	// 		throw new TSPException("TSP update failed, UUID should be " + uuid + " in updated TSP");
	// 	}

	// 	String existedTL = getTrustlistFromLocalStore(framework);
	// 	String tlType = TSPAUtil.getContentType(existedTL);
	// 	File existedTLFile = TSPAUtil.FindFileFromPath(mPath, framework);

	// 	if (tlType.equals("xml")) {
	// 		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
	// 		Marshaller marshaller = jaxbContext.createMarshaller();
	// 		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	// 		// Existed TL.
	// 		trustListPojo = (TrustServiceStatusList) unmarshaller
	// 				.unmarshal(new StreamSource(new StringReader(existedTL)));

	// 		// Requested TSP delete.
	// 		trustListPojo = deleteTsp(trustListPojo, uuid);

	// 		// Update new tsp.
	// 		trustListPojo = addTSPinExistedTL(trustListPojo, tspList);

	// 		PrintWriter file = new PrintWriter(existedTLFile);
	// 		marshaller.marshal(trustListPojo, file);
	// 		file.close();
	// 	} else if (tlType.equals("json")) {
	// 		EnableRootElementConfig();
	// 		trustListPojo = omTrustList.readValue(existedTL, TrustServiceStatusList.class);
	// 		trustListPojo = deleteTsp(trustListPojo, uuid);
	// 		trustListPojo = addTSPinExistedTL(trustListPojo, tspList);
	// 		omTrustList.writerWithDefaultPrettyPrinter().writeValue(existedTLFile, trustListPojo);
	// 	}

	// 	// Updating VC
	// 	try {
	// 		ivcService.createVC(framework, tlType);
	// 	} catch (PropertiesAccessException | DecoderException | GeneralSecurityException | JsonLDException e) {
	// 		e.printStackTrace();
	// 	}

	// }

	// // --> TSP update from IPFS
	// private void tspUpdationIPFS(String framework, String uuid, String newTSP)
	// 		throws FileEmptyException, PropertiesAccessException, IOException, JAXBException {

	// 	setConfgurationObjectMapper();
	// 	TrustServiceStatusList trustListPojo = null;
	// 	List<TSPCustomType> tspList = new ArrayList<TSPCustomType>();

	// 	JsonObject tspjsonObjct = (JsonObject) JsonParser.parseString(newTSP);
	// 	JsonElement tspElement = tspjsonObjct.get("TrustServiceProvider");
	// 	if (tspElement.isJsonObject()) {
	// 		tspList = handleSingleTSP(tspElement.toString());
	// 	} else {
	// 		throw new TSPException("UpdatingTSP; 'TrustServiceProvider' should be JsonObject");
	// 	}

	// 	if (!uuid.equals(tspList.get(0).getTspID())) {
	// 		throw new TSPException("TSP update failed, UUID should be " + uuid + " in updated TSP");
	// 	}

	// 	String existedTL = getTrustlistFromIPFS(framework);
	// 	String tlType = TSPAUtil.getContentType(existedTL);

	// 	if (tlType.equals("xml")) {
	// 		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
	// 		Marshaller marshaller = jaxbContext.createMarshaller();
	// 		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	// 		// Existed TL.
	// 		trustListPojo = (TrustServiceStatusList) unmarshaller
	// 				.unmarshal(new StreamSource(new StringReader(existedTL)));

	// 		// Requested TSP delete.
	// 		trustListPojo = deleteTsp(trustListPojo, uuid);

	// 		// Update new tsp.
	// 		trustListPojo = addTSPinExistedTL(trustListPojo, tspList);

	// 		StringWriter sw = new StringWriter();
	// 		marshaller.marshal(trustListPojo, sw);
	// 		String newTrustlistAsString = sw.toString();
	// 		ipfsWriter(framework, newTrustlistAsString, "xml");

	// 	} else if (tlType.equals("json")) {
	// 		EnableRootElementConfig();
	// 		trustListPojo = omTrustList.readValue(existedTL, TrustServiceStatusList.class);
	// 		trustListPojo = deleteTsp(trustListPojo, uuid);
	// 		trustListPojo = addTSPinExistedTL(trustListPojo, tspList);
	// 		String newTrustlistAsString = omTrustList.writerWithDefaultPrettyPrinter()
	// 				.writeValueAsString(trustListPojo);
	// 		ipfsWriter(framework, newTrustlistAsString, "json");
	// 	}

	// 	// Updating VC
	// 	try {
	// 		ivcService.createVC(framework, tlType);
	// 	} catch (PropertiesAccessException | DecoderException | GeneralSecurityException | JsonLDException e) {
	// 		e.printStackTrace();
	// 	}

	// }

	// private void ipfsWriter(String frameworkname, String content, String type)
	// 		throws PropertiesAccessException, IOException {
	// 	setDirectoryIPFS();

	// 	String fileName = frameworkname + "." + type;
	// 	String filepath = mPath + fileName;

	// 	// System.out.println("here");

	// 	ipfs.files.rm(filepath, true, true);
	// 	NamedStreamable.ByteArrayWrapper ns = new NamedStreamable.ByteArrayWrapper(fileName, content.getBytes());
	// 	ipfs.files.write(filepath, ns, true, true);

	// }
}
