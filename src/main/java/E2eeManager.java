
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class E2eeManager {
    private KeyPair keyPair;
    private SecretKey sharedSecret;
     
    // encrypt function https://medium.com/@pravallikayakkala123/understanding-aes-encryption-and-aes-gcm-mode-an-in-depth-exploration-using-java-e03be85a3faa

    //kpg.initialize(new ECGenParameterSpec("secp256r1")); // remember Signal uses this curve <-- mention it in report

  public E2eeManager() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("curve25519")); // remember Signal uses this curve <-- mention it in report
        this.keyPair = kpg.generateKeyPair();
    }
     public byte[] getPublicKeyBytes() {
        return keyPair.getPublic().getEncoded();
    }
    
    public void deriveSharedSecret(byte[] peerPubKeyBytes) throws Exception {
        KeyFactory kF = KeyFactory.getInstance("EC");
        PublicKey peerPubKey = kF.generatePublic(new X509EncodedKeySpec(peerPubKeyBytes));

        KeyAgreement kA = KeyAgreement.getInstance("ECDH");
        kA.init(keyPair.getPrivate());
        kA.doPhase(peerPubKey, true);

        byte[] rawSecret = kA.generateSecret();
        this.sharedSecret = deriveAESKEY(rawSecret);
    }
             private SecretKey deriveAESKEY(byte[] rawSecret) throws Exception{
                    MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                    byte [] keyBytes = sha256.digest(rawSecret);
                    return new SecretKeySpec(keyBytes,"AES");
            }

     
   
    public byte[] encrypt(String plaintext) throws Exception{
        byte[] iv = new byte[12]; 
        new SecureRandom().nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, sharedSecret, new GCMParameterSpec(128,iv));

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));
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
