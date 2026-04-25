
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.NamedParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;

public class E2eeManager {
    private KeyPair keyPair;
    private SecretKey sharedSecret;

    // encrypt function https://medium.com/@pravallikayakkala123/understanding-aes-encryption-and-aes-gcm-mode-an-in-depth-exploration-using-java-e03be85a3faa


  public E2eeManager() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("XDH");
        kpg.initialize(NamedParameterSpec.X25519); 
        this.keyPair = kpg.generateKeyPair();
    }
     public byte[] getPublicKeyBytes() {
        return keyPair.getPublic().getEncoded();
    }
    
    public void deriveSharedSecret(byte[] peerPubKeyBytes) throws Exception {
        KeyFactory kF = KeyFactory.getInstance("XDH");
        PublicKey peerPubKey = kF.generatePublic(new X509EncodedKeySpec(peerPubKeyBytes));

        KeyAgreement kA = KeyAgreement.getInstance("XDH");
        kA.init(keyPair.getPrivate());
        kA.doPhase(peerPubKey, true);

        byte[] rawSecret = kA.generateSecret();
        this.sharedSecret = deriveAESKEY(rawSecret);
    }
    private SecretKey deriveAESKEY(byte[] rawSecret) throws Exception{
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(new HKDFParameters(rawSecret, null, "phoebe-E2ee".getBytes(StandardCharsets.UTF_8)));
        byte [] keyBytes = new byte[32];
        return new SecretKeySpec(keyBytes,"AES");
    }

    public byte[] encrypt(String plaintext) throws Exception{
        byte[] iv = new byte[12]; 
        new SecureRandom().nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, sharedSecret, new GCMParameterSpec(128,iv));

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        byte[] attachedMsg = new byte[iv.length + ciphertext.length];

        System.arraycopy(iv, 0, attachedMsg, 0, iv.length);
        System.arraycopy(ciphertext, 0, attachedMsg, iv.length, ciphertext.length);
        return attachedMsg;
    }

    public String decrypt(byte[] encryptedData) throws Exception{
        byte[] iv = new byte[12];
        System.arraycopy(encryptedData, 0, iv, 0, 12);
        byte[] ciphertext = new byte[encryptedData.length - 12];
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, sharedSecret, new GCMParameterSpec(128,iv));
        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext, StandardCharsets.UTF_8);
    }


}
