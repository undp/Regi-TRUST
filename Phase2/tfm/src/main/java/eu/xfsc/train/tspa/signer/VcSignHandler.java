package eu.xfsc.train.tspa.signer;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.codec.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.http.media.MediaType;
import com.danubetech.keyformats.crypto.PrivateKeySigner;
import com.danubetech.keyformats.crypto.PrivateKeySignerFactory;
import com.danubetech.keyformats.jose.JWK;
import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.google.gson.JsonObject;
import com.nimbusds.jose.JOSEException;

import eu.xfsc.train.tspa.exceptions.PropertiesAccessException;
import eu.xfsc.train.tspa.jwt.JwtVerifiableCredential;
import eu.xfsc.train.tspa.jwt.ToJwtConverter;
import eu.xfsc.train.tspa.utils.VCUtil;
import foundation.identity.jsonld.ConfigurableDocumentLoader;
import foundation.identity.jsonld.JsonLDException;
import info.weboftrust.ldsignatures.LdProof;
import info.weboftrust.ldsignatures.jsonld.LDSecurityKeywords;
import info.weboftrust.ldsignatures.signer.LdSigner;
import info.weboftrust.ldsignatures.signer.LdSignerRegistry;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.*;


@Component
public class VcSignHandler {

	private static final Logger log = LoggerFactory.getLogger(VcSignHandler.class);

	private static final String CONST_WK_STRING = "well-known";
	private static final String CONST_VC_STRING = "vc";
	public static final okhttp3.MediaType JSON = okhttp3.MediaType.parse("application/json");
	private OkHttpClient Client = new OkHttpClient();
	public static final String SET_SIGNER_INTERNAL = "INTERNAL";
	public static final String SET_SIGNER_TSA = "TSA";
	
	@Value("classpath:Vault/PrivateKey/WkJWKPrivateKey")
	private Resource jwkForWK;
	@Value("classpath:Vault/PrivateKey/WkJWKPrivateKey")
	private Resource jwkForVC;
	@Value("${well-known.proof.signaturesuit}")
	private String wk_SignatureSuit;
	@Value("${well-known.signature.algo}")
	private String wk_SignAlgo;
	@Value("${well-known.jwt.kid}")
	private String wk_KID;
	@Value("${trustlist.vc.proof.signaturesuit}")
	private String vc_SignatureSuit;
	@Value("${trustlist.vc.signature.algo}")
	private String vc_SignAlgo;
	@Value("${trustlist.vc.jwt.kid}")
	private String vc_KID;
	@Value("${trustlist.vc.signer.url}")
	private String externalsigner_url;
	@Value("${trustlist.vc.signer.key}")
	private String tsasigner_key;
	@Value("${trustlist.vc.signer.namespace}")
	private String tsasigner_namespace;
	@Value("${trustlist.vc.signer.group}")
	private String tsasigner_group;
	@Value("${well-known.context.url1}")
	private String wk_context_url1;
	

	/**
	 * Create Proof for the Payload.
	 * 
	 * @param vcString : Desired payload as string
	 * @param verificationMethod: verification did
	 * @param purpose: const. String ("well-known"/"vc");
	 * @return Verifiable credential with LDProof as strins
	 * @throws DecoderException
	 * @throws GeneralSecurityException
	 * @throws JsonLDException
	 * @throws IOException
	 * @throws PropertiesAccessException
	 */
	public String creatLDProofString(String vcString, String verificationMethod, String purpose)
			throws DecoderException, GeneralSecurityException, JsonLDException, IOException, PropertiesAccessException {

		VerifiableCredential vcPayload = VerifiableCredential.fromJson(vcString);
		
		URI additionalContextUrl = URI.create(wk_context_url1);

		// Check local cache.
		Map<URI, JsonDocument> dlCache = ((ConfigurableDocumentLoader) vcPayload.getDocumentLoader()).getLocalCache();
		if (!dlCache.containsKey(additionalContextUrl)) {
			// Context url absent then add in local cache
			try {
				dlCache.putIfAbsent(additionalContextUrl, JsonDocument.of(MediaType.JSON_LD,
						VcSignHandler.class.getResourceAsStream("well-known.jsonld")));
			} catch (JsonLdError e) {
				log.error("CreatLDProof; error during the loading context because : ", e);
			}
		}
		
		//Set the properties for creating proof (Signaturesuits, algo, JWK ...)
		Parameter parameter = setParameterProof(purpose);

		String jwkAsString = parameter.getJwk();
		String signatureSuitName = parameter.getSignatureSuitString();
		String alg = parameter.getAlgoName(); // getAlgFromSuit(signatureSuitName);
		if (jwkAsString == null || signatureSuitName == null || alg == null) {
			log.error("Purpose for the Signature is missing/wrong : Select either 'well-known' or 'vc'");
			throw new NullPointerException(
					"Purpose for the Signature is missing/wrong: Select either 'well-known' or 'vc'");
		}

		JWK privatekeyJWK = JWK.fromJson(jwkAsString);

		PrivateKeySigner<?> signerkey = PrivateKeySignerFactory.privateKeySignerForKey(privatekeyJWK, alg);

		LdSigner<?> signer = LdSignerRegistry.getLdSignerBySignatureSuiteTerm(signatureSuitName);

		ZoneId zoneId = ZoneId.of("Europe/Berlin");
		ZonedDateTime offseTime = ZonedDateTime.now(zoneId).plusHours(1);

		signer.setCreated(Date.from(offseTime.toInstant()));
		signer.setProofPurpose(LDSecurityKeywords.JSONLD_TERM_ASSERTIONMETHOD);
		signer.setVerificationMethod(URI.create(verificationMethod));

		signer.setSigner(signerkey);

		LdProof proof = signer.sign(vcPayload);
		return vcPayload.toJson(true);
	}

