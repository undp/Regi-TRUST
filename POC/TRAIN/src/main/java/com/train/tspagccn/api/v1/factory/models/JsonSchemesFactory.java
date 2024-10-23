package com.train.tspagccn.api.v1.factory.models;

import com.train.tspagccn.api.v1.models.json.JsonSchemes;
import com.train.tspagccn.interfaces.models.IJsonSchemes;

public class JsonSchemesFactory {

  public static IJsonSchemes newJsonSchemes() {
    return new JsonSchemes();
  }
}
