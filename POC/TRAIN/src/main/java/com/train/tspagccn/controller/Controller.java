package com.train.tspagccn.controller;

import com.google.gson.Gson;
import com.nimbusds.jose.shaded.json.JSONArray;
import com.train.tspagccn.api.VersionFactory;
import com.train.tspagccn.api.exceptions.InvalidStatusCodeException;
import com.train.tspagccn.api.exceptions.UnknownInterfaceVersion;
import com.train.tspagccn.api.v1.models.json.JsonTrustListPublication;
import com.train.tspagccn.interfaces.publication.IPublicationManager;
import com.train.tspagccn.interfaces.zone.IZoneManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMSource;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

@RestController
@RequestMapping("api/v1")
public class Controller {
  // @PathParam("api_version")
  public String mApiVersion = "v1";

  Log mLog = LogFactory.getLog(Controller.class);
  // mLog.info("Reached inside api v1");
  PropertiesConfiguration mConfig = null;
  IZoneManager mZoneManager = null;
  IPublicationManager mPublicationManager = null;

  public Controller() throws ConfigurationException, UnknownInterfaceVersion {
    mConfig = new PropertiesConfiguration("config.properties");
  }

  private void buildManager(String version) throws UnknownInterfaceVersion {
    mZoneManager = VersionFactory.makeZoneManagerFactory(version);
    mPublicationManager = VersionFactory.makePublicationManagerFactory(version);

    mZoneManager.setConfiguration(mConfig);
    mPublicationManager.setConfiguration(mConfig);
    // System.out.println("reached here");
    mLog.info("zone and publication manager configured with config.properties");
  }

  /*
   * Query a scheme (XML file) from server. Returns the XML trust list as JDOM
   * document
   */
  private Document getTrustScheme(String schemeName)
    throws IOException, JDOMException, UnknownInterfaceVersion {
    IPublicationManager publicationManager;
    publicationManager = VersionFactory.makePublicationManagerFactory(mApiVersion);
    publicationManager.setConfiguration(mConfig);
    String trustListXmlString;
    trustListXmlString = publicationManager.getTrustScheme(schemeName);
    if (trustListXmlString == null) {
      throw new IOException();
    }
    InputStream stream = new ByteArrayInputStream(trustListXmlString.getBytes("UTF-8"));
    SAXBuilder builder = new SAXBuilder();
    // https://rules.sonarsource.com/java/RSPEC-2755 // prevent xxe
    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    Document trustRegistry;
    trustRegistry = builder.build(stream);
    return trustRegistry;
  }

  /* Publishes a XML file to the server with schemeName */
  public ResponseEntity<String> publishXmlFromXml(String schemeName, String data) {
    try {
      buildManager(mApiVersion);
      // String signed = mPublicationManager.signTrustList(data);
      mPublicationManager.publishTrustList(schemeName, data);
      // X509Certificate cert = mPublicationManager.extractSignature(data);
      // mLog.debug(cert);
      // mLog.debug(Arrays.toString(cert.getEncoded()));
      // String certEncoded = DigestUtils.sha256Hex(cert.getEncoded());

      JsonTrustListPublication pub = new JsonTrustListPublication();
      // String localTestPublishUrl =
      // "http://localhost:8000/tspa/api/v1/gccn/trustregistry/";
      // String localTestPublishUrl =
      // "https://essif.iao.fraunhofer.de/tspa/api/v1/gccn/trustregistry/";
      // String localTestPublishUrl =
      // "https://essif.trust-scheme.de/tspa/api/v1/gccn/trustregistry/";
      String publishUrlBase = mConfig.getString("publishUrlBase");
      pub.setUrl(publishUrlBase + "/gccn/trustregistry/" + schemeName);

      // JsonDaneCertificate dane = new JsonDaneCertificate();
      // dane.setData(certEncoded.toUpperCase());
      // dane.setUsage("dane-ee");
      // dane.setSelector("cert");
      // dane.setMatching("sha256");
      // pub.setCertificate(dane);
      // if (!pub.verify()) {
      // throw new IOException();
      // }

      Gson gson = new Gson();
      String zoneData = gson.toJson(pub);
      mZoneManager.publishTrustList(schemeName, zoneData);
    } catch (UnknownInterfaceVersion unknownInterfaceVersion) {
      mLog.error(unknownInterfaceVersion.getMessage(), unknownInterfaceVersion);
      return new ResponseEntity<String>(
        "Failed to build API interface",
        HttpStatus.INTERNAL_SERVER_ERROR
      );
    } catch (IOException e) {
      mLog.error(e.getMessage(), e);
      return new ResponseEntity<String>(
        "Failed to publish trust list. " + e.getMessage(),
        HttpStatus.INTERNAL_SERVER_ERROR
      );
    } catch (InvalidStatusCodeException e) {
      mLog.error(e.getMessage(), e);
      return new ResponseEntity<String>(
        "Error. " + e.getMessage() + "with code: " + e.getCode(),
        HttpStatus.INTERNAL_SERVER_ERROR
      );
    }
    return new ResponseEntity<String>(
      "TSP added. Trust scheme " + schemeName + " succesfully updated",
      HttpStatus.OK
    );
  }

