import sun.plugin2.applet.Plugin2ClassLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;

public class EV3Client {


    private static final String DISCOVERY_MESSAGE = "DISCOVER_EV3" ;
    private static final int TCP_PORT = 12345;
    private static final int DISCOVERY_PORT = 7799;
    private static final String RESPONSE_MESSAGE = "EV3_HERE";


    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;

    public static void main(String[] args) {
        Scanner scanner;
        connectWithAutoDiscovery();

        while (true)
        {

        }

    }


    private static InetAddress discoverServer() throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            byte[] buf = DISCOVERY_MESSAGE.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
            socket.send(packet);

            socket.setSoTimeout(5000);
            packet = new DatagramPacket(new byte[256], 256);

            try {
                socket.receive(packet);
                String response = new String(packet.getData(), 0, packet.getLength());
                if (RESPONSE_MESSAGE.equals(response)) {
                    return packet.getAddress();
                }
            } catch (SocketTimeoutException e) {
                System.err.println("Discovery timeout");
            }
        }
        return null;
    }

    public static void sendCommand(String command) {
        if (out != null) {
            out.println(command);
        }
    }

    public static String getResponse() throws IOException {
        if (in != null) {
            return in.readLine();
        }
        return null;
    }

    public static void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
    }


    public static boolean connect(String ipAddress, int port) {
        try {
            InetAddress serverAddress = InetAddress.getByName(ipAddress);
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to EV3 server at " + ipAddress + ":" + port);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean connectWithAutoDiscovery() {
        try {
            InetAddress serverAddress = discoverServer();
            if (serverAddress != null) {
                socket = new Socket(serverAddress, TCP_PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("Connected to EV3 server using Auto-Discovery.");
                return true;
            } else {
                System.err.println("Server not found using Auto-Discovery");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
