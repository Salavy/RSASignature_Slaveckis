import javax.swing.SwingUtilities;

public class MainVerifier {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VerifierFrame frame = new VerifierFrame();
            frame.setVisible(true);
        });
    }
}