  /*
   * (1) Adds new TSP (json object as body parameter) into the test XML file in
   * the test server
   */
  @RolesAllowed("Registry_reviewer")
  @RequestMapping(
    value = "/gccn/trustlist/publish/{TrustSchemeName}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON
  )
  public ResponseEntity<String> addIndividualTSPfromJson(
    @PathVariable("TrustSchemeName") String trustSchemeName,
    @RequestBody String body,
    @RequestHeader String Authorization
  )
    throws JDOMException, IOException, JSONException {
    mLog.info("------- PUT REQUEST TO ADD NEW TSP TO TRUST REGISTRY --------");

    // --> Get the XML file as JDOM document
    Document trustRegistry;
    try {
      trustRegistry = getTrustScheme(trustSchemeName);
    } catch (IOException e) {
      mLog.error("The scheme claim is not published within this TSPA");
      return new ResponseEntity<String>(
        "TSP could not be added. Trust scheme " + trustSchemeName + " was not found.",
        HttpStatus.NOT_FOUND
      );
    } catch (JDOMException | UnknownInterfaceVersion e) {
      mLog.error(e.getMessage());
      return new ResponseEntity<String>("Problem in trust list.", HttpStatus.BAD_REQUEST);
    }

    // --> Find the node in the tree where the new TSP must be added
    Namespace namespace = Namespace.getNamespace("http://uri.etsi.org/02231/v2#");
    Element root = trustRegistry.getRootElement(); // "TrustServiceStatusList"
    Element tspListElement = root.getChild("TrustServiceProviderList", namespace);

    // --> convert the Json request body into XML format
    JSONObject jsonBody = new JSONObject(body);
    String xmlTsp = XML.toString(jsonBody);

    // --> prevent multiple entries for the same TSP
    String uid = jsonBody.getJSONObject("TrustServiceProvider").getString("UID");
    List<Element> list = tspListElement.getChildren("TrustServiceProvider");
    Element existitngTSP = list
      .stream()
      .filter(tspElement -> uid.equals(tspElement.getChild("UID").getValue()))
      .findAny()
      .orElse(null);
    if (existitngTSP != null) {
      return new ResponseEntity<String>(
        "TSP with UID " + uid + " already exists.",
        HttpStatus.BAD_REQUEST
      );
    }

    // --> convert the xml TSP string into an element of a new document
    StringReader stringReader = new StringReader(xmlTsp);
    SAXBuilder newBuilder = new SAXBuilder();
    Document newDoc = newBuilder.build(stringReader);
    Element rootE = newDoc.getRootElement();

    // --> add new TSP to whole xml structure
    tspListElement.addContent(rootE.detach());

    // --> validate the final XML against XSD schema

    /*
     * mLog.info("Performing validation before publishing..."); String
     * validationResponse = isValidXML("xsd", trustRegistry); if (validationResponse
     * != "OK") { mLog.info("Validation failed"); return new ResponseEntity<String>(
     * "Request body failed schema validation. \n " + validationResponse,
     * HttpStatus.BAD_REQUEST ); }
     */

    // --> Publish the updated XML to the server
    String outputter = new XMLOutputter(Format.getPrettyFormat())
    .outputString(trustRegistry);

    ResponseEntity<String> response = publishXmlFromXml(trustSchemeName, outputter);

    return response;
  }

