package util;

import javax.crypto.Cipher;
import java.io.IOException;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


public class RSAEncryptionUtil {
    private static KeyFactory keyFactory;

    static {
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static PublicKey loadPublicKey(String stored) throws GeneralSecurityException, IOException {
        byte[] data = Base64.getDecoder().decode((stored.getBytes()));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        return keyFactory.generatePublic(spec);
    }

    public static PrivateKey loadPrivateKey(String stored)throws GeneralSecurityException, IOException {
        byte[] data = Base64.getDecoder().decode((stored.getBytes()));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        return keyFactory.generatePrivate(spec);
    }

    public static String encrypt(String text, PublicKey key) {
        byte[] cipherText;
        try {
            final Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text.getBytes());
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }

    public static String decrypt(String text, PrivateKey key) {
        byte[] decryptedText = Base64.getDecoder().decode(text);
        try {
            final Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            decryptedText = cipher.doFinal(decryptedText);
            return new String(decryptedText);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return text;
    }
}