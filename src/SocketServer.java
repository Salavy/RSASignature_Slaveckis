import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {

    public static void startServer(int port, MessageReceivedListener listener) {
        new Thread(() -> {
            try (
                    ServerSocket serverSocket = new ServerSocket(port);
                    Socket socket = serverSocket.accept();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(socket.getInputStream())
                    )
            ) {
                StringBuilder data = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.equals("END")) {
                        break;
                    }

                    data.append(line).append("\n");
                }

                SignedMessage message =
                        SignedMessage.fromTransferFormat(data.toString());

                listener.onMessageReceived(message);

            } catch (Exception ex) {
                listener.onError(ex.getMessage());
            }
        }).start();
    }
}