import javax.swing.SwingUtilities;

public class Main {
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            RSASignatureFrame frame = new RSASignatureFrame();
//            frame.setVisible(true);
//        });
//    }
//}

    public static void main(String[] args) {
        SignedMessage original = new SignedMessage(
                "Hello",
                "12345",
                "publickeytext"
        );

        String data = original.toTransferFormat();
        SignedMessage parsed = SignedMessage.fromTransferFormat(data);

        System.out.println(parsed.getMessage());
        System.out.println(parsed.getSignature());
        System.out.println(parsed.getPublicKey());

    }
}