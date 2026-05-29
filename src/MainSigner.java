import java.math.BigInteger;
import java.security.KeyPair;

public class MainSigner {
    public static void main(String[] args) {
        try {
            String message = "Hello from signer";

            KeyPair keyPair = RSASignatureUtil.generateKeyPair();

            byte[] hash = RSASignatureUtil.hashMessage(message);
            BigInteger hashValue = RSASignatureUtil.hashToBigInteger(hash);

            BigInteger signature =
                    RSASignatureUtil.signHash(hashValue, keyPair.getPrivate());

            String publicKeyText =
                    RSAKeyUtil.publicKeyToBase64(keyPair.getPublic());

            SignedMessage signedMessage =
                    new SignedMessage(
                            message,
                            signature.toString(),
                            publicKeyText
                    );

            SocketClient.sendSignedMessage(
                    "localhost",
                    5000,
                    signedMessage
            );

            System.out.println("Signed message sent.");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}