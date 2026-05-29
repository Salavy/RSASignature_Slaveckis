import javax.swing.SwingUtilities;

public class MainSigner {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SignerFrame frame = new SignerFrame();
            frame.setVisible(true);
        });
    }
}
