//import java.io.IOException;
//
//public class FCXCMClientTester {
//
//    public void connectToServer() throws IOException {
//
//        // Get the server address from a dialog box.
//        String serverAddress = JOptionPane.showInputDialog(
//            frame,
//            "Enter IP Address of the Server:",
//            "Welcome to the Capitalization Program",
//            JOptionPane.QUESTION_MESSAGE);
//
//        // Make connection and initialize streams
//        Socket socket = new Socket(serverAddress, 9898);
//        in = new BufferedReader(
//                new InputStreamReader(socket.getInputStream()));
//        out = new PrintWriter(socket.getOutputStream(), true);
//
//        // Consume the initial welcoming messages from the server
//        for (int i = 0; i < 3; i++) {
//            messageArea.append(in.readLine() + "\n");
//        }
//    }
//}
