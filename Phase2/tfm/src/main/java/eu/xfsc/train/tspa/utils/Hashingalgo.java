package eu.xfsc.train.tspa.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import eu.xfsc.train.tspa.exceptions.PropertiesAccessException;
import io.ipfs.multihash.Multihash;
import io.ipfs.multihash.Multihash.Type;

/**
 * Additional algo. for Hash can be configurable here
 */
public class Hashingalgo {

	
	private static final String SHA_1 = "SHA-1";
	private static final String SHA2_256 = "SHA-256";
	private static final String SHA2_512 = "SHA-512";
	private static final String SHA3_256 = "SHA3-256";

	
	private static final List<String> names= Arrays.asList("sha-1","sha2-256","sha2-512","sha3-256");

	/*
	private enum HashAlgorithm {

		SHA1("sha1", "SHA-1"), SHA256("sha2_256", "SHA-256"), SHA512("sha2_512", "SHA-512"),
		SHA3_256("sha3_256", "SHA3-256");

		private final String type;
		private final String algo;

		HashAlgorithm(String type, String algo) {
			this.type = type;
			this.algo = algo;

		}

		private String getType() {
			return type;
		}

		private String getAlgo() {
			return algo;
		}

		private static List<HashAlgorithm> names = new ArrayList<HashAlgorithm>();

		static {
			for (HashAlgorithm t : HashAlgorithm.values()) {
				names.add(t);
			}
		}

		private static HashAlgorithm hashAlgorithmValidation(String serchingAlgo) {

			for (HashAlgorithm a : names) {
				if (a.getType().compareTo(serchingAlgo) == 0) {
					return a;
				}
			}
			return null;

		}
	}

	public static String TrustlistHashing(byte[] data, String type) throws PropertiesAccessException {
		HashAlgorithm matchAlgorithm = HashAlgorithm.hashAlgorithmValidation(type);
		if (matchAlgorithm == null)
			throw new PropertiesAccessException("Algo. not avaliable");

		return byteHasher(data, matchAlgorithm.getType(), matchAlgorithm.getAlgo());

	}

	private static String byteHasher(byte[] data, String type, String algo) {

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(algo);
		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();
		}

		byte[] hashBytes = md.digest(data);

		Multihash hasing = new Multihash(Multihash.Type.valueOf(type), hashBytes);
		return hasing.toHex();

	}*/

	
	public static String TrustlistHashing(byte[] data, String algo) throws PropertiesAccessException {

		if (algo.compareTo("sha-1") == 0) {
			return SHA_byteHasher(data, Multihash.Type.sha1, SHA_1);
		} else if (algo.compareTo("sha2-256") == 0) {
			return SHA_byteHasher(data, Multihash.Type.sha2_256, SHA2_256);
		} else if (algo.compareTo("sha2-512") == 0) {
			return SHA_byteHasher(data, Multihash.Type.sha2_512, SHA2_512);
		}else if (algo.compareTo("sha3-256") == 0) {
			return SHA_byteHasher(data, Multihash.Type.sha3_256, SHA3_256);
		}

		throw new PropertiesAccessException("Algo. not avaliable");
	}
	

	/**
	 * @param data: desired content
	 * @param type: Multihash Type : reference 'https://github.com/multiformats/java-multihash/blob/master/src/main/java/io/ipfs/multihash/Multihash.java'
	 * @param algo: Algo. for create Digest 
	 * @return Multihash base58 as String
	 */
	private static String SHA_byteHasher(byte[] data, Type type, String algo) {

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(algo);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		byte[] hashBytes = md.digest(data);

		Multihash hasing = new Multihash(type, hashBytes);
		return hasing.toBase58();

	}

	
	private static boolean hashAlgorithmValidation(String serchingAlgo) {
		return names.contains(serchingAlgo) ? true : false;
	}

}
