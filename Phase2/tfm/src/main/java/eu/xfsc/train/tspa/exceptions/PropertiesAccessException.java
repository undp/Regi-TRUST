package eu.xfsc.train.tspa.exceptions;

@SuppressWarnings("serial")
public class PropertiesAccessException extends RuntimeException{
	
	public PropertiesAccessException(String missingTemplatename)
	{
		super(missingTemplatename);
	}

}
