package com.train.tspagccn.api.v1.models.json;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.train.tspagccn.interfaces.models.IJsonDaneCertificate;
import java.lang.reflect.Type;

public class JsonDaneCertificate implements IJsonDaneCertificate {

  @JsonAdapter(Usage.Serializer.class)
  public enum Usage {
    @SerializedName("pkix-ta")
    PKIXTA("pkix-ta"),
    @SerializedName("pkix-ee")
    PKIXEE("pkix-ee"),
    @SerializedName("dane-ta")
    DANETA("dane-ta"),
    @SerializedName("dane-ee")
    DANEEE("dane-ee"),
    @SerializedName("")
    EMPTY("");

    private String mUsage;

    Usage(String usage) {
      mUsage = usage.toLowerCase();
    }

    String usage() {
      return mUsage;
    }

    public static Usage fromString(String data) {
      for (Usage u : values()) {
        if (u.usage().equalsIgnoreCase(data) == true) {
          return u;
        }
      }
      return null;
    }

    static class Serializer implements JsonSerializer<Usage>, JsonDeserializer<Usage> {

      @Override
      public JsonElement serialize(
        Usage src,
        Type typeOfSrc,
        JsonSerializationContext context
      ) {
        return context.serialize(src.usage());
      }

      @Override
      public Usage deserialize(
        JsonElement json,
        Type typeOfT,
        JsonDeserializationContext context
      ) {
        try {
          return fromString(json.getAsString());
        } catch (JsonParseException e) {
          return EMPTY;
        }
      }
    }
  }

  @JsonAdapter(Selector.Serializer.class)
  public enum Selector {
    @SerializedName("cert")
    CERT("cert"),
    @SerializedName("spki")
    SPKI("spki"),
    @SerializedName("")
    EMPTY("");

    private String mSelector;

    Selector(String selector) {
      mSelector = selector.toLowerCase();
    }

    public String selector() {
      return mSelector;
    }

    public static Selector fromString(String data) {
      for (Selector s : values()) {
        if (s.selector().equalsIgnoreCase(data) == true) {
          return s;
        }
      }
      return null;
    }

    static class Serializer
      implements JsonSerializer<Selector>, JsonDeserializer<Selector> {

      @Override
      public JsonElement serialize(
        Selector src,
        Type typeOfSrc,
        JsonSerializationContext context
      ) {
        return context.serialize(src.selector());
      }

      @Override
      public Selector deserialize(
        JsonElement json,
        Type typeOfT,
        JsonDeserializationContext context
      ) {
        try {
          return fromString(json.getAsString());
        } catch (JsonParseException e) {
          return EMPTY;
        }
      }
    }
  }

  @JsonAdapter(Matching.Serializer.class)
  public enum Matching {
    @SerializedName("full")
    FULL("full"),
    @SerializedName("sha256")
    SHA256("sha256"),
    @SerializedName("sha512")
    SHA512("sha512"),
    @SerializedName("")
    EMPTY("");

    private String mMatching;

    Matching(String matching) {
      mMatching = matching.toLowerCase();
    }

    public String matching() {
      return mMatching;
    }

    public static Matching fromString(String data) {
      for (Matching m : values()) {
        if (m.matching().equalsIgnoreCase(data) == true) {
          return m;
        }
      }
      return null;
    }

    static class Serializer
      implements JsonSerializer<Matching>, JsonDeserializer<Matching> {

      @Override
      public JsonElement serialize(
        Matching src,
        Type typeOfSrc,
        JsonSerializationContext context
      ) {
        return context.serialize(src.matching());
      }

      @Override
      public Matching deserialize(
        JsonElement json,
        Type typeOfT,
        JsonDeserializationContext context
      ) {
        try {
          return fromString(json.getAsString());
        } catch (JsonParseException e) {
          return EMPTY;
        }
      }
    }
  }

  // Optional string for the usage field of the DANE record
  // Can either be:
  // *) "pkix-ta"
  // *) "pkix-ee"
  // *) "dane-ta"
  // *) "dane-ee"
  // The field can be missing. In this case "dane-ee" is assumed.
  @SerializedName("usage")
  protected Usage mUsage = Usage.PKIXTA;

  // Optional string for the selector field of the DANE record
  // Can either be:
  // *) "cert"
  // *) "spki"
  // The field can be missing. In this case "spki" is assumed.
  @SerializedName("selector")
  protected Selector mSelector = Selector.SPKI;

  // Optional string for the matching type field of the DANE record
  // Can either be:
  // *) "full"
  // *) "sha256"
  // *) "sha512"
  // The field can be missing. In this case "sha256" is assumed.
  @SerializedName("matching")
  protected Matching mMatching = Matching.SHA256;

  // String containing the data of the record according to the other fields
  @SerializedName("data")
  protected String mData;

  @Override
  public void setUsage(String data) {
    mUsage = Usage.fromString(data);
  }

  @Override
  public String getUsage() {
    return mUsage.usage();
  }

  @Override
  public void setSelector(String data) {
    mSelector = Selector.fromString(data);
  }

  @Override
  public String getSelector() {
    return mSelector.selector();
  }

  @Override
  public void setMatching(String data) {
    mMatching = Matching.fromString(data);
  }

  @Override
  public String getMatching() {
    return mMatching.matching();
  }

  @Override
  public void setData(String data) {
    mData = data;
  }

  @Override
  public String getData() {
    return mData;
  }

  @Override
  public boolean verify() {
    if (mData.isEmpty()) {
      return false;
    }
    return true;
  }
}
