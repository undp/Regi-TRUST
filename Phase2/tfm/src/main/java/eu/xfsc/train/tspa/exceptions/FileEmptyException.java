package eu.xfsc.train.tspa.exceptions;

public class FileEmptyException extends RuntimeException{


	public  FileEmptyException(String filename) {
		super(filename + " is Empty !!!!!");
	}

}
