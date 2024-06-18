import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class EV3Server {

    private static final int PORT = 420;
    private static final int DISCOVERY_PORT = 7799;
    private static final String DISCOVERY_MESSAGE = "DISCOVER_EV3";
    private static final String RESPONSE_MESSAGE = "EV3_HERE";
    public static Socket clientSocket;

    public static void main(String[] args) {

        System.out.println("Waiting for client");
        Thread discoveryThread = new Thread(new DiscoveryTask());
        discoveryThread.start();

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (true) {
                clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                handleClient(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }


    }

    private static void handleClient(Socket clientSocket) {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received command: " + inputLine);
                                                                                                                        // Handle client commands
                handleCommand(inputLine, out);
            }
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    private static void handleCommand(String command, PrintWriter out) {
        String[] parts = command.split(":");
        if (parts.length == 2) {
            String moveCommand = parts[0];
            int speed = Integer.parseInt(parts[1]);
            switch (moveCommand) {
                case "START_LINE_FOLLOWER":
                    Actions.followLine();
                    break;
                case "STOP_LINE_FOLLOWER":
                    Actions.stopFollowLine();
                    break;

                default:
                    out.println("Unknown command: " + command);
                    break;
            }
        } else {
            out.println("Unknown command format: " + command);
        }
    }

    private static class DiscoveryTask implements Runnable {
        @Override
        public void run() {
            try {
                DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT);
                while (true) {
                    byte[] buf = new byte[256];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);

                    String received = new String(packet.getData(), 0, packet.getLength());
                    if (received.equals(DISCOVERY_MESSAGE)) {
                                                                                                                        // Send response to client
                        byte[] responseBuf = RESPONSE_MESSAGE.getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(
                                responseBuf, responseBuf.length, packet.getAddress(), packet.getPort());
                        socket.send(responsePacket);
                    }
                }
            } catch (IOException e) {
                System.err.println("Discovery error: " + e.getMessage());
            }
        }
    }
}