  /**
   * (2) GET a simplified list of TSPs in schemeName registry: {
   * "TrustListEntries": [ { "TSPName": "string", "TrustSchemeName": "string" } ]
   * }
   *
   * Must be called as GET
   * /atvtrain_vc/api/vl/gccn/trustregistry/{GCCN_Scheme_Name}
   *
   * @throws JSONException
   */
  @RequestMapping(
    value = "/gccn/trustregistry/{TrustSchemeName}",
    method = RequestMethod.GET
  )
  public ResponseEntity<String> getSimplifiedTspList(
    @PathVariable("TrustSchemeName") String trustSchemeName
  )
    throws IOException, JDOMException, JSONException {
    mLog.info(
      "--------------- GET SIMPLIFIED LIST OF TSPs IN A TRUST REGISTRY ---------------"
    );
    // --> Get the XML file as JDOM document
    Document trustRegistry;
    try {
      trustRegistry = getTrustScheme(trustSchemeName);
    } catch (IOException e) {
      mLog.error("The scheme name was not found");
      return new ResponseEntity<String>(
        "Trust list with scheme name " + trustSchemeName + " was not found.",
        HttpStatus.NOT_FOUND
      );
    } catch (JDOMException | UnknownInterfaceVersion e) {
      mLog.error(e.getMessage());
      return new ResponseEntity<String>("Problem in trust list.", HttpStatus.BAD_REQUEST);
    }

    Element root = trustRegistry.getRootElement(); // "TrustServiceStatusList"
    Namespace namespace = Namespace.getNamespace("http://uri.etsi.org/02231/v2#");
    Element tspListElement = root.getChild("TrustServiceProviderList", namespace);

    // Create a JSON Object to be sent as a response
    JSONObject jsonResponse = new JSONObject();
    JSONArray tspArray = new JSONArray();

    // Extract all TSPs in the trust scheme and list them in json format
    List<Element> list = tspListElement.getChildren("TrustServiceProvider");
    for (Element tsp : list) {
      String UID = tsp.getChild("UID").getValue();
      String tspName = tsp.getChild("TSPInformation").getChild("TSPName").getValue();
      String tspTrustSchemeName = tsp
        .getChild("TSPInformation")
        .getChild("TrustSchemeName")
        .getValue();
      JSONObject trustListEntry = new JSONObject();
      trustListEntry.put("UID", UID);
      trustListEntry.put("TSPName", tspName);
      trustListEntry.put("TrustSchemeName", tspTrustSchemeName);
      tspArray.appendElement(trustListEntry);
    }
    jsonResponse.put("trustListEntries", tspArray);
    String jsonPrettyPrintString = "";
    jsonPrettyPrintString = jsonResponse.toString(4);
    return ResponseEntity.ok(jsonPrettyPrintString.toString());
  }

  /**
   * (3) GET a list of full detailed TSPs in the trust registry schemeName.
   *
   * @throws JSONException
   */
  @RequestMapping(
    value = "/gccn/trustlist/country/tsp/{TrustSchemeName}",
    method = RequestMethod.GET
  )
  public ResponseEntity<String> getTspList(
    @PathVariable("TrustSchemeName") String trustSchemeName
  )
    throws IOException, JDOMException, JSONException {
    mLog.info(
      "--------------- GET LIST OF DETAILED TSPs IN A TRUST REGISTRY ---------------"
    );

    // --> Get the XML file as JDOM document
    Document trustRegistry;
    try {
      trustRegistry = getTrustScheme(trustSchemeName);
    } catch (IOException e) {
      mLog.error("The scheme name was not found");
      return new ResponseEntity<String>(
        "Trust list with scheme name " + trustSchemeName + " was not found.",
        HttpStatus.NOT_FOUND
      );
    } catch (JDOMException | UnknownInterfaceVersion e) {
      mLog.error(e.getMessage());
      return new ResponseEntity<String>("Problem in trust list.", HttpStatus.BAD_REQUEST);
    }

    Element root = trustRegistry.getRootElement(); // "TrustServiceStatusList"
    Namespace namespace = Namespace.getNamespace("http://uri.etsi.org/02231/v2#");
    Element tspListElement = root.getChild("TrustServiceProviderList", namespace);

    // Create a JSON Object to be sent as a response and array for detailed TSPs
    JSONObject jsonResponse = new JSONObject();
    JSONArray tspArray = new JSONArray();

    // Extract all TSPs in the trust scheme and list them in json format
    List<Element> list = tspListElement.getChildren("TrustServiceProvider");
    for (Element currentTSP : list) {
      String outputter = new XMLOutputter(Format.getPrettyFormat())
      .outputString(currentTSP);

      JSONObject xmlJSONObj;
      String jsonPrettyPrintString = "";
      try {
        xmlJSONObj = XML.toJSONObject(outputter);
        int PRETTY_PRINT_INDENT_FACTOR = 4;
        jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
        System.out.println(jsonPrettyPrintString);
        tspArray.appendElement(xmlJSONObj);
      } catch (JSONException e) {
        System.out.println(e.toString());
        return ResponseEntity
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("JSON object handling error");
      }
    }

    jsonResponse.put("TrustedServiceProviderDetails", tspArray);
    String jsonPrettyPrintString = "";
    jsonPrettyPrintString = jsonResponse.toString(4);
    return ResponseEntity.ok(jsonPrettyPrintString.toString());
  }

