import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class RSASignatureUtil {

    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    public static byte[] hashMessage(String message) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(message.getBytes(StandardCharsets.UTF_8));
    }

    public static BigInteger hashToBigInteger(byte[] hash) {
        return new BigInteger(1, hash);
    }

    public static BigInteger signHash(BigInteger hashValue, PrivateKey privateKey) {
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;

        BigInteger d = rsaPrivateKey.getPrivateExponent();
        BigInteger n = rsaPrivateKey.getModulus();

        return hashValue.modPow(d, n);
    }

    public static BigInteger recoverHashFromSignature(BigInteger signature, PublicKey publicKey) {
        RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;

        BigInteger e = rsaPublicKey.getPublicExponent();
        BigInteger n = rsaPublicKey.getModulus();

        return signature.modPow(e, n);
    }

    public static boolean verifySignature(String message, BigInteger signature, PublicKey publicKey) throws Exception {
        byte[] newHash = hashMessage(message);
        BigInteger newHashValue = hashToBigInteger(newHash);

        BigInteger recoveredHash = recoverHashFromSignature(signature, publicKey);

        return newHashValue.equals(recoveredHash);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}