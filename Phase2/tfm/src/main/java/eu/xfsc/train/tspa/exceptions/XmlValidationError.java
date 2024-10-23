package eu.xfsc.train.tspa.exceptions;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 *   Error handler for the XML validation.
 */
public class XmlValidationError implements ErrorHandler {
	
	private List<SAXParseException> exceptions;

    public XmlValidationError() {
        this.exceptions = new ArrayList<>();
    }

    public List<SAXParseException> getExceptions() {
        return exceptions;
    }

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		// TODO Auto-generated method stub
		exceptions.add(exception);
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		// TODO Auto-generated method stub
		exceptions.add(exception);
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		// TODO Auto-generated method stub
		exceptions.add(exception);
	}

}