  /**
   * (4) GET the details of an individual TSP. It returns the same json object
   * that was used as body in the PUT (publish) request.
   *
   * @throws JSONException
   */
  @Produces(MediaType.APPLICATION_JSON)
  @RequestMapping(
    value = "gccn/trustlist/tsp/individual/{TrustSchemeName}/{UID}",
    method = RequestMethod.GET
  )
  // @RequestMapping(value =
  public ResponseEntity<String> getIndividualTSP(
    @PathVariable("TrustSchemeName") String trustSchemeName,
    @PathVariable("UID") String uid
  )
    throws IOException, JDOMException, JSONException {
    mLog.info("--------------- GET DETAILS OF AN INDIVIDUAL TSP ---------------");

    // --> Get the XML file as JDOM document
    Document trustRegistry;
    try {
      trustRegistry = getTrustScheme(trustSchemeName);
    } catch (IOException e) {
      mLog.error("The scheme name was not found");
      return new ResponseEntity<String>(
        "Trust list with scheme name " + trustSchemeName + " was not found.",
        HttpStatus.NOT_FOUND
      );
    } catch (JDOMException | UnknownInterfaceVersion e) {
      mLog.error(e.getMessage());
      return new ResponseEntity<String>("Problem in trust list.", HttpStatus.BAD_REQUEST);
    }

    Element root = trustRegistry.getRootElement(); // "TrustServiceStatusList"
    Namespace namespace = Namespace.getNamespace("http://uri.etsi.org/02231/v2#");
    Element tspListElement = root.getChild("TrustServiceProviderList", namespace);
    List<Element> list = tspListElement.getChildren("TrustServiceProvider");

    // find the individual TSP by using scheme name
    Element currentTSP = list
      .stream()
      .filter(tspElement -> uid.equals(tspElement.getChild("UID").getValue()))
      .findAny()
      .orElse(null);

    if (currentTSP == null) {
      System.out.format("TSP with UID %s not found", uid);
      return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body("TSP with UID " + uid + " not found.");
    } else {
      // Here: return the json object of currentTSP
      mLog.info(
        "--> Current ID taken directly from the XML: " +
        currentTSP.getChild("UID").getValue()
      );
      String outputter = new XMLOutputter(Format.getPrettyFormat())
      .outputString(currentTSP);
      JSONObject xmlJSONObj;
      String jsonPrettyPrintString = "";
      try {
        xmlJSONObj = XML.toJSONObject(outputter);
        int PRETTY_PRINT_INDENT_FACTOR = 4;
        jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
        // System.out.println(jsonPrettyPrintString);
      } catch (JSONException je) {
        System.out.println(je.toString());
        return ResponseEntity
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("JSON object handling error");
      }

      return new ResponseEntity<String>(jsonPrettyPrintString, HttpStatus.OK);
    }
  }

  /**
   * (5) Returns the XML file of specific trust scheme
   *
   * @throws JSONException
   *
   */
  // @Produces(MediaType.APPLICATION_XML)
  @RequestMapping(
    value = "/scheme/{TrustSchemeName}",
    method = RequestMethod.GET,
    produces = { "application/xml", "text/xml" }
  )
  public ResponseEntity<String> getXMLTrustScheme(
    @PathVariable("TrustSchemeName") String trustSchemeName
  )
    throws IOException, JDOMException, JSONException {
    mLog.info("--------------- GET TRUST SCHEME (XML) ---------------");
    mLog.info("Scheme Name: '" + trustSchemeName + "'");

    // --> Get the XML file as JDOM document
    Document trustRegistry;
    String xmlTrustRegistry = "";
    try {
      trustRegistry = getTrustScheme(trustSchemeName);
      xmlTrustRegistry =
        new XMLOutputter(Format.getPrettyFormat()).outputString(trustRegistry);
    } catch (IOException e) {
      mLog.error("The scheme claim is not published within this TSPA");
      return new ResponseEntity<String>(
        "TSP could not be added. Trust scheme " + trustSchemeName + " was not found.",
        HttpStatus.NOT_FOUND
      );
    } catch (JDOMException | UnknownInterfaceVersion e) {
      mLog.error(e.getMessage());
      return new ResponseEntity<String>("Problem in trust list.", HttpStatus.BAD_REQUEST);
    }

    return ResponseEntity.status(HttpStatus.OK).body(xmlTrustRegistry);
  }

