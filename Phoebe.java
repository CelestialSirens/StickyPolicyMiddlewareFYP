import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.*;

import org.json.JSONObject;
import org.json.JSONObject.*;
import java.nio.*; 

public class Phoebe {
    //private static final String UUID = UUID.randomUUID().toString(); // this is temp before DHT
//private static final String UUID = 
    // List to keep track of all the people we are talking to : Change to DHT 
    private static final List<Peer> peers = Collections.synchronizedList(new ArrayList<>());
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

        //USER INPUT While case
        while (true) {
            String input = scanner.nextLine();

            if (input.trim().isEmpty()) continue;

            String[] cmdCheck = input.split(" ",2);
            String command = cmdCheck[0];    
            switch (command) {
                case "/connect":
                    String[] con_parts = input.split(" ");
                    if (con_parts.length == 3) {
                    String ip = con_parts[1];
                    int port = Integer.parseInt(con_parts[2]);
                    connectToPeer(ip, port);
                } else {
                    System.out.println("Must contain: [IP] [PORT] ");
                }
                    break;
                case "/message":
                    String[] msg_parts = input.split(" ", 3);
                    if (msg_parts.length < 3){
                        System.out.println("/message [Username to send to] [Content]");
                    } else {
                        String receiverUUID = msg_parts[1];
                        String content = msg_parts[2];
                        String json = JsonBuilder(receiverUUID, receiverUUID, content, "text", "");
                        broadcast(json);
                        // edit this with the JSON called for MSGs 
                    }
                    break;
                    case "/image": // change this since its no longer just image ^
                        String[] img_parts = input.split(" ",3);
                        if (img_parts.length < 3){
                            System.out.println("/image [Image] [Username to send to]");
                        } else{
                            String imagePath = img_parts[1];
                            String receiver = img_parts[2];
                          //  sendImage (imagePath, receiver); //edit name in a min
                        }
                        break;
                    case "/file": 
                        break;  
                    case "/help":
                        System.out.println("Write listed commands and their features here.");
                        break;
                    case "/exit":
                        System.out.println("Closing Phoebe");
                        System.exit(0);
                        break; 
               default:
                   if (input.startsWith("/")){
                       System.out.println("Command not recognised, type /help for listed commands.");
                   } else{
                       broadcast("["+ username + "]:" + input);
                    }
                    break;
            }
        
        }
    }
// Add new commands above ^ always remember the gap between "" 

// Edit this all its ass . For those not me reading, crow = message Oculus = logs of message
   public static String JsonBuilder(String senderUsername, String reciverUsername, String message, String type, String fileName){
        
    JSONObject crow = new JSONObject()
    .put("proginitor", senderUsername)
    .put("receiver", reciverUsername)
    .put("message", message)
    .put("filename", fileName);
    JSONObject Oculus = new JSONObject()
    .put("timestamp", Instant.now().getEpochSecond())
    .put("version", "1.0")
    .put("Type", "type");  // type of message like normal msg or image
    JSONObject Combined = new JSONObject()
    .put("crow", crow)
    .put("Oculus", Oculus);
    return Combined.toString(); // change this its not returning anything atm
    }
    
    //Image sending command function here 
    private static void sendImag


    // File sending & recieving functions 
    // private static void sendFile(String filePath, String recieverName){
    //     try{
    //         String b64 = fileToBase64(filePath);

    //     } catch (IOException e){
    //         System.out.println("Failed to read image");
    //     }
    // }
    
    // Make one function turning the image into a string 
    // public static String base64Tofile(String filePath) throws IOException{
        
    // }
    // Then another turning it back into an image
    
    
    // Client thread connect-other
    private static void connectToPeer(String ip, int port) {
        try {
            Socket socket = new Socket(ip, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            out.println(username);
            setupStreams(socket, out);
            System.out.println("Connected to peer at " + ip + ":" + port);
        } catch (IOException e) {
            System.out.println("Failed to connect to " + ip + ":" + port);
        }
    }
    // Stores the output stream and starts a listener thread for a new socket

    // New code processes username from connect->peer (for below)
    private static void setupStreams(Socket socket, PrintWriter out) throws IOException {
        peers.add(new Peer("defaultName", 
        socket.getInetAddress().getHostAddress(), 
        socket.getPort(), 
        out));
        new Thread(new PeerHandler(socket, out)).start();
    }

    // Helper to send a message to everyone in the peers list
    private static void broadcast(String message) {
        synchronized (peers) {
            for (Peer peer : peers) {
                peer.out.println(message);
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

    // this needs HEAVY editing, its the biggest connection
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
                String senderUsername = in.readLine();
                if (senderUsername == null) return;
                synchronized(peers){
                    for( Peer peer : peers){
                        if (peer.out == out) {
                            peers.remove(peer);
                            peers.add(new Peer( 
                                senderUsername, 
                                socket.getInetAddress().getHostAddress(),
                                socket.getPort(),
                                out));
                                break;
                        }
                    }
                }
                System.out.println("[Phoebe]:" + senderUsername + "has connected.");
                String message;
                while ((message = in.readLine()) != null) {
                    try{
                    JSONObject json = new JSONObject(message);
                    JSONObject oculus = json.getJSONObject("oculus");
                    JSONObject crow = json.getJSONObject("crow");

                    String type = oculus.getString("type");
                    String sender = crow.getString("proginator");
                    String receiver = crow.getString("receiver");
                    if (!receiver.equals(username) && !receiver.equals("all")) {
                        continue;
                    }
                    switch (type) {
                        case "text":
                            System.out.println("[" + sender + "]: " + crow.getString("message"));
                            break;
                        case "image":
                            String imgFilename = crow.getString("imgFilename");
                            String imgData = crow.getString("imgFilename");
                            // add ref to img -> b64 here
                            break;
                        case "file":
                            String fileFilename = crow.getString("filename");
                            String fileData = crow.getString("message");
                            String outputPath = "received_from_" + sender + "_" + fileFilename;
                            // base64ToFile(fileData, outputPath);   actually make this 
                            System.out.println("[" + sender + "] sent a file -> saved as " + outputPath);
                            break;

                        default:
                            System.out.println("[" + sender + "] sent an unknown message type: " + type);
                            break;
                    }
                  } catch (Exception e) {
                    // Not JSON or malformed, print raw as fallback
                    System.out.println(message);
                }
            }
            } catch (IOException e) {
                // Peer disconnected
            } finally {
                System.out.println("[Phoebe]: A peer disconnected." );
                peers.removeIf(peer -> peer.out == out); // Maybe mention in report? Makes more accurate, https://www.w3schools.com/java/ref_arraylist_removeif.asp
                try { socket.close(); } catch (IOException e) {}
            }
        }
    }
// info for broadcast stuff 
    public static class Peer {
        public final String username;
        public final String ip;
        public final int port;
        public final PrintWriter out;

        public Peer(String username, String ip, int port, PrintWriter out){
            this.username =username;
            this.ip = ip;
            this.port = port;
            this.out = out;
        } 
    }
    
}