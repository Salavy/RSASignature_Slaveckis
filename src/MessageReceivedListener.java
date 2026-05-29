public interface MessageReceivedListener {
    void onMessageReceived(SignedMessage message);
    void onError(String errorMessage);
}
//in Swing, socket listening should not run directly on the main GUI thread, or the window may freeze.