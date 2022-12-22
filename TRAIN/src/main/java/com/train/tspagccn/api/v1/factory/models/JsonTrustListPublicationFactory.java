package com.train.tspagccn.api.v1.factory.models;

import com.train.tspagccn.api.v1.models.json.JsonTrustListPublication;
import com.train.tspagccn.interfaces.models.IJsonTrustListPublication;

public class JsonTrustListPublicationFactory {

  public static IJsonTrustListPublication newJsonTrustListPublication() {
    return new JsonTrustListPublication();
  }
}
