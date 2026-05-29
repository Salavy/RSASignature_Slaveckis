import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {

    public static void sendSignedMessage(String host, int port, SignedMessage signedMessage) throws Exception {
        try (
                Socket socket = new Socket(host, port);
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            writer.println(signedMessage.toTransferFormat());
        }
    }
}