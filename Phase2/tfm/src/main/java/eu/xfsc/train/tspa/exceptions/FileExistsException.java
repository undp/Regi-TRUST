package eu.xfsc.train.tspa.exceptions;

public class FileExistsException extends RuntimeException {
	public FileExistsException(String exceptionString) {
		super(exceptionString);
	}
}
