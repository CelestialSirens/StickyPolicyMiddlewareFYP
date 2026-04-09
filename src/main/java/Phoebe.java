
import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.*;

import org.json.JSONObject;
 

public class Phoebe {   

    private static final DHT dht = new DHT();

    private static final List<Peer> peers = Collections.synchronizedList(new ArrayList<>());
    private static String username;
    private static int myPort;
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("--- Welcome to Phoebe ---");
        System.out.print("Enter your username: ");
        username = scanner.nextLine();
        System.out.print("Enter the port you want to listen on (e.g. 6000): ");
        int myPort = Integer.parseInt(scanner.nextLine());
        try {
            String localIp = InetAddress.getLocalHost().getHostAddress();
            dht.register(username, localIp, myPort);
            System.out.println("[Testing]: Registed as " + username + "at " + localIp + ": " + myPort);
        } catch (Exception e) {
            System.out.println("[Testing]: " + e.getMessage());
        }
        
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
                case "/connect":   
                    System.out.println("Username to connect to:");
                    String connectUsername = scanner.nextLine().trim();

                    boolean alreadyConnected = false;
                    synchronized (peers) {
                        for (Peer peer : peers){
                            if (peer.username.equals(connectUsername)){
                                alreadyConnected = true;
                                break;
                            }
                        }
                    }
                    if (alreadyConnected) {
                        System.out.println("[Phoebe]: Already connected to " + connectUsername);
                        break;
                    }
                    try{ 
                        DHT.PeerInfo peerInfo = dht.lookup(connectUsername);
                        connectToPeer(peerInfo.ip, peerInfo.port);
                        System.out.println("[Phoebe]: Connected to " + connectUsername);
                    } catch (Exception e){
                        System.out.println(e.getMessage() + " ");
                    } 
                    break;
                case "/initalise":
                        System.out.println("IP:");
                        String initalisedIP = scanner.nextLine().trim();
                        System.out.println("Port:");
                        int initalisedPort = Integer.parseInt(scanner.nextLine().trim());
                        connectToPeer(initalisedIP, initalisedPort);
                        try{
                            System.out.println("[Phoebe]: Initalised into network.");
                            System.out.println("[Phoebe]: Please remember to do this regularly for an up to date network.");
                        } catch (Exception e){
                            System.out.println("[Phoebe]: " + e.getMessage());
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
                        System.out.println("/initalise - need to do this with a known user to initiate your tables");
                        System.out.println("/connect [username] - connect to a known user");
                        System.out.println("/message - send a message to a known user");
                        System.out.println("/image - send an image to a known user");
                        System.out.println("/file - send a file to a known user");
                        System.out.println("/exit - close Phoebe");
                        System.err.println("For more detailed information, do any command (except /exit) without all prematers");                       
                        break;
                    case "/exit":
                        System.out.println("Closing Phoebe");
                        System.exit(0);
                        break; 
               default:
                   if (input.startsWith("/")){
                       System.out.println("Command not recognised, type /help for listed commands. Please check all tables updated with /initalise");
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
            System.out.println("[Testing]: Looking for " + targetName + "in list");
            for (Peer peer:peers){
                System.out.println("[Testing]: Found peer:" + peer.username + "");
                if (peer.username.equals(targetName)){
                    peer.out.println(message);
                    return true;
                }
            }
        }
        System.out.println("[Phoebe]: User "+ targetName + " Not found");
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
        synchronized (peers) {
            for (Peer peer :peers){
                if (peer.ip.equals(ip)){
                    System.out.println("[Phoebe]: Already connected to this peer");
                    return;
                }
            }
        }
        try {
            Socket socket = new Socket(ip, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("INIT");
            out.println(username);
            out.println("PORT:" +myPort);
            out.println(dht.toJson().toString());

            String peerDHTJson = input.readLine();
            if(peerDHTJson != null){
                try {
                    DHT peerDHT = DHT.fromJson(new JSONObject(peerDHTJson));
                    dht.merge(peerDHT);
                System.out.println("[Testing]: Tables merged - Line 214");
            } catch (Exception e){
                System.out.println("[Testing]: Tables failed to merge - 216");
            }
        }
            String ready = input.readLine();
            if (!"READY".equals(ready)){
                System.out.println("[Phoebe]: Handshake failed");
                socket.close();
                return;
            }
            String peerUsername = "defaultName";
            synchronized (dht) {
                for (Map.Entry<String, DHT.PeerInfo> en : dht.getTable().entrySet()) {
                    if (en.getValue().ip.equals(ip)){
                        peerUsername = en.getKey();
                        break;
                    }
                    
                }
            }
            setupStreams(socket, out, input, true, peerUsername);
            System.out.println("Connected to peer at " + ip + ":" + port);
        } catch (IOException e) {
            System.out.println("Failed to connect to " + ip + ":" + port);
        }
    }
    // New code processes username from connect->peer (for below)
    private static void setupStreams(Socket socket, PrintWriter out, BufferedReader in, boolean handShakeDone, String knownUsername) throws IOException {
        peers.add(new Peer(knownUsername != null ? knownUsername :"defaultName", 
        socket.getInetAddress().getHostAddress(), 
        socket.getPort(), 
        out));
        new Thread(new PeerHandler(socket, out, in, handShakeDone)).start();
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
        private final int port;

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
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    setupStreams(socket,out, in, false, null);
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
        private final boolean handShakeDone;

        public PeerHandler(Socket socket, PrintWriter out, BufferedReader in, boolean handShakeDone) throws IOException {
            this.socket = socket;
            this.out = out;
            this.in = in;
            this.handShakeDone = handShakeDone;
        }
        @Override
        public void run() {
            try {
                if (!handShakeDone){
                String firstline = in.readLine();
                if (firstline == null) return;
                if (!firstline.equals("INIT")){
                    System.out.println("[Phoebe]: Invalid handshake");
                    return;
                }

                String senderUsername = in.readLine();
                if (senderUsername == null) return;
                String portline = in.readLine();
                int d1ListenPort = Integer.parseInt(portline.replace("PORT:", ""));
                String peerDHTJson = in.readLine();
                if (peerDHTJson != null){
                    try {
                        DHT peerDHT = DHT.fromJson(new JSONObject(peerDHTJson));
                        dht.merge(peerDHT);
                        System.out.println("[Peer handler]: Tables merged with " + senderUsername);
                    } catch (Exception e) {
                        System.out.println("[Peer handler]: Tables failed to merge with " + senderUsername);
                    }
                }
                out.println(dht.toJson().toString());
                out.println("READY");
                try {
                    dht.register(senderUsername, socket.getInetAddress().getHostAddress(), d1ListenPort);
                } catch (Exception e) {
                    System.out.println("[Peer handler]:"+ e.getMessage());
                }
                synchronized(peers){
                    for(Peer peer : peers){
                        if (peer.out == out) {
                            peers.remove(peer);
                            peers.add(new Peer( 
                                senderUsername, 
                                socket.getInetAddress().getHostAddress(),
                                d1ListenPort,
                                out));
                                break;
                        }
                    }
                }
                System.out.println("[Phoebe]:" + senderUsername + " " + "has connected.");
                } else 
                try{
                    String nonDefaultName = "defaultName";
                    for (Map.Entry<String, DHT.PeerInfo> en : dht.getTable().entrySet()) {
                        if (en.getValue().ip.equals(socket.getInetAddress().getHostAddress())){
                        nonDefaultName = en.getKey();
                        break;
                    }
                }
                final String realUsername = nonDefaultName;
                synchronized (peers) {
                    for (Peer peer : peers){
                        if (peer.out == out){
                            peers.remove(peer);
                            peers.add(new Peer( realUsername, socket.getInetAddress().getHostAddress(), myPort, out));
                            break;
                        }
                    }
                }
                System.out.println("[Phoebe]: " + realUsername + " has connected");
            } catch (Exception e){
                System.out.println("[Phoebe]: Could not resolve username");
            }
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
                  } catch (Exception e) {// Not JSON or malformed, print raw as fallback
                    System.out.println(message);
                }
            }
            } catch (IOException e) {
                // Peer disconnected
            } finally {
                System.out.println("[Phoebe]: A peer disconnected." );
                peers.removeIf(peer -> peer.out == out); // Maybe mention in report? Makes more accurate, https://www.w3schools.com/java/ref_arraylist_removeif.asp
                synchronized (peers) {
                    for (Peer peer : peers){
                        if (peer.out == out){
                            dht.remove(peer.username);
                            break;
                        }
                    }
                }
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
