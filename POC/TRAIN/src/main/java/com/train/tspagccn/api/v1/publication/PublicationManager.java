package com.train.tspagccn.api.v1.publication;

import com.train.tspagccn.interfaces.publication.IPublicationManager;
import java.io.*;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PublicationManager implements IPublicationManager {
  private String mPath = null;

  private Log mLog = LogFactory.getLog(PublicationManager.class);

  public void setConfiguration(PropertiesConfiguration config) {
    mPath = config.getString("list.path");

    File store = new File(mPath);
    if (!store.exists()) {
      mLog.debug("Store does not exist, creating at " + mPath);
      store.mkdirs();
    }
  }

  @Override
  public void publishTrustList(String trustListName, String trustListData)
    throws IOException {
    if (isTrustListExisting(trustListName)) {
      mLog.info("TrustList already exists. UPDATE.");
    } else {
      mLog.info("TrustList doesn't exist. CREATE");
    }

    PrintWriter file = new PrintWriter(mPath + "/" + trustListName);
    file.write(trustListData);
    file.close();
  }

  @Override
  public String getTrustScheme(String trustListName) throws IOException {
    if (!isTrustListExisting(trustListName)) {
      return null;
    }

    StringBuilder content = new StringBuilder();

    try (
      BufferedReader reader = new BufferedReader(
        new FileReader(mPath + "/" + trustListName)
      )
    ) {
      String line = reader.readLine();

      while (line != null) {
        content.append(line);
        content.append(System.lineSeparator());
        line = reader.readLine();
      }
    }

    return content.toString();
  }

  @Override
  public String signTrustList(String data) throws URISyntaxException {
    mLog.fatal("signTrustList() not implemented." + data);
    return null;
  }

  @Override
  public X509Certificate extractSignature(String data)
    throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, CertificateException {
    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = builder.parse(new InputSource(new StringReader(data)));

    doc.getDocumentElement().normalize();

    XPath xpath = XPathFactory.newInstance().newXPath();
    String expression =
      "TrustServiceStatusList/TrustServiceProviderList/TrustServiceProvider/TSPServices/TSPService/ServiceInformation/ServiceDigitalIdentity/DigitalId/X509Certificate";

    NodeList node = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODE);

    mLog.debug("Count: " + node.getLength());

    for (int i = 0; i < node.getLength(); ++i) {
      mLog.debug("Certificate: " + convertToX509Cert(node.item(i).getTextContent()));
    }

    Node certNode = node.item(0);
    mLog.debug("Raw: " + certNode.getTextContent());
    mLog.debug(
      "Certificate: " + convertToX509Cert(certNode.getTextContent()).getPublicKey()
    );
    X509Certificate cert = convertToX509Cert(certNode.getTextContent());
    return cert;
  }

  public static X509Certificate convertToX509Cert(String certificateString)
    throws CertificateException {
    X509Certificate certificate = null;
    CertificateFactory cf = null;
    try {
      if (certificateString != null && !certificateString.trim().isEmpty()) {
        certificateString =
          certificateString
            .replace("-----BEGIN CERTIFICATE-----\n", "")
            .replace("-----END CERTIFICATE-----", ""); // NEED FOR PEM FORMAT CERT STRING
        byte[] certificateData = Base64.decodeBase64(certificateString);
        cf = CertificateFactory.getInstance("X509");
        certificate =
          (X509Certificate) cf.generateCertificate(
            new ByteArrayInputStream(certificateData)
          );
      }
    } catch (CertificateException e) {
      throw new CertificateException(e);
    }
    return certificate;
  }

  private boolean isTrustListExisting(String name) {
    File folder = new File(mPath);
    File[] files = folder.listFiles();

    if (files == null) {
      mLog.debug("Nothing found in store folder: " + folder);
      return false;
    }

    for (File f : files) {
      if (f.isFile() && f.getName().compareTo(name) == 0) {
        mLog.debug("Found scheme, it exists!");
        return true;
      }
    }

    return false;
  }
}
