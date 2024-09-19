package com.train.tspagccn.services;

import com.train.tspagccn.api.VersionFactory;
import com.train.tspagccn.api.exceptions.UnknownInterfaceVersion;
import com.train.tspagccn.interfaces.publication.IPublicationManager;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Path("/{api_version}")
public class QueryService {
  @PathParam("api_version")
  private String mApiVersion;

  Log mLog = LogFactory.getLog(QueryService.class);

  private PropertiesConfiguration mConfig = null;

  public QueryService() throws ConfigurationException {
    mConfig = new PropertiesConfiguration("config.properties");
  }

  @Path("/scheme/{scheme_name}")
  @Produces(MediaType.TEXT_XML)
  @GET
  public Response getTrustScheme(@PathParam("scheme_name") String schemeName) {
    mLog.info("--------------- QUERY TRUST SCHEME ---------------");
    mLog.info("Scheme Name: '" + schemeName + "'");

    IPublicationManager publicationManager;

    try {
      publicationManager = VersionFactory.makePublicationManagerFactory(mApiVersion);
      publicationManager.setConfiguration(mConfig);
    } catch (UnknownInterfaceVersion unknownInterfaceVersion) {
      mLog.error(unknownInterfaceVersion.getMessage(), unknownInterfaceVersion);
      return Response.serverError().entity("Unknown interface version").build();
    }

    String data = null;
    try {
      data = publicationManager.getTrustScheme(schemeName);
    } catch (IOException e) {
      mLog.error(e.getMessage(), e);
      return Response
        .status(Response.Status.NOT_FOUND)
        .entity("The scheme claim is not published within this TSPA")
        .build();
    }
    return Response.ok().entity(data).build();
  }
}
