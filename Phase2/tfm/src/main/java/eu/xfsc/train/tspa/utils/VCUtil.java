package eu.xfsc.train.tspa.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component
public class VCUtil {
	

	private static final Logger log = LoggerFactory.getLogger(VCUtil.class);

	
	//--> Read data from desired URL as bytes
	@SuppressWarnings("deprecation")
	public static byte[] readBytefromURL(String urlString) throws IOException  {
		URL url = new URL(urlString);
		 InputStream inputStream = url.openStream();
		 String data = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8); 
		 return data.getBytes();
	}
	
	//--> Additional method for read xml as Document (Optional method)
	public static byte[] fileXMlread(File xmlFile)
			throws ParserConfigurationException, SAXException, IOException, TransformerException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document doc = null;
		builder = factory.newDocumentBuilder();
		doc = builder.parse(xmlFile);

		DOMSource source = new DOMSource(doc);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		StreamResult xmlOutput = new StreamResult(byteArrayOutputStream);
		TransformerFactory factory1 = TransformerFactory.newInstance();
		Transformer transformer = factory1.newTransformer();
		transformer.setOutputProperty("encoding", "UTF-8");
		transformer.setOutputProperty("indent", "no");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(source, xmlOutput);

		byte[] xmlBytes = byteArrayOutputStream.toByteArray();
		return xmlBytes;
	}

	// -->Method for storing the VC in Store.
	public static void vcStore(String pathString, String VC, String filename) throws IOException {

		File storeFile = new File(pathString);
		if (!storeFile.exists()) {
			log.debug("Store does not exist, Creating at {}", pathString);
			storeFile.mkdirs(); // make new directory.
		}

		PrintWriter writeVC = new PrintWriter(pathString + "/" + filename + ".json");
		writeVC.write(VC);
		writeVC.close();
		log.info("Creating / Overwriting file in '{}' with '{}.json'", pathString, filename);
	}

	public static JsonObject resource_to_JsonObject(Resource resource) throws IOException, NullPointerException {

		File file = resource.getFile();
		String vcString = new String(Files.readAllBytes(file.toPath()));
		if (vcString.isEmpty()) {
			log.error("There is no template for VC available in {}", file.getPath());
			throw new NullPointerException("Template " + file.getPath() + " is Empty!");
		}

		JsonObject jsonObjct = (JsonObject) JsonParser.parseString(vcString);

		return jsonObjct;
	}

	public static String resource_to_String(Resource resource) throws IOException {

		File file = resource.getFile();
		String string = new String(Files.readAllBytes(file.toPath()));
		return string;
	}
	
	public static String bytesToHex(byte[] in) {
		final StringBuilder builder = new StringBuilder();
		for (byte b : in) {
			builder.append(String.format("%02x", b));
		}
		return builder.toString();
	}
}
