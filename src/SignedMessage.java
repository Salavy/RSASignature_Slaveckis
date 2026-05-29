public class SignedMessage {

    private final String message;
    private final String signature;
    private final String publicKey;

    public SignedMessage(String message, String signature, String publicKey) {
        this.message = message;
        this.signature = signature;
        this.publicKey = publicKey;
    }

    public String getMessage() {
        return message;
    }

    public String getSignature() {
        return signature;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String toTransferFormat() {
        return "MESSAGE=" + escape(message) + "\n" +
                "SIGNATURE=" + signature + "\n" +
                "PUBLIC_KEY=" + publicKey + "\n" +
                "END";
    }

    public static SignedMessage fromTransferFormat(String data) {
        String message = "";
        String signature = "";
        String publicKey = "";

        String[] lines = data.split("\\R");

        for (String line : lines) {
            if (line.startsWith("MESSAGE=")) {
                message = unescape(line.substring("MESSAGE=".length()));
            } else if (line.startsWith("SIGNATURE=")) {
                signature = line.substring("SIGNATURE=".length());
            } else if (line.startsWith("PUBLIC_KEY=")) {
                publicKey = line.substring("PUBLIC_KEY=".length());
            }
        }

        if (message.isEmpty()) {
            throw new IllegalArgumentException("Received data does not contain a message.");
        }

        if (signature.isEmpty()) {
            throw new IllegalArgumentException("Received data does not contain a signature.");
        }

        if (publicKey.isEmpty()) {
            throw new IllegalArgumentException("Received data does not contain a public key.");
        }

        return new SignedMessage(message, signature, publicKey);
    }

    private static String escape(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\n", "\\n");
    }

    private static String unescape(String text) {
        return text
                .replace("\\n", "\n")
                .replace("\\\\", "\\");
    }
}