package com.train.tspagccn.api.v1.publication;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Provider;
import java.security.Security;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
/*
import iaik.security.provider.IAIK;
import iaik.xml.crypto.XSecProvider;
 */
import org.apache.commons.lang.NotImplementedException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Signer {
  private URI mSchemaURI = new URI(
    "file",
    null,
    "resources/schema/xmldsig-core-schema.xsd",
    null,
    null
  );
  private Provider mProvider = null;
  private XMLSignatureFactory mSignatureFactory = null;

  public Signer() throws URISyntaxException {
    /*
        IAIK.addAsJDK14Provider();
        mProvider = new XSecProvider();
        Security.addProvider(mProvider);
        mSignatureFactory = XMLSignatureFactory.getInstance("DOM", mProvider);

        Provider otherXMLDsigProvider = Security.getProvider("XMLDSig");
        if ( otherXMLDsigProvider != null ) {
            Security.removeProvider(otherXMLDsigProvider.getName());
            Security.addProvider(otherXMLDsigProvider);
        }
         */
  }
  /*
    public  boolean sign(String xmlDocument) throws IOException, SAXException, ParserConfigurationException {
        Document doc = parseSignatureDocument(xmlDocument);

        throw new NotImplementedException();
    }

    private Document parseSignatureDocument(String xmlDocument) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setExpandEntityReferences(false);
        dbf.setValidating(true);
        dbf.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        dbf.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", mSchemaURI.toString());
        dbf.setAttribute("http://apache.org/xml/featurs/validation/schema/normalized-value", Boolean.FALSE);

        return dbf.newDocumentBuilder().parse(xmlDocument);
    }
     */
}
