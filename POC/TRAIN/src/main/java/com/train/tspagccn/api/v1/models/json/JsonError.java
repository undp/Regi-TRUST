package com.train.tspagccn.api.v1.models.json;

import com.google.gson.annotations.SerializedName;

public class JsonError {
  @SerializedName("component")
  private String mComponentName;

  @SerializedName("code")
  private Integer mErrorCode;

  @SerializedName("message")
  private String mMessage;
}
