package com.train.tspagccn.api.v1.factory.models;

import com.train.tspagccn.api.v1.models.json.JsonDaneCertificate;
import com.train.tspagccn.interfaces.models.IJsonDaneCertificate;

public class JsonDaneCertificateFactory {

  public static IJsonDaneCertificate newJsonDaneCertificate() {
    return new JsonDaneCertificate();
  }
}
