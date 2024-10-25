package eu.xfsc.train.tspa.exceptions;

public class FileEmptyException extends RuntimeException{


	public  FileEmptyException(Exception e) {
		super(e.getMessage());
	}

	public  FileEmptyException(String message) {
		super(message);
	}	
}
