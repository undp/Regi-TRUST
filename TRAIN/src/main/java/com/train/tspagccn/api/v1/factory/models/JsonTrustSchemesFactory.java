package com.train.tspagccn.api.v1.factory.models;

import com.train.tspagccn.api.v1.models.json.JsonTrustSchemes;
import com.train.tspagccn.interfaces.models.IJsonTrustSchemes;

public class JsonTrustSchemesFactory {

  public static IJsonTrustSchemes newJsonTrustSchemes() {
    return new JsonTrustSchemes();
  }
}
