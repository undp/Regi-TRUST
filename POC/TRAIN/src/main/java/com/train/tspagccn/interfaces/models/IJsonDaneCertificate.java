package com.train.tspagccn.interfaces.models;

public interface IJsonDaneCertificate {
  void setUsage(String data);
  String getUsage();

  void setSelector(String data);
  String getSelector();

  void setMatching(String data);
  String getMatching();

  void setData(String data);
  String getData();

  boolean verify();
}