  /*
   * [Setup 1] Creates a scheme
   */
  @RequestMapping(
    value = "/{scheme_name}/schemes",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON
  )
  public Response publishSchemeJson(
    @PathVariable("scheme_name") String schemeName,
    @RequestBody String data
  ) {
    mLog.info("--------------- PUBLISH TRUST SCHEME (JSON) ---------------");
    mLog.debug("calling publishSchemeJson endpoint -> publishing scheme from JSON data!");
    mLog.info("Trust Scheme Name: '" + schemeName + "'");
    mLog.info("Data: '" + data + "'");
    System.out.println("jsonBody:" + " " + data);

    try {
      buildManager(mApiVersion);
      mZoneManager.publishTrustSchemes(schemeName, data);
      mPublicationManager.publishTrustList(schemeName, data);
    } catch (UnknownInterfaceVersion unknownInterfaceVersion) {
      mLog.error(unknownInterfaceVersion.getMessage(), unknownInterfaceVersion);
      return Response.serverError().entity("Failed to build API interface").build();
    } catch (IOException e) {
      mLog.error(e.getMessage(), e);
      return Response
        .serverError()
        .entity("Failed to publish trust scheme " + schemeName + " : " + e.getMessage())
        .build();
    } catch (InvalidStatusCodeException e) {
      mLog.error(e.getMessage(), e);
      return Response.status(e.getCode()).entity(e.getCustomErrorMessage()).build();
    }

    return Response.status(Response.Status.CREATED).build();
  }

  /* [Setup 2] Publishes an empty trust list (XML file) with no TSPs */
  @RequestMapping(
    value = "/{scheme_name}/trust-list",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_XML
  )
  public Response publishTrustListXml(
    @PathVariable("scheme_name") String schemeName,
    @RequestBody String data
  ) {
    mLog.info("--------------- PUBLISH TRUST LIST (XML) ---------------");
    mLog.debug(
      "Calling publishTrustListXML endpoint -> publishing trust list from XML data!"
    );
    mLog.info("Trust Scheme Name: '" + schemeName + "'");
    mLog.info("Data: '" + data + "'");

    try {
      buildManager(mApiVersion);
      // String signed = mPublicationManager.signTrustList(data);
      mPublicationManager.publishTrustList(schemeName, data);

      // Throwing error in extractSignature: The node is empty. To be checked.
      // 27.02.2023
      // X509Certificate cert = mPublicationManager.extractSignature(data);
      // mLog.debug(cert);
      // mLog.debug(Arrays.toString(cert.getEncoded()));
      // String certEncoded = DigestUtils.sha256Hex(cert.getEncoded());

      JsonTrustListPublication pub = new JsonTrustListPublication();
      // pub.setUrl("http://localhost:8000/api/v1/scheme/" + schemeName);
      // pub.setUrl("https://essif.iao.fraunhofer.de/tspa/api/v1/scheme/" +
      // schemeName);
      // where the trust list XML is located (can be accessed directly from browser)
      String publishUrlBase = mConfig.getString("publishUrlBase");
      pub.setUrl(publishUrlBase + "/scheme/" + schemeName);

      // JsonDaneCertificate dane = new JsonDaneCertificate();
      // dane.setData(certEncoded.toUpperCase());
      // dane.setUsage("dane-ee");
      // dane.setSelector("cert");
      // dane.setMatching("sha256");
      //
      // pub.setCertificate(dane);
      //
      // if (!pub.verify()) {
      // throw new IOException();
      // }

      Gson gson = new Gson();
      String zoneData = gson.toJson(pub);
      mZoneManager.publishTrustList(schemeName, zoneData);
    } catch (UnknownInterfaceVersion unknownInterfaceVersion) {
      mLog.error(unknownInterfaceVersion.getMessage(), unknownInterfaceVersion);
      return Response.serverError().entity("Failed to build API interface").build();
    } catch (IOException e) {
      // removed exceptions: | SAXException | XPathExpressionException |
      // CertificateEncodingException | ParserConfigurationException |
      // CertificateException
      mLog.error(e.getMessage(), e);
      return Response
        .serverError()
        .entity("Failed to publish trust list! " + e.getMessage())
        .build();
    } catch (InvalidStatusCodeException e) {
      mLog.error(e.getMessage(), e);
      return Response.status(e.getCode()).entity(e.getCustomErrorMessage()).build();
    }

    return Response.ok().build();
  }

