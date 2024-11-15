package eu.xfsc.train.tspa.utils;


import io.ipfs.api.IPFS;
import io.ipfs.multiaddr.MultiAddress;

/**
 *  Singleton class for creating only one object of the IPFS.
 */
public class IpfsUtil {
	private static IpfsUtil instance;
    public IPFS ipfs;
    
  
    private IpfsUtil(String api) {
      this.ipfs = new IPFS(new MultiAddress(api));
    }

    // Public method to get the single instance of the class
    public static IpfsUtil getInstance(String apiString) {
        if (instance == null) {
            instance = new IpfsUtil(apiString);
        }
        return instance;
    }

}
