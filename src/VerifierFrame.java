import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.security.PublicKey;

public class VerifierFrame extends JFrame {

    private JTextArea messageArea;
    private JTextArea signatureArea;
    private JTextArea publicKeyArea;
    private JTextField portField;
    private JTextField recoveredHashField;
    private JTextField newlyComputedHashField;
    private JLabel statusLabel;
    private JLabel resultLabel;

    private JButton startServerButton;
    private JButton verifyButton;
    private JButton clearButton;

    public VerifierFrame() {
        setTitle("RSA Signature - Verifier Application");
        setSize(900, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildGui();
    }

    private void buildGui() {
        setLayout(new BorderLayout(10, 10));
        add(createTopPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Receiver Server"));

        portField = new JTextField("5000");
        statusLabel = new JLabel("Status: not listening");

        startServerButton = new JButton("Start Receiver");
        startServerButton.addActionListener(e -> startReceiver());

        panel.add(new JLabel("Listen on port:"));
        panel.add(portField);
        panel.add(startServerButton);
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

        publicKeyArea = new JTextArea();
        publicKeyArea.setLineWrap(true);
        publicKeyArea.setWrapStyleWord(true);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createTitledBorder("Received Message"));
        messagePanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JPanel signaturePanel = new JPanel(new BorderLayout());
        signaturePanel.setBorder(BorderFactory.createTitledBorder("Received Signature"));
        signaturePanel.add(new JScrollPane(signatureArea), BorderLayout.CENTER);

        JPanel publicKeyPanel = new JPanel(new BorderLayout());
        publicKeyPanel.setBorder(BorderFactory.createTitledBorder("Received Public Key (B64)"));
        publicKeyPanel.add(new JScrollPane(publicKeyArea), BorderLayout.CENTER);

        panel.add(messagePanel);
        panel.add(signaturePanel);
        panel.add(publicKeyPanel);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Verification"));

        recoveredHashField = new JTextField();
        recoveredHashField.setEditable(false);

        newlyComputedHashField = new JTextField();
        newlyComputedHashField.setEditable(false);

        resultLabel = new JLabel("Verification result: not checked yet");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 16));

        verifyButton = new JButton("Verify Received Signature");
        verifyButton.addActionListener(e -> verifySignature());

        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearFields());

        JPanel hashPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        hashPanel.add(new JLabel("Hash recovered from signature:"));
        hashPanel.add(recoveredHashField);
        hashPanel.add(new JLabel("New SHA-256 hash of received message:"));
        hashPanel.add(newlyComputedHashField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(verifyButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(resultLabel);

        panel.add(hashPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void startReceiver() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            statusLabel.setText("Status: listening on port " + port + "...");
            startServerButton.setEnabled(false);

            SocketServer.startServer(port, new MessageReceivedListener() {
                @Override
                public void onMessageReceived(SignedMessage message) {
                    SwingUtilities.invokeLater(() -> {
                        messageArea.setText(message.getMessage());
                        signatureArea.setText(message.getSignature());
                        publicKeyArea.setText(message.getPublicKey());
                        recoveredHashField.setText("");
                        newlyComputedHashField.setText("");
                        resultLabel.setText("Verification result: message received, not checked yet");
                        statusLabel.setText("Status: message received");
                        startServerButton.setEnabled(true);
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Status: server error");
                        startServerButton.setEnabled(true);
                        showError("Server error: " + errorMessage);
                    });
                }
            });
        } catch (NumberFormatException ex) {
            showError("Port must be a valid number.");
        }
    }

    private void verifySignature() {
        try {
            String message = messageArea.getText();
            String signatureText = signatureArea.getText().trim();
            String publicKeyText = publicKeyArea.getText().trim();

            if (message.isEmpty()) {
                showError("Message cannot be empty.");
                return;
            }
            if (signatureText.isEmpty()) {
                showError("Signature cannot be empty.");
                return;
            }
            if (publicKeyText.isEmpty()) {
                showError("Public key cannot be empty.");
                return;
            }

            // This is the important Level 2 change:
            // The verifier uses the public key received from the signer, not a local key pair.
            PublicKey publicKey = RSAKeyUtil.publicKeyFromBase64(publicKeyText);
            BigInteger signature = new BigInteger(signatureText);

            BigInteger recoveredHash = RSASignatureUtil.recoverHashFromSignature(signature, publicKey);
            byte[] newHash = RSASignatureUtil.hashMessage(message);
            BigInteger newHashValue = RSASignatureUtil.hashToBigInteger(newHash);

            recoveredHashField.setText(recoveredHash.toString(16));
            newlyComputedHashField.setText(RSASignatureUtil.bytesToHex(newHash));

            if (newHashValue.equals(recoveredHash)) {
                resultLabel.setText("Verification result: VALID signature");
            } else {
                resultLabel.setText("Verification result: INVALID signature");
            }
        } catch (NumberFormatException ex) {
            showError("Signature must be a valid number.");
        } catch (Exception ex) {
            showError("Error verifying signature: " + ex.getMessage());
        }
    }

    private void clearFields() {
        messageArea.setText("");
        signatureArea.setText("");
        publicKeyArea.setText("");
        recoveredHashField.setText("");
        newlyComputedHashField.setText("");
        resultLabel.setText("Verification result: not checked yet");
        statusLabel.setText("Status: cleared");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
