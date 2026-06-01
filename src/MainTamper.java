import javax.swing.*;

public class MainTamper {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TamperFrame frame = new TamperFrame();
            frame.setVisible(true);
        });
    }
}