	/**
	 * Create JWT for payload
	 * 
	 * @param vcString :Payload as String
	 * @param purpose : const. String ("well-known"/"vc");
	 * @return JWT as string.
	 * @throws DecoderException
	 * @throws IOException
	 * @throws PropertiesAccessException
	 */
	public String creatJWTString(String vcString,String purpose )
			throws DecoderException, IOException, PropertiesAccessException {

		VerifiableCredential verifiableCredential = VerifiableCredential.fromJson(vcString);
		JwtVerifiableCredential jwtVerifiableCredential = ToJwtConverter
				.toJwtVerifiableCredential(verifiableCredential);

		//Set properties (JWK, algo, kid(Still form configuration file))
		Parameter parameter = setParameterJWT(purpose);

		String jwkAsString = parameter.getJwk();
		String alg = parameter.getAlgoName(); 
		String kid = parameter.getKId();
		if (jwkAsString == null || alg == null || kid == null ) {
			log.error("Purpose for the Signature is missing/wrong : Select either 'well-known' or 'vc'");
			throw new NullPointerException(
					"Purpose for the Signature is missing/wrong: Select either 'well-known' or 'vc'");
		}
		JWK privatekeyJWK = JWK.fromJson(jwkAsString);

		PrivateKeySigner<?> signerkey = PrivateKeySignerFactory.privateKeySignerForKey(privatekeyJWK, alg);

		String jwtString=null;
		try {
			jwtString = jwtVerifiableCredential.sign_with_AllAlgorithems(signerkey, alg, kid);
		} catch (JOSEException e) {
			log.error("CreateJWT; problem : ",e);
		}
		return jwtString;
	}

	
	/**
	 * @param purpose-->Purpose of signature for the Wellknown Or VC --> Pass the constant String either "well-known" or "vc"
	 * @return DTO Parameter for with needed properties for creating Proof.
	 * @throws IOException
	 * @throws PropertiesAccessException
	 */
	private Parameter setParameterProof(String purpose) throws IOException, PropertiesAccessException {
		String jwkKey = null;
		String signatureSuitString = null;
		String algo = null;
		if (purpose != null)
			if (purpose.compareTo(CONST_WK_STRING) == 0) {
				jwkKey = VCUtil.resource_to_String(jwkForWK);
				algo = wk_SignAlgo;
				signatureSuitString = wk_SignatureSuit;			
			} else if (purpose.compareTo(CONST_VC_STRING) == 0) {
				jwkKey = VCUtil.resource_to_String(jwkForVC);
				algo = vc_SignAlgo;
				signatureSuitString = vc_SignatureSuit;
			}
		return new Parameter(jwkKey, algo, signatureSuitString, null);
	}
	
	/**
	 * @param--> Purpose of signature for the Wellknown Or VC --> Pass the constant String either "well-known" or "vc"
	 * @return  DTO Parameter for with needed properties for creating JWT.
	 * @throws IOException
	 * @throws PropertiesAccessException
	 */
	private Parameter setParameterJWT(String purpose) throws IOException, PropertiesAccessException {
		String jwkKey = null;
		String algo = null;
		String kid = null;
		if (purpose != null)
			if (purpose.compareTo(CONST_WK_STRING) == 0) {
				jwkKey = VCUtil.resource_to_String(jwkForWK);
				algo = wk_SignAlgo;
				kid=wk_KID;
			} else if (purpose.compareTo(CONST_VC_STRING) == 0) {
				jwkKey = VCUtil.resource_to_String(jwkForVC);
				algo = vc_SignAlgo;
				kid = vc_KID;
			}
		return new Parameter(jwkKey, algo, null, kid);
	}


	//-->DAO class
	@Getter
	@Setter
	private class Parameter {
		private String jwk;
		private String algoName;
		private String signatureSuitString;
		private String kId;
		

		private Parameter(String jwk, String algoName, String signatureSuitString, String kId) {
			super();
			this.jwk = jwk;
			this.algoName = algoName;
			this.signatureSuitString = signatureSuitString;
			this.kId=kId;
		}
	}
	

	public String TSASigner(JsonObject vcobject) {

		String jsonResponse = null;

		vcobject.addProperty("key", tsasigner_key);
		vcobject.addProperty("namespace", tsasigner_namespace);
		vcobject.addProperty("group", tsasigner_group);
        RequestBody requestBody = RequestBody.create(vcobject.toString(), JSON);
        // Build the POST request and call the TSA Signer Instance deployed
        
		Request request = new Request.Builder()
                .url(externalsigner_url)
                .post(requestBody)
                .build();

        // Execute the request
        try (Response response = Client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // Parse and print the JSON response
                jsonResponse = response.body().string();
                log.debug("JSON Response: {}", jsonResponse);
            } else {
            	log.error("Request failed. HTTP Status Code: {} ", response.code());
            }
        } catch (IOException e) {
        	 log.error("Signature request failed:", e);
        }
        return jsonResponse;
	}
}
