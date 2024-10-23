package eu.xfsc.train.tspa.interfaces;

import java.io.IOException;
import eu.xfsc.train.tspa.exceptions.InvalidStatusCodeException;

public interface IZoneManager {

	public int publishTrustSchemes(String schemeName, String data) throws IOException, InvalidStatusCodeException;

	public int publishDIDUri(String TrustName, String data) throws IOException, InvalidStatusCodeException;

	public int deleteTrustSchemes(String schemeName) throws IOException, InvalidStatusCodeException;

	public int deleteDIDUriRecords(String TrustName) throws IOException, InvalidStatusCodeException;
}
