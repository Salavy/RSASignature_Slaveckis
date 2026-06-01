import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

public class SignerFrame extends JFrame {

    private JTextArea messageArea;
    private JTextArea signatureArea;
    private JTextArea publicKeyArea;
    private JTextField hashField;
    private JTextField hostField;
    private JTextField portField;
    private JLabel statusLabel;

    private JButton generateKeysButton;
    private JButton signButton;
    private JButton sendButton;
    private JButton clearButton;

    private KeyPair keyPair;

    public SignerFrame() {
        setTitle("RSA Signature - Signer Application");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildGui();
    }

    private void buildGui() {
        setLayout(new BorderLayout(10, 10));
        add(createConnectionPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Connection and Key Info"));

        hostField = new JTextField("localhost");
        portField = new JTextField("5000");
        statusLabel = new JLabel("Status: generate keys, sign, then send");

        generateKeysButton = new JButton("Generate RSA Keys");
        generateKeysButton.addActionListener(e -> generateKeys());

        panel.add(new JLabel("Receiver host:"));
        panel.add(hostField);
        panel.add(new JLabel("Receiver port:"));
        panel.add(portField);
        panel.add(generateKeysButton);
        panel.add(statusLabel);

        return panel;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 8, 8));

        messageArea = new JTextArea();
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        signatureArea = new JTextArea();
        signatureArea.setLineWrap(true);
        signatureArea.setWrapStyleWord(true);
        signatureArea.setEditable(false);

        publicKeyArea = new JTextArea();
        publicKeyArea.setLineWrap(true);
        publicKeyArea.setWrapStyleWord(true);
        publicKeyArea.setEditable(false);

        hashField = new JTextField();
        hashField.setEditable(false);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createTitledBorder("Message to Sign"));
        messagePanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JPanel signaturePanel = new JPanel(new BorderLayout());
        signaturePanel.setBorder(BorderFactory.createTitledBorder("Generated Signature"));
        signaturePanel.add(new JScrollPane(signatureArea), BorderLayout.CENTER);
        signaturePanel.add(hashField, BorderLayout.SOUTH);

        JPanel publicKeyPanel = new JPanel(new BorderLayout());
        publicKeyPanel.setBorder(BorderFactory.createTitledBorder("Sender Public Key (B64)"));
        publicKeyPanel.add(new JScrollPane(publicKeyArea), BorderLayout.CENTER);

        panel.add(messagePanel);
        panel.add(signaturePanel);
        panel.add(publicKeyPanel);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        signButton = new JButton("Sign Message");
        signButton.addActionListener(e -> signMessage());

        sendButton = new JButton("Send to Proxy");
        sendButton.addActionListener(e -> sendToVerifier());

        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearFields());

        panel.add(signButton);
        panel.add(sendButton);
        panel.add(clearButton);

        return panel;
    }

    private void generateKeys() {
        try {
            keyPair = RSASignatureUtil.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

            publicKeyArea.setText(RSAKeyUtil.publicKeyToBase64(publicKey));
            statusLabel.setText("Status: RSA keys generated. Public exponent e = " + publicKey.getPublicExponent());

            JOptionPane.showMessageDialog(this, "RSA key pair generated successfully.");
        } catch (Exception ex) {
            showError("Error generating keys: " + ex.getMessage());
        }
    }

    private void signMessage() {
        try {
            if (keyPair == null) {
                showError("Please generate RSA keys first.");
                return;
            }

            String message = messageArea.getText();
            if (message.isEmpty()) {
                showError("Message cannot be empty.");
                return;
            }

            byte[] hash = RSASignatureUtil.hashMessage(message);
            BigInteger hashValue = RSASignatureUtil.hashToBigInteger(hash);
            BigInteger signature = RSASignatureUtil.signHash(hashValue, keyPair.getPrivate());

            hashField.setText("SHA-256 hash: " + RSASignatureUtil.bytesToHex(hash));
            signatureArea.setText(signature.toString());
            publicKeyArea.setText(RSAKeyUtil.publicKeyToBase64(keyPair.getPublic()));
            statusLabel.setText("Status: message signed. Ready to send.");
        } catch (Exception ex) {
            showError("Error signing message: " + ex.getMessage());
        }
    }

    private void sendToVerifier() {
        try {
            String message = messageArea.getText();
            String signature = signatureArea.getText().trim();
            String publicKey = publicKeyArea.getText().trim();

            if (message.isEmpty()) {
                showError("Message cannot be empty.");
                return;
            }
            if (signature.isEmpty()) {
                showError("Please sign the message before sending.");
                return;
            }
            if (publicKey.isEmpty()) {
                showError("Public key is missing. Generate keys first.");
                return;
            }

            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());

            SignedMessage signedMessage = new SignedMessage(message, signature, publicKey);
            SocketClient.sendSignedMessage(host, port, signedMessage);

            statusLabel.setText("Status: signed message sent to proxy.");
            JOptionPane.showMessageDialog(this, "Signed message sent successfully.");
        } catch (NumberFormatException ex) {
            showError("Port must be a valid number.");
        } catch (Exception ex) {
            showError("Error sending message: " + ex.getMessage());
        }
    }

    private void clearFields() {
        messageArea.setText("");
        signatureArea.setText("");
        hashField.setText("");
        statusLabel.setText("Status: cleared");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
