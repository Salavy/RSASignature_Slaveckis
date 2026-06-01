import javax.swing.*;
import java.awt.*;

public class TamperFrame extends JFrame {

    private JTextField listenPortField;
    private JTextField verifierHostField;
    private JTextField verifierPortField;

    private JTextArea messageArea;
    private JTextArea signatureArea;
    private JTextArea publicKeyArea;

    private JLabel statusLabel;

    private JButton startReceiverButton;
    private JButton forwardButton;
    private JButton clearButton;

    public TamperFrame() {
        setTitle("RSA Signature - Proxy Application");
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
        JPanel panel = new JPanel(new GridLayout(2, 4, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Proxy Connection Settings"));

        listenPortField = new JTextField("5000");
        verifierHostField = new JTextField("localhost");
        verifierPortField = new JTextField("5001");

        statusLabel = new JLabel("Status: not listening");

        startReceiverButton = new JButton("Start Proxy Receiver");
        startReceiverButton.addActionListener(e -> startReceiver());

        panel.add(new JLabel("Listen for signer on port:"));
        panel.add(listenPortField);
        panel.add(new JLabel("Verifier host:"));
        panel.add(verifierHostField);

        panel.add(new JLabel("Forward to verifier port:"));
        panel.add(verifierPortField);
        panel.add(startReceiverButton);
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
        messagePanel.setBorder(BorderFactory.createTitledBorder("Message received from signer"));
        messagePanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JPanel signaturePanel = new JPanel(new BorderLayout());
        signaturePanel.setBorder(BorderFactory.createTitledBorder("Signature received from signer"));
        signaturePanel.add(new JScrollPane(signatureArea), BorderLayout.CENTER);

        JPanel publicKeyPanel = new JPanel(new BorderLayout());
        publicKeyPanel.setBorder(BorderFactory.createTitledBorder("Public key received from signer"));
        publicKeyPanel.add(new JScrollPane(publicKeyArea), BorderLayout.CENTER);

        panel.add(messagePanel);
        panel.add(signaturePanel);
        panel.add(publicKeyPanel);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        forwardButton = new JButton("Forward to Verifier");
        forwardButton.addActionListener(e -> forwardToVerifier());

        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearFields());

        panel.add(forwardButton);
        panel.add(clearButton);

        return panel;
    }

    private void startReceiver() {
        try {
            int listenPort = Integer.parseInt(listenPortField.getText().trim());

            statusLabel.setText("Status: listening for signer on port " + listenPort + "...");
            startReceiverButton.setEnabled(false);

            SocketServer.startServer(listenPort, new MessageReceivedListener() {
                @Override
                public void onMessageReceived(SignedMessage message) {
                    SwingUtilities.invokeLater(() -> {
                        messageArea.setText(message.getMessage());
                        signatureArea.setText(message.getSignature());
                        publicKeyArea.setText(message.getPublicKey());

                        statusLabel.setText("Status: received signed data. You may tamper, then forward.");
                        startReceiverButton.setEnabled(true);
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Status: proxy receiver error");
                        startReceiverButton.setEnabled(true);
                        showError("Proxy receiver error: " + errorMessage);
                    });
                }
            });

        } catch (NumberFormatException ex) {
            showError("Listen port must be a valid number.");
        }
    }

    private void forwardToVerifier() {
        try {
            String message = messageArea.getText();
            String signature = signatureArea.getText().trim();
            String publicKey = publicKeyArea.getText().trim();

            if (message.isEmpty()) {
                showError("Message cannot be empty.");
                return;
            }

            if (signature.isEmpty()) {
                showError("Signature cannot be empty.");
                return;
            }

            if (publicKey.isEmpty()) {
                showError("Public key cannot be empty.");
                return;
            }

            String verifierHost = verifierHostField.getText().trim();
            int verifierPort = Integer.parseInt(verifierPortField.getText().trim());

            SignedMessage signedMessage = new SignedMessage(message, signature, publicKey);
            SocketClient.sendSignedMessage(verifierHost, verifierPort, signedMessage);

            statusLabel.setText("Status: forwarded data to verifier.");
            JOptionPane.showMessageDialog(this, "Data forwarded to verifier successfully.");

        } catch (NumberFormatException ex) {
            showError("Verifier port must be a valid number.");
        } catch (Exception ex) {
            showError("Error forwarding data: " + ex.getMessage());
        }
    }

    private void clearFields() {
        messageArea.setText("");
        signatureArea.setText("");
        publicKeyArea.setText("");
        statusLabel.setText("Status: cleared");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
