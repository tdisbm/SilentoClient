package tasks;

import kraken.extension.task.Task;
import java.security.*;
import java.util.Base64;

public class RSAKeyGenerator extends Task {
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private KeyPairGenerator generator;

    private String serializedPublicKey;
    private String serializedPrivateKey;

    public RSAKeyGenerator(String keySize) {
        try {
            generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(Integer.parseInt(keySize));
        } catch (NoSuchAlgorithmException ignored) {
        }
    }

    @Override
    public void run() {
        KeyPair keyPair = generator.genKeyPair();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
        serializedPublicKey = new String(Base64.getEncoder().encode(publicKey.getEncoded()));
        serializedPrivateKey = new String(Base64.getEncoder().encode(privateKey.getEncoded()));
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getSerializedPublicKey() {
        return serializedPublicKey;
    }

    public String getSerializedPrivateKey() {
        return serializedPrivateKey;
    }
}
