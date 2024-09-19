package com.train.tspagccn.services;

import com.google.gson.Gson;
import com.train.tspagccn.api.VersionFactory;
import com.train.tspagccn.api.exceptions.InvalidStatusCodeException;
import com.train.tspagccn.api.exceptions.UnknownInterfaceVersion;
import com.train.tspagccn.api.v1.models.json.JsonDaneCertificate;
import com.train.tspagccn.api.v1.models.json.JsonTrustListPublication;
import com.train.tspagccn.api.v1.publication.misc.Base16;
import com.train.tspagccn.interfaces.publication.IPublicationManager;
import com.train.tspagccn.interfaces.zone.IZoneManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

//@RestController
@Path("/{api_version}")
//@RequestMapping("/{api_version}")
public class PublicationService {
  @PathParam("api_version")
  private String mApiVersion;

  Log mLog = LogFactory.getLog(PublicationService.class);

  PropertiesConfiguration mConfig = null;
  IZoneManager mZoneManager = null;
  IPublicationManager mPublicationManager = null;

  public PublicationService() throws ConfigurationException, UnknownInterfaceVersion {
    mConfig = new PropertiesConfiguration("config.properties");
  }

  private void buildManager(String version) throws UnknownInterfaceVersion {
    mZoneManager = VersionFactory.makeZoneManagerFactory(version);
    mPublicationManager = VersionFactory.makePublicationManagerFactory(version);

    mZoneManager.setConfiguration(mConfig);
    mPublicationManager.setConfiguration(mConfig);
  }

  //@Path("/test")
  //@GET
  @RolesAllowed("valet")
  @RequestMapping(value = "/test", method = RequestMethod.GET)
  public ResponseEntity<String> test(@RequestHeader String Authorization) {
    mLog.info("--------------- TEST ---------------");
    try {
      mLog.debug("Calling test endpoint");
      mLog.debug("Creating API Version ...");

      buildManager(mApiVersion);
    } catch (UnknownInterfaceVersion unknownInterfaceVersion) {
      mLog.error(unknownInterfaceVersion.getMessage(), unknownInterfaceVersion);
      //return ResponseEntity.ok(Response.Status.BAD_REQUEST);
    }

    mLog.debug(mConfig.getString("nameserver.address"));
    mLog.debug(mApiVersion + " This is the Api Version");
    mLog.debug(mZoneManager.toString());

    return ResponseEntity.ok("ApiVersion: " + mApiVersion);
  }

  @Path("/{scheme_name}/trust-list")
  @Consumes(MediaType.APPLICATION_JSON)
  @PUT
  public Response publishTrustListJson(
    @PathParam("scheme_name") String schemeName,
    String data
  ) {
    // PUT https://nameserver.address/api/{scheme_name]/trust-list
    // Content: * JsonTrustListPublication Object (JSON)
    //          * Trust List Content (XML)

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

  @Path("/{scheme_name}/trust-list")
  @Consumes(MediaType.APPLICATION_XML)
  @PUT
  public Response publishTrustListXml(
    @PathParam("scheme_name") String schemeName,
    String data
  ) {
    mLog.info("--------------- PUBLISH TRUST LIST (XML) ---------------");
    mLog.debug(
      "Calling publishTrustListXML endpoint -> publishing trust list from XML data!"
    );
    mLog.info("Trust Scheme Name: '" + schemeName + "'");
    mLog.info("Data: '" + data + "'");

    try {
      buildManager(mApiVersion);
      //String signed = mPublicationManager.signTrustList(data);
      mPublicationManager.publishTrustList(schemeName, data);
      X509Certificate cert = mPublicationManager.extractSignature(data);
      mLog.debug(cert);
      mLog.debug(Arrays.toString(cert.getEncoded()));
      String certEncoded = DigestUtils.sha256Hex(cert.getEncoded());

      JsonTrustListPublication pub = new JsonTrustListPublication();
      pub.setUrl(
        "https://tspa.trust-scheme.de/tspa_train_domain/api/v1/scheme/" + schemeName
      );
      JsonDaneCertificate dane = new JsonDaneCertificate();
      dane.setData(certEncoded.toUpperCase());
      dane.setUsage("dane-ee");
      dane.setSelector("cert");
      dane.setMatching("sha256");

      pub.setCertificate(dane);

      if (!pub.verify()) {
        throw new IOException();
      }

      Gson gson = new Gson();
      String zoneData = gson.toJson(pub);
      mZoneManager.publishTrustList(schemeName, zoneData);
    } catch (UnknownInterfaceVersion unknownInterfaceVersion) {
      mLog.error(unknownInterfaceVersion.getMessage(), unknownInterfaceVersion);
      return Response.serverError().entity("Failed to build API interface").build();
    } catch (
      IOException
      | SAXException
      | XPathExpressionException
      | CertificateEncodingException
      | ParserConfigurationException e
    ) {
      mLog.error(e.getMessage(), e);
      return Response
        .serverError()
        .entity("Failed to publish trust list! " + e.getMessage())
        .build();
    } catch (InvalidStatusCodeException e) {
      mLog.error(e.getMessage(), e);
      return Response.status(e.getCode()).entity(e.getCustomErrorMessage()).build();
    } catch (CertificateException e) {
      e.printStackTrace();
    }

    return Response.ok().build();
  }

  @Path("/{scheme_name}/trust-list")
  @Consumes(MediaType.TEXT_PLAIN)
  @PUT
  public Response publishTrustListUrl(
    @PathParam("scheme_name") String schemeName,
    String url
  ) {
    mLog.info("--------------- PUBLISH TRUST LIST (URL) ---------------");
    mLog.debug("calling publishTrustListUrl endpoint");
    mLog.info("Trust Scheme Name: '" + schemeName + "'");
    mLog.info("URL: '" + url + "'");

    try {
      buildManager(mApiVersion);
      mZoneManager.publishTrustListFromUrl(schemeName, url);
    } catch (UnknownInterfaceVersion unknownInterfaceVersion) {
      mLog.error(unknownInterfaceVersion.getMessage(), unknownInterfaceVersion);
      return Response.serverError().entity("Failed to build API interface").build();
    } catch (IOException e) {
      mLog.error(e.getMessage(), e);
      return Response.serverError().entity("Failed to publish trust list").build();
    } catch (InvalidStatusCodeException e) {
      mLog.error(e.getMessage(), e);
      return Response.status(e.getCode()).entity("Coudln't publish trust list").build();
    }

    return Response.ok().build();
  }

  @Path("/{scheme_name}/trust-list")
  @DELETE
  public Response deleteTrustList(@PathParam("scheme_name") String schemeName) {
    mLog.info("--------------- DELETE TRUST LIST ---------------");
    mLog.debug("calling deleteTrustList endpoint");
    mLog.info("Trust Scheme Name: '" + schemeName + "'");

    try {
      buildManager(mApiVersion);
      mZoneManager.deleteTrustList(schemeName);
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
      switch (e.getCode()) {
        case 404:
          return Response
            .status(e.getCode())
            .entity(
              e.getCode() +
              ": There exists No trust list with the given trust scheme parameter"
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

  //@RolesAllowed("valet")
  @Path("/{scheme_name}/schemes")
  @Consumes(MediaType.APPLICATION_JSON)
  @PUT
  public Response publishSchemeJson(
    @PathParam("scheme_name") String schemeName,
    String data
  ) {
    mLog.info("--------------- PUBLISH TRUST SCHEME (JSON) ---------------");
    mLog.debug("calling publishSchemeJson endpoint -> publishing scheme from JSON data!");
    mLog.info("Trust Scheme Name: '" + schemeName + "'");
    mLog.info("Data: '" + data + "'");

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

  @Path("/{scheme_name}/schemes")
  @Consumes(MediaType.TEXT_PLAIN)
  @PUT
  public Response publishSchemeUrl(
    @PathParam("scheme_name") String schemeName,
    String url
  ) {
    mLog.info("--------------- PUBLISH TRUST SCHEME (URL) ---------------");
    mLog.debug("calling publishSchemeUrl endpoing -> creating scheme from URL");
    mLog.info("Trust Scheme Name: '" + schemeName + "'");
    mLog.info("URL: '" + url + "'");

    try {
      buildManager(mApiVersion);
      mZoneManager.publishTrustSchemes(schemeName, url);
      mPublicationManager.publishTrustList(schemeName, url);
    } catch (UnknownInterfaceVersion unknownInterfaceVersion) {
      mLog.error(unknownInterfaceVersion.getMessage(), unknownInterfaceVersion);
      return Response.serverError().entity("Failed to build API interface").build();
    } catch (InvalidStatusCodeException e) {
      mLog.error(e.getMessage(), e);
      return Response.status(e.getCode()).entity("Coudln't publish trust scheme").build();
    } catch (IOException e) {
      mLog.error(e.getMessage(), e);
      return Response
        .serverError()
        .entity("Failed to publish trust scheme " + schemeName)
        .build();
    }

    return Response.ok().build();
  }

  @Path("/{scheme_name}/schemes")
  @DELETE
  public Response deleteScheme(@PathParam("scheme_name") String schemeName) {
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
}
