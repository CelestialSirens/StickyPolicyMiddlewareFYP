
import javax.crypto.*;
import javax.crypto.spec.*;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;

public class E2eeManager {
    private KeyPair keyPair;
    private SecretKey sharedSecret;

    public E2eeManager() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1")); // remember Signal uses this curve <-- mention it in report
        this.keyPair = kpg.generateKeyPair();
    }
     public byte[] getPublicKeyBytes() {
        return keyPair.getPublic().getEncoded();
    }
    
    public void deriveSharedSecret(byte[] peerPubKeyBytes) throws Exception {
        KeyFactory kF = KeyFactory.getInstance("EC");
        PublicKey peerPubKey = kF.generatePublic(new X509EncodedKeySpec(peerPubKeyBytes));

        KeyAgreement kA = KeyAgreement.getInstance("ECHD");
        kA.init(keyPair.getPrivate());
        kA.doPhase(peerPubKey, true);

        byte[] rawSecret = kA.generateSecret();
        this.sharedSecret = deriveAESKEY(rawSecret);
    }
             private SecretKey deriveAESKEY(byte[] rawSecret) throws Exception{
                    MessageDigest hkfd = MessageDigest.getInstance("HKFD");
                    byte [] keyBytes = hkfd.digest(rawSecret);
                    return new SecretKeySpec(keyBytes,"AES");
            }

     
    // encrypt function https://medium.com/@pravallikayakkala123/understanding-aes-encryption-and-aes-gcm-mode-an-in-depth-exploration-using-java-e03be85a3faa

    public byte[] encrypt(String plaintext) throws Exception{
        byte[] iv = new byte[12]; 
        new SecureRandom().nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM");
        cipher.init(Cipher.ENCRYPT_MODE, sharedSecret, new GCMParameterSpec(128,iv));

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        byte[] attachedMsg = new byte[iv.length + ciphertext.length];

        System.arraycopy(iv, 0, attachedMsg, 0, iv.length);
        System.arraycopy(ciphertext, 0, attachedMsg, iv.length, ciphertext.length);
        return attachedMsg;
    }

    // decrypt function

    public String decrypt(byte[] encryptedData) throws Exception{
        byte[] iv = new byte[12];
    }


}