  /*
   * Helper function to validate a XML file against a defined XSD trust list
   * scheme.
   */
  private static String isValidXML(String xsdPath, Document xmlDocument) {
    // parse an XML document into a DOM tree
    DocumentBuilder parser;
    org.w3c.dom.Document document;
    try {
      parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();

      // Option 1, convert the JDOM xml document into a file
      // XMLOutputter outputter = new XMLOutputter();
      // String docString = outputter.outputString(xmlDocument);
      // InputStream inputstream = new ByteArrayInputStream(docString.toByteArray());

      // This is to convert JDOM xml document into an InputStream to then be parsed
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      Source xmlSource = new JDOMSource(xmlDocument);
      Result outputTarget = new StreamResult(outputStream);
      try {
        TransformerFactory
          .newInstance()
          .newTransformer()
          .transform(xmlSource, outputTarget);
      } catch (TransformerException | TransformerFactoryConfigurationError e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return e.getLocalizedMessage();
      }
      InputStream inputstreamFromXMLdoc = new ByteArrayInputStream(
        outputStream.toByteArray()
      );

      document = parser.parse(inputstreamFromXMLdoc); // receives an InsputStream
      // document = parser.parse(new File("xmlPath")); // receives an InsputStream
      // create a SchemaFactory capable of understanding WXS schemas
      SchemaFactory factory = SchemaFactory.newInstance(
        XMLConstants.W3C_XML_SCHEMA_NS_URI
      );

      // load a WXS schema, represented by a Schema instance
      // Source schemaFile = new StreamSource(new File(xsdPath));
      Source schemaFile = new StreamSource(
        new File("./src/main/resources/validationSchema.xsd")
      );
      Schema schema = factory.newSchema(schemaFile);

      // create a Validator instance, which can be used to validate an instance
      // document
      Validator validator = schema.newValidator();
      validator.validate(new DOMSource(document));
      System.out.println("Schema validation OK.");
      return "OK";
    } catch (SAXException | IOException e) {
      System.err.println("Validation exception. " + e.getLocalizedMessage());
      return e.getLocalizedMessage();
    } catch (ParserConfigurationException e) {
      System.err.println("Validation exception. " + e.getLocalizedMessage());
      // e.printStackTrace();
      return e.getLocalizedMessage();
    }
  }

  /* Not used */
  @Path("/{scheme_name}/trust-list")
  @Consumes(MediaType.APPLICATION_JSON)
  @PUT
  public Response publishTrustListJson(
    @PathParam("scheme_name") String schemeName,
    String data
  ) {
    // PUT https://nameserver.address/api/{scheme_name]/trust-list
    // Content: * JsonTrustListPublication Object (JSON)
    // * Trust List Content (XML)

    mLog.info("--------------- PUBLISH TRUST LIST (JSON) ---------------");
    mLog.debug(
      "Calling publishTrustListJson endpoint -> publishing trust list from JSON data!"
    );
    mLog.info("Trust Scheme Name: '" + schemeName + "'");
    mLog.info("Data: '" + data + "'");

    try {
      buildManager(mApiVersion);

      Gson gson = new Gson();
      JsonTrustListPublication pub = gson.fromJson(data, JsonTrustListPublication.class);
      if (!pub.verify()) {
        throw new IOException();
      }

      mZoneManager.publishTrustList(schemeName, data);
    } catch (UnknownInterfaceVersion unknownInterfaceVersion) {
      mLog.error(unknownInterfaceVersion.getMessage(), unknownInterfaceVersion);
      return Response.serverError().entity("Failed to build API interface").build();
    } catch (IOException e) {
      mLog.error(e.getMessage(), e);
      return Response
        .serverError()
        .entity("Failed to publish trust list! " + e.getMessage())
        .build();
    } catch (InvalidStatusCodeException e) {
      mLog.error(e.getMessage(), e);
      return Response.status(e.getCode()).entity(e.getCustomErrorMessage()).build();
    }

    mLog.debug("Successfully published!");

    return Response.ok().build();
  }

