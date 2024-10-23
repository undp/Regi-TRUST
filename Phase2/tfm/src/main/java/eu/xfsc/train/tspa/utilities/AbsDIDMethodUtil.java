package eu.xfsc.train.tspa.utilities;

import eu.xfsc.train.tspa.exceptions.PropertiesAccessException;

public abstract class AbsDIDMethodUtil {
	
	 public abstract boolean isWellknownValid(String did);
	 public abstract  boolean isDIDMethodValid(String didMethod) throws PropertiesAccessException;

}
