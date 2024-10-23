package eu.xfsc.train.tspa.model.json;

import com.google.gson.annotations.SerializedName;
import eu.xfsc.train.tspa.interfaces.model.IJsonSchemes;

import java.util.List;

public class JsonSchemes implements IJsonSchemes{

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