package com.train.tspagccn.interfaces.publication;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.xml.sax.SAXException;

public interface IPublicationManager {
  void setConfiguration(PropertiesConfiguration config);
  void publishTrustList(String trustListName, String trustListData) throws IOException;
  String getTrustScheme(String trustListName) throws IOException;
  String signTrustList(String data) throws URISyntaxException;

  X509Certificate extractSignature(String data)
    throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, CertificateException;
}
