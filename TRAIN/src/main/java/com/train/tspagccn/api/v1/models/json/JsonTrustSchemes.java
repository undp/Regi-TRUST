package com.train.tspagccn.api.v1.models.json;

import com.google.gson.annotations.SerializedName;
import com.train.tspagccn.interfaces.models.IJsonTrustSchemes;
import java.util.List;

public class JsonTrustSchemes implements IJsonTrustSchemes {
  @SerializedName("schemes")
  public List<String> mSchemes;

  @Override
  public void setTrustSchemes(List<String> schemes) {
    mSchemes = schemes;
  }

  @Override
  public List<String> getTrustSchemes() {
    return mSchemes;
  }
}
