package com.train.tspagccn.services;

import java.io.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/{api_version}")
public class LogServices {
  @PathParam("api_version")
  public String mApiVersion;

  @GET
  @Path("/log")
  public Response getLog() throws IOException {
    Response rsp = null;

    File logFile = new File("log/logger_tspa.log");
    StringBuilder logContentBuilder = new StringBuilder();
    try (FileInputStream logFileStream = new FileInputStream(logFile)) {
      int content;
      while ((content = logFileStream.read()) != -1) {
        // convert to char and display it
        if (content == '\n') logContentBuilder.append(
          "<br/>"
        ); else logContentBuilder.append((char) content);
      }

      rsp = Response.ok(logContentBuilder.toString()).build();
    }
    return rsp;
  }
}
