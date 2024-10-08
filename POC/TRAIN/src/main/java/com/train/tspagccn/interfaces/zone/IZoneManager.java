package com.train.tspagccn.interfaces.zone;

import com.train.tspagccn.api.exceptions.InvalidStatusCodeException;
import java.io.IOException;
import org.apache.commons.configuration.PropertiesConfiguration;

public interface IZoneManager {
  void setConfiguration(PropertiesConfiguration config);

  int publishTrustList(String schemeName, String data)
    throws IOException, InvalidStatusCodeException;

  int deleteTrustList(String schemeName) throws IOException, InvalidStatusCodeException;

  int publishTrustSchemes(String schemeName, String data)
    throws IOException, InvalidStatusCodeException;

  int deleteTrustSchemes(String schemeName)
    throws IOException, InvalidStatusCodeException;

  int publishTrustListFromUrl(String schemeName, String url)
    throws IOException, InvalidStatusCodeException;
}
