package com.train.tspagccn.api.v1.models.json;

import com.google.gson.annotations.SerializedName;
import com.train.tspagccn.interfaces.models.IJsonDaneCertificate;
import com.train.tspagccn.interfaces.models.IJsonTrustListPublication;
import java.util.ArrayList;
import java.util.List;

public class JsonTrustListPublication implements IJsonTrustListPublication {
  @SerializedName("url")
  private String mUrl;

  @SerializedName("certificate")
  private List<JsonDaneCertificate> mCertificates = new ArrayList<>();

  @Override
  public void setUrl(String url) {
    mUrl = url;
  }

  @Override
  public String getUrl() {
    return mUrl;
  }

  @Override
  public void setCertificate(JsonDaneCertificate cert) {
    mCertificates.add(cert);
  }

  @Override
  public int getCertificateCount() {
    return mCertificates.size();
  }

  @Override
  public JsonDaneCertificate getCertificate(int n) {
    return mCertificates.get(n);
  }

  @Override
  public List<JsonDaneCertificate> getCertificates() {
    return mCertificates;
  }

  @Override
  public boolean verify() {
    for (JsonDaneCertificate c : mCertificates) {
      if (c.verify() == false) return false;
    }
    return true;
  }
}
