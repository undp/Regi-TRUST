package eu.xfsc.train.tspa.jwt;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.jwt.JwtKeywords;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public class JwtVerifiableCredential extends JwtWrappingObject<VerifiableCredential> {

	public JwtVerifiableCredential(JWTClaimsSet payload, VerifiableCredential payloadObject, JWSObject jwsObject, String compactSerialization) {

		super(payload, payloadObject, jwsObject, compactSerialization);
	}

	/*
	 * Factory methods
	 */

	public static JwtVerifiableCredential fromCompactSerialization(String compactSerialization) throws ParseException {

		SignedJWT signedJWT = SignedJWT.parse(compactSerialization);

		JWTClaimsSet jwtPayload = signedJWT.getJWTClaimsSet();
		Map<String, Object> jsonObject = (Map<String, Object>) jwtPayload.getClaims().get(JwtKeywords.JWT_CLAIM_VC);
		if (jsonObject == null) return null;

		VerifiableCredential payloadVerifiableCredential = VerifiableCredential.fromJsonObject(new LinkedHashMap<>(jsonObject));

		return new JwtVerifiableCredential(jwtPayload, payloadVerifiableCredential, signedJWT, compactSerialization);
	}

}
