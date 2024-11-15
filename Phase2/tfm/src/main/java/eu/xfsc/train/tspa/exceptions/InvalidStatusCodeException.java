package eu.xfsc.train.tspa.exceptions;

import java.io.Serializable;

public class InvalidStatusCodeException extends Exception implements Serializable {

	 private int mCode = 0;

	  public InvalidStatusCodeException(String body, int code) {
	    super(body);
	    mCode = code;
	  }

	  public int getCode() {
	    return mCode;
	  }

	  public String getCustomErrorMessage() {
	    if (mCode == 500) {
	      return "ZoneManager is currently not reachable!";
	    }
	    return  getMessage();
	  }
}
