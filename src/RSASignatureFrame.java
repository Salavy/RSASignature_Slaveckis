import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

public class RSASignatureFrame extends JFrame {

    private JTextArea messageToSignArea;
    private JTextArea signatureArea;
    private JTextArea messageToVerifyArea;

    private JTextField hashField;
    private JTextField recoveredHashField;
    private JTextField publicKeyField;
    private JLabel resultLabel;

    private JButton generateKeysButton;
    private JButton signButton;
    private JButton copyMessageButton;
    private JButton verifyButton;
    private JButton clearButton;

    private KeyPair keyPair;

    public RSASignatureFrame() {
        setTitle("RSA Digital Signature");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        buildGui();
    }

    private void buildGui() {
        setLayout(new BorderLayout(10, 10));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("RSA Key Information"));

        generateKeysButton = new JButton("Generate RSA Keys");
        generateKeysButton.addActionListener(e -> generateKeys());

        publicKeyField = new JTextField();
        publicKeyField.setEditable(false);

        panel.add(generateKeysButton, BorderLayout.WEST);
        panel.add(publicKeyField, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        mainPanel.add(createSigningPanel());
        mainPanel.add(createVerificationPanel());

        return mainPanel;
    }

    private JPanel createSigningPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("1. Message Signing"));

        messageToSignArea = new JTextArea();
        messageToSignArea.setLineWrap(true);
        messageToSignArea.setWrapStyleWord(true);

        hashField = new JTextField();
        hashField.setEditable(false);

        signatureArea = new JTextArea();
        signatureArea.setLineWrap(true);
        signatureArea.setWrapStyleWord(true);

        signButton = new JButton("Sign Message");
        signButton.addActionListener(e -> signMessage());

        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        infoPanel.add(new JLabel("SHA-256 hash of message:"));
        infoPanel.add(hashField);
        infoPanel.add(new JLabel("Generated signature:"));

        panel.add(new JScrollPane(messageToSignArea), BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(new JScrollPane(signatureArea), BorderLayout.SOUTH);
        panel.add(signButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel createVerificationPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("2. Signature Verification"));

        messageToVerifyArea = new JTextArea();
        messageToVerifyArea.setLineWrap(true);
        messageToVerifyArea.setWrapStyleWord(true);

        recoveredHashField = new JTextField();
        recoveredHashField.setEditable(false);

        verifyButton = new JButton("Verify Signature");
        verifyButton.addActionListener(e -> verifySignature());

        copyMessageButton = new JButton("Copy Signing Message to Verify");
        copyMessageButton.addActionListener(e ->
                messageToVerifyArea.setText(messageToSignArea.getText())
        );

        resultLabel = new JLabel("Verification result: not checked yet");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        buttonPanel.add(copyMessageButton);
        buttonPanel.add(verifyButton);
        buttonPanel.add(resultLabel);

        JPanel hashPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        hashPanel.add(new JLabel("Hash recovered from signature:"));
        hashPanel.add(recoveredHashField);

        panel.add(new JScrollPane(messageToVerifyArea), BorderLayout.CENTER);
        panel.add(hashPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearFields());

        panel.add(clearButton);

        return panel;
    }

    private void generateKeys() {
        try {
            keyPair = RSASignatureUtil.generateKeyPair();

            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

            publicKeyField.setText(
                    "Public key: n = " + publicKey.getModulus() +
                            ", e = " + publicKey.getPublicExponent()
            );

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

            String message = messageToSignArea.getText();

            if (message.isEmpty()) {
                showError("Message cannot be empty.");
                return;
            }

            byte[] hash = RSASignatureUtil.hashMessage(message);
            BigInteger hashValue = RSASignatureUtil.hashToBigInteger(hash);

            BigInteger signature =
                    RSASignatureUtil.signHash(hashValue, keyPair.getPrivate());

            hashField.setText(RSASignatureUtil.bytesToHex(hash));
            signatureArea.setText(signature.toString());

            resultLabel.setText("Verification result: not checked yet");
            recoveredHashField.setText("");

        } catch (Exception ex) {
            showError("Error signing message: " + ex.getMessage());
        }
    }

    private void verifySignature() {
        try {
            if (keyPair == null) {
                showError("Please generate RSA keys first.");
                return;
            }

            String verificationMessage = messageToVerifyArea.getText();

            if (verificationMessage.isEmpty()) {
                showError("Verification message cannot be empty.");
                return;
            }

            String signatureText = signatureArea.getText().trim();

            if (signatureText.isEmpty()) {
                showError("Signature cannot be empty.");
                return;
            }

            BigInteger signature = new BigInteger(signatureText);

            BigInteger recoveredHash =
                    RSASignatureUtil.recoverHashFromSignature(
                            signature,
                            keyPair.getPublic()
                    );

            byte[] newHash = RSASignatureUtil.hashMessage(verificationMessage);
            BigInteger newHashValue = RSASignatureUtil.hashToBigInteger(newHash);

            recoveredHashField.setText(recoveredHash.toString(16));

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
        messageToSignArea.setText("");
        messageToVerifyArea.setText("");
        signatureArea.setText("");
        hashField.setText("");
        recoveredHashField.setText("");
        resultLabel.setText("Verification result: not checked yet");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}