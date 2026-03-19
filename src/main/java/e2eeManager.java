
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.*;

public class e2eeManager {
    private KeyPair keyPair;
    private SecretKey sharedSecret;

    public e2eeManager() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1")); // remember Signal uses this curve <-- mention it in report
        this.keyPair = kpg.generateKeyPair();
    }
     public byte[] getPublicKeyBytes() {
        return keyPair.getPublic().getEncoded();
    }
    
    public void deriveSharedSecret(byte[] peerPubKeyBytes) throws Exception {
        KeyFactory kf = KeyFactory.getInstance("EC");
        PublicKey peerKey = kf.generatePublic(new X509EncodedKeySpec(peerPubKeyBytes));
        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(keyPair.getPrivate());
        ka.doPhase(peerKey, true);
        byte[] raw = ka.generateSecret();
        byte[] keyBytes = MessageDigest.getInstance("SHA-256").digest(raw);
        this.sharedSecret = new SecretKeySpec(keyBytes, "AES");
    }
     
    
}
