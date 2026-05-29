import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RSASignatureFrame frame = new RSASignatureFrame();
            frame.setVisible(true);
        });
    }
}