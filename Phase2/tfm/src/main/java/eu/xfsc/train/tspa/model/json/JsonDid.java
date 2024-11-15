package eu.xfsc.train.tspa.model.json;

import com.google.gson.annotations.SerializedName;

public class JsonDid {
	
	@SerializedName("did")
	public String mDid;

	public String getmDid() {
		return mDid;
	}

	public void setmDid(String mDid) {
		this.mDid = mDid;
	}
}
