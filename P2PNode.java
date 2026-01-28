import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.*;

import org.json.JSONObject;
import org.json.JSONObject.*;

public class P2PNode {
    //private static final String UUID = UUID.randomUUID().toString(); // this is temp before DHT
private static final String UUID = 
    // List to keep track of all the people we are talking to : Change to DHT 
    private static final List<PrintWriter> peers = Collections.synchronizedList(new ArrayList<>());
    private static String username;
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("--- Welcome to Phoebe ---");
        System.out.print("Enter your username: ");
        username = scanner.nextLine();
        System.out.print("Enter the port you want to listen on (e.g. 6000): ");
        int myPort = Integer.parseInt(scanner.nextLine());
      
        // Server Thread start
        new Thread(new ServerTask(myPort)).start();
        System.out.println("You are listening on port " + myPort);
        System.out.println("Connect to other user, type: /connect [IP] [PORT]");
        System.out.println("To specify a user you wish to dm (JSON), type:/message [Username]");
        System.out.println("When you wish to send an image, use /send_image");
        System.out.println("To send a message, just type it.");
        System.out.println("-------------------------------------------------");

        // USER INPUT While case
        while (true) {
            String input = scanner.nextLine();

            if (input.startsWith("/connect")) {
                // Command format: /connect 127.0.0.1 6001
                String[] parts = input.split(" ");
                if (parts.length == 3) {
                    String ip = parts[1];
                    int port = Integer.parseInt(parts[2]);
                    connectToPeer(ip, port);
                } else {
                    System.out.println("Must contain: [IP] [PORT] ");//[PORT]Usage: /connect [IP] [PORT]");
                }
            } else {
                // Normal chat message - Send to ALL connected peers
                broadcast("[" + username + "]: " + input);
            } 
                if (input.startsWith("/message")) {
                // /message [reciverUUID] [content]
                String[] parts = input.split("input",3);
                if (parts.length < 3) {
                    System.out.println("Command: /message [Username to send to] [Content]");
                } else {
                    String reciverUUID = parts[1];
                    String JsonContent = parts[2];
                }
                    // Call builder here, add Image sending code input here 
            }
        }
    }
//Add Json Builder here
// Edit this all its ass . For those not me reading, crow = message Oculus = metadata of message
   public static String JsonBuilder(String senderUUID, String reciverUUID, String message){
        
    JSONObject crow = new JSONObject();
    crow.put("proginitor", senderUUID);
    crow.put("message", message);
    JSONObject Oculus = new JSONObject();
    Oculus.put("timestamp", Instant.now().getEpochSecond());
    Oculus.put("version", "1.0");
    }

    // Client thread connect-other
    private static void connectToPeer(String ip, int port) {
        try {
            Socket socket = new Socket(ip, port);
            setupStreams(socket);
            System.out.println("Connected to peer at " + ip + ":" + port);
        } catch (IOException e) {
            System.out.println("Failed to connect to " + ip + ":" + port);
        }
    }
    // Stores the output stream and starts a listener thread for a new socket
    private static void setupStreams(Socket socket) throws IOException {
        // Output stream: used to send messages TO this peer
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        
        // Change this for DHT
        peers.add(out);

        // Input stream: used to listen for messages FROM this peer
        // We start a new thread so we can listen to multiple peers at once
        new Thread(new PeerHandler(socket, out)).start();
    }

    // Helper to send a message to everyone in the peers list
    private static void broadcast(String message) {
        synchronized (peers) {
            for (PrintWriter out : peers) {
                out.println(message);
            }
        }
    }

    // ------------------------------------------------------------
    // BACKGROUND TASK: WAITS FOR INCOMING CONNECTIONS
    // ------------------------------------------------------------
    private static class ServerTask implements Runnable {
        private int port;

        public ServerTask(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    // When someone connects, set up the streams
                    setupStreams(socket);
                    System.out.println("A new peer has connected to you!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ------------------------------------------------------------
    // BACKGROUND TASK: LISTENS TO ONE SPECIFIC PEER
    // ------------------------------------------------------------
    private static class PeerHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public PeerHandler(Socket socket, PrintWriter out) throws IOException {
            this.socket = socket;
            this.out = out;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                // Peer disconnected
            } finally {
                System.out.println("A peer disconnected." );
                peers.remove(out); // Remove from list so we don't send to dead socket
                try { socket.close(); } catch (IOException e) {}
            }
        }
    }
}