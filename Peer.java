import java.io.*;
import java.net.*;

public class Peer {

    private int listenPort;

    public Peer(int listenPort) {
        this.listenPort = listenPort;
    }

    // --------------------------
    // SERVER PART (Accept connections)
    // --------------------------
    public void startServer() {
        Thread serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(listenPort)) {
                System.out.println("Listening on port " + listenPort);

                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Connected: " + socket.getRemoteSocketAddress());

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    String message;

                    while ((message = in.readLine()) != null) {
                        System.out.println("Received: " + message);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
    }

    // --------------------------
    // CLIENT PART (Connect to another peer)
    // --------------------------
    public void connectToPeer(String host, int port) {
        new Thread(() -> {
            try {
                Socket socket = new Socket(host, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                System.out.println("Connected to peer " + host + ":" + port);

                // Example: send a message
                out.println("Hello from peer on port " + listenPort);

                // keep socket alive or continue messaging
                Thread.sleep(5000);

                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // --------------------------
    // MAIN
    // --------------------------
    public static void main(String[] args) {
        // Example run:
        // Peer A: java Peer 5000
        // Peer B: java Peer 6000

        int myPort = Integer.parseInt(args[0]);
        Peer p = new Peer(myPort);
        p.startServer();

        // OPTIONAL: Try connecting to another peer
        if (args.length == 3) {
            String otherHost = args[1];
            int otherPort = Integer.parseInt(args[2]);
            p.connectToPeer(otherHost, otherPort);
        }
    }
}
