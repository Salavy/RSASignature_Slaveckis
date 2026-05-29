import javax.swing.SwingUtilities;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RSASignatureFrame frame = new RSASignatureFrame();
            frame.setVisible(true);
        });
    }
}

    //    public static void main(String[] args) {
//        SignedMessage original = new SignedMessage(
//                "Hello",
//                "12345",
//                "publickeytext"
//        );
//
//        String data = original.toTransferFormat();
//        SignedMessage parsed = SignedMessage.fromTransferFormat(data);
//
//        System.out.println(parsed.getMessage());
//        System.out.println(parsed.getSignature());
//        System.out.println(parsed.getPublicKey());
//
//    }
//}

//    public static void main(String[] args) {
//        try {
//            // 1. Generate keys
//            KeyPair keyPair = RSASignatureUtil.generateKeyPair();
//
//            // 2. Create message
//            String message = "Hello";
//
//            // 3. Hash message
//            byte[] hash = RSASignatureUtil.hashMessage(message);
//            BigInteger hashValue = RSASignatureUtil.hashToBigInteger(hash);
//
//            // 4. Sign hash using private key
//            BigInteger signature =
//                    RSASignatureUtil.signHash(hashValue, keyPair.getPrivate());
//
//            // 5. Convert public key to text
//            String publicKeyText =
//                    RSAKeyUtil.publicKeyToBase64(keyPair.getPublic());
//
//            // 6. Convert text back to PublicKey
//            PublicKey restoredPublicKey =
//                    RSAKeyUtil.publicKeyFromBase64(publicKeyText);
//
//            // 7. Verify using restored public key
//            boolean valid =
//                    RSASignatureUtil.verifySignature(
//                            message,
//                            signature,
//                            restoredPublicKey
//                    );
//
//            System.out.println("Message: " + message);
//            System.out.println("Signature: " + signature);
//            System.out.println("Public key text: " + publicKeyText);
//            System.out.println("Verification result: " + valid);
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//}