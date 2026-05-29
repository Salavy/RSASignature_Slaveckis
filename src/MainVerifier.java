import javax.swing.SwingUtilities;
import java.math.BigInteger;
import java.security.PublicKey;

public class MainVerifier {
    public static void main(String[] args) {
        System.out.println("Verifier started. Waiting for signed message...");

        SocketServer.startServer(5000, new MessageReceivedListener() {
            @Override
            public void onMessageReceived(SignedMessage message) {
                try {
                    System.out.println("Message received!");
                    System.out.println("Message: " + message.getMessage());
                    System.out.println("Signature: " + message.getSignature());
                    System.out.println("Public key: " + message.getPublicKey());

                    PublicKey publicKey =
                            RSAKeyUtil.publicKeyFromBase64(message.getPublicKey());

                    BigInteger signature =
                            new BigInteger(message.getSignature());

                    boolean valid =
                            RSASignatureUtil.verifySignature(
                                    message.getMessage(),
                                    signature,
                                    publicKey
                            );

                    System.out.println("Verification result: " + valid);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("Server error: " + errorMessage);
            }
        });
    }
}