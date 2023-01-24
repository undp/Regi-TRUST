package com.train.tspagccn.interfaces.models;

import com.train.tspagccn.api.v1.models.json.JsonDaneCertificate;
import java.util.List;

public interface IJsonTrustListPublication {
  void setUrl(String url);
  String getUrl();

  void setCertificate(JsonDaneCertificate cert);
  int getCertificateCount();
  IJsonDaneCertificate getCertificate(int n);
  List<JsonDaneCertificate> getCertificates();

  boolean verify();
}