  /* Not used yet */
  @RequestMapping(value = "/{scheme_name}/schemes", method = RequestMethod.DELETE)
  public Response deleteScheme(
    @RequestHeader String Authorization,
    @PathVariable("scheme_name") String schemeName
  ) {
    mLog.info("--------------- DELETE TRUST SCHEME ---------------");
    mLog.debug("calling deleteScheme endpoint");
    mLog.info("Trust Scheme Name: '" + schemeName + "'");

    try {
      buildManager(mApiVersion);
      mZoneManager.deleteTrustSchemes(schemeName);

      mLog.debug("  deleting of scheme not yet implemented in mPublicationManager.");
    } catch (UnknownInterfaceVersion unknownInterfaceVersion) {
      mLog.error(unknownInterfaceVersion.getMessage(), unknownInterfaceVersion);
      return Response.serverError().build();
    } catch (IOException e) {
      mLog.error(e.getMessage(), e);
      return Response.serverError().build();
    } catch (InvalidStatusCodeException e) {
      mLog.error(e.getMessage(), e);
      switch (e.getCode()) {
        case 404:
          return Response
            .status(e.getCode())
            .entity(
              e.getCode() +
              ": There exists No trust scheme with the given trust scheme parameter"
            )
            .build();
        case 500:
          return Response
            .status(e.getCode())
            .entity("ZoneManager is currently not reachable!")
            .build();
        default:
          return Response
            .status(e.getCode())
            .entity(e.getCode() + ": " + e.getMessage())
            .build();
      }
    }

    return Response.ok().build();
  }

  @RequestMapping(
    value = "/gccn/trustlist/update/{TrustSchemeName}/{UID}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON
  )
  public ResponseEntity<String> updateIndividualTSPfromJson(
    @PathVariable("TrustSchemeName") String trustSchemeName,
    @PathVariable("UID") String UID,
    @RequestBody String body
    // @RequestHeader String Authorization add before commit.
  )
    throws JDOMException, IOException, JSONException {
    mLog.info("------- PUT REQUEST TO ADD NEW TSP TO TRUST REGISTRY --------");

    // --> Get the XML file as JDOM document
    Document trustRegistry;
    try {
      trustRegistry = getTrustScheme(trustSchemeName);
    } catch (IOException e) {
      mLog.error("The scheme claim is not published within this TSPA");
      return new ResponseEntity<String>(
        "TSP could not be added. Trust scheme " + trustSchemeName + " was not found.",
        HttpStatus.NOT_FOUND
      );
    } catch (JDOMException | UnknownInterfaceVersion e) {
      mLog.error(e.getMessage());
      return new ResponseEntity<String>("Problem in trust list.", HttpStatus.BAD_REQUEST);
    }

    // --> Find the node in the tree where the new TSP must be added
    Namespace namespace = Namespace.getNamespace("http://uri.etsi.org/02231/v2#");
    Element root = trustRegistry.getRootElement(); // "TrustServiceStatusList"
    Element tspListElement = root.getChild("TrustServiceProviderList", namespace);

    // --> convert the Json request body into XML format
    JSONObject jsonBody = new JSONObject(body);
    String xmlTsp = XML.toString(jsonBody);

    // --> prevent multiple entries for the same TSP
    String uid = jsonBody.getJSONObject("TrustServiceProvider").getString("UID");
    List<Element> list = tspListElement.getChildren("TrustServiceProvider");

    for (Element e : list) {
      String id = e.getChild("UID").getValue();

      if ((UID).equals(id.trim())) {
        e.detach();

        break;
      }
    }

    // --> convert the xml TSP string into an element of a new document
    StringReader stringReader = new StringReader(xmlTsp);
    SAXBuilder newBuilder = new SAXBuilder();
    Document newDoc = newBuilder.build(stringReader);

    Element rootE = newDoc.getRootElement();

    // --> add new TSP to whole xml structure
    tspListElement.addContent(rootE.detach());

    // --> Publish the updated XML to the server
    String outputter = new XMLOutputter(Format.getPrettyFormat())
    .outputString(trustRegistry);
    ResponseEntity<String> response = publishXmlFromXml(trustSchemeName, outputter);

    return response;
  }
}
