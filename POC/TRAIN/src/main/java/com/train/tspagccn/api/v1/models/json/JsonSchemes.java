package com.train.tspagccn.api.v1.models.json;

import com.google.gson.annotations.SerializedName;
import com.train.tspagccn.interfaces.models.IJsonSchemes;
import java.util.List;

public class JsonSchemes implements IJsonSchemes {
  @SerializedName("schemes")
  public List<String> mSchemes;

  @Override
  public void setSchemes(List<String> data) {
    mSchemes = data;
  }

  @Override
  public List<String> getSchemes() {
    return mSchemes;
  }
}
