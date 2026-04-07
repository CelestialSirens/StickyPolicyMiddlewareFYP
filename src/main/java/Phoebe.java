
import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.*;

import org.json.JSONObject;
 

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
        System.out.println("Connect to other user, type: /connect [IP] [PORT]");  // get rid after Peer discov
        System.out.println("To specify a user you wish to dm (JSON), type:/message [Username]");
        System.out.println("When you wish to send an image, use /send_image");
        System.out.println("To send a message, just type it.");
        System.out.println("-------------------------------------------------");

        //USER INPUT While case
        while (true) {
            String input = scanner.nextLine();

            if (input.trim().isEmpty()) continue;

            String command = input;    
            switch (command) {
                case "/connect":    // <-- This needs changing once the DHT / Peer discovery has been fully changed
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
                        System.out.print("Send to:");
                        String recieverUUID = scanner.nextLine().trim();
                        System.out.print("Message to send:");
                        String content = scanner.nextLine().trim();
                        System.out.println("Set Policy requirements (spam enter if none needed)");
                        System.out.println("Allow user to read? (yes or no):");  // if no just ends the process
                        String isReadAllow = scanner.nextLine().trim();
                        boolean read = isReadAllow.isEmpty() || isReadAllow.equalsIgnoreCase("yes");
                            if (!read) { System.out.println("([Phoebe]: Read isn't allowed, ending command.)"); break;}
                        
                        System.out.println("Expiry date/time ( DD/MM/YYYY HH:mm UTC, or leave empty for no expire)");   // <--- Maybe change this to have a check feature of what timezone they want./
                        String expiryInput = scanner.nextLine().trim();

                        StickyPolicy policy = new StickyPolicy.Builder()
                            .allowRead(read)
                            .expiryFromInput(expiryInput)
                            .build();

                        sendTo(recieverUUID, jsonBuilder(username, recieverUUID, content, "text", "", policy));
                    break;

                    case "/image": 
                        System.out.println("Filepath:");
                        String imgPath = scanner.nextLine().trim();
                        System.out.println("Send To:");
                        String imgReciever = scanner.nextLine().trim();
                        System.out.println("Allow user to read? (yes or no):");
                        String imgReadAllow = scanner.nextLine().trim();
                        boolean imgRead = imgReadAllow.isEmpty() || imgReadAllow.equalsIgnoreCase("yes");
                        if (!imgRead) { System.out.println("([Phoebe]: Read isn't allowed, ending command.)"); break;}
                        System.out.println("Expiry date/time ( DD/MM/YYYY HH:mm UTC, or leave empty for no expire");
                        String imgExpiry = scanner.nextLine().trim();
                        StickyPolicy imgPolicy = new StickyPolicy.Builder()
                            .allowRead(imgRead)
                            .expiryFromInput(imgExpiry)
                            .build();
                        try {
                            String ext = FileConversions.getExtension(imgPath);
                            String b64 = FileConversions.imageToB64(imgPath);
                            String imgJson = jsonBuilder(username, imgReciever, b64, "image", ext, imgPolicy);
                            sendTo(imgReciever, imgJson);
                        } catch (Exception e) {
                            System.out.println("[Phoebe]: "+ e.getMessage());
                        }
                        break;

                    case "/file": 
                        System.out.println("Filepath:");
                        String fileFilePath = scanner.nextLine().trim();
                        System.out.println("Send To:");
                        String fileReciever = scanner.nextLine().trim();
                        System.out.println("Allow user to read? (yes or no):");
                        String fileReadAllow = scanner.nextLine().trim();
                        boolean fileRead = fileReadAllow.isEmpty() || fileReadAllow.equalsIgnoreCase("yes");
                        if (!fileRead) { System.out.println("([Phoebe]: Read isn't allowed, ending command.)"); break;}
                        System.out.println("Expiry date/time ( DD/MM/YYYY HH:mm UTC, or leave empty for no expire");
                        String fileExpiry = scanner.nextLine().trim();
                        StickyPolicy filePolicy = new StickyPolicy.Builder()
                            .allowRead(fileRead)
                            .expiryFromInput(fileExpiry)
                            .build();
                        try {
                            String ext = FileConversions.getExtension(fileFilePath);
                            String b64 = FileConversions.fileToB64(fileFilePath); 
                            String fileJson = jsonBuilder(username, fileReciever, b64, "file", ext, filePolicy);
                            sendTo(fileReciever, fileJson);
                        } catch (Exception e) {
                            System.out.println("[Phoebe]: "+ e.getMessage());
                        }
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
// Checks target is real and ACTUALLY dm's only to them
    private static boolean sendTo(String targetName, String message){
        synchronized(peers){
            for (Peer peer:peers){
                if (peer.username.equals(targetName)){
                    peer.out.println(message);
                    return true;
                }
            }
        }
        System.out.println("[Phoebe]: User"+ targetName + "Not found");
        return false;
    }


    // -- Json Code below -- 
// Edit this all its ass . For those not me reading, crow = message Oculus = logs of message
   public static String jsonBuilder(String senderUsername, String reciverUsername, String message, String typeOfData, String fileName, StickyPolicy policy){
        
    JSONObject crow = new JSONObject()
    .put("proginator", senderUsername)
    .put("receiver", reciverUsername)
    .put("message", message)
    .put("fileName", fileName);

    JSONObject Oculus = new JSONObject()
    .put("timestamp", Instant.now().getEpochSecond())
    .put("type", typeOfData)  // type of message like normal msg or image
    .put("policy", policy.toJSON());

    JSONObject Combined = new JSONObject()
    .put("crow", crow)
    .put("oculus", Oculus);
    return Combined.toString(); 
    }
    // Conversion code moved to other class. 

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


    // BACKGROUND TASK: Listening for clients

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
                    PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
                    setupStreams(socket,out);
                    System.out.println("A new peer has connected to you!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // BACKGROUND TASK: Listening to dm client  -- need to make test on this ::::::::::::

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
                System.out.println("[Phoebe]:" + senderUsername + " " + "has connected.");
                String message;
                while ((message = in.readLine()) != null) {
                    try{
                    JSONObject json = new JSONObject(message);
                    JSONObject oculus = json.getJSONObject("oculus");
                    JSONObject crow = json.getJSONObject("crow");

                    String typeOfData = oculus.getString("type");
                    String sender = crow.getString("proginator");
                    String receiver = crow.getString("receiver");
                    if (!receiver.equals(username) && !receiver.equals("all")) {
                        continue;
                    }
                    switch (typeOfData) { 
                        case "text":
                            StickyPolicy policy = StickyPolicy.fromJSON(oculus.getJSONObject("policy"));
                            PolicyEnforcer enforcer = new PolicyEnforcer(policy);
                            if (!enforcer.canRead()) break;
                            System.out.println("[" + sender + "]: " + crow.getString("message"));
                            break;
                        case "image":
                            StickyPolicy imgPolicy = StickyPolicy.fromJSON(oculus.getJSONObject("policy"));
                            PolicyEnforcer imgEnforcer = new PolicyEnforcer(imgPolicy);
                            if (!imgEnforcer.canRead()) break; 
                            System.out.println("["+ sender +"]: " + "send an image");
                            FileConversions.B64ToImage(
                                sender,
                                crow.getString("message"),
                                crow.getString("fileName")
                            );
                            break;
                        case "file":   // do same as ^ when this one is made
                            StickyPolicy filePolicy = StickyPolicy.fromJSON(oculus.getJSONObject("policy"));
                            PolicyEnforcer fileEnforcer = new PolicyEnforcer(filePolicy);
                            if (!fileEnforcer.canRead()) break; 
                            System.out.println("["+ sender +"]: " + "send a file");
                            FileConversions.B64ToFile(
                                sender,
                                crow.getString("message"),
                                crow.getString("fileName")
                            );
                            break;
                        default:
                            System.out.println("[" + sender + "] sent an unknown message type: " + typeOfData);
                            break;
                    }
                  } catch (RuntimeException e) {
                    throw e;
                  } catch (Exception e) {// Not JSON or malformed, print raw as fallback
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
    public static class PolicyEnforcer {
        private final StickyPolicy policy;

        public PolicyEnforcer(StickyPolicy policy){
            this.policy = policy;
        }

        public boolean canRead(){
            if (policy.isExpired()){
                System.out.println("[Phoebe]: Message Blocked: Permissions have expired");
                return false;
            }
            if (!policy.canRead()){
                System.out.println("[Phoebe]: Message Blocked: No read permissions granted");
                return  false;
            }
            return true;
        }


        // public boolean canForward(){
        //   if ()}
     

}
}
