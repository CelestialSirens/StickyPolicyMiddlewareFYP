
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONObject;
public class Phoebe {   

    private static final DHT dht = new DHT();
    private static final List<Peer> peers = Collections.synchronizedList(new ArrayList<>());
    private static final List<PendingRequest> pendingRequests = Collections.synchronizedList(new ArrayList<>());

        private static String username;
        private static int myPort;
        private static String localIp;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("--- Welcome to Phoebe ---");
        System.out.print("Enter your username: ");
        username = scanner.nextLine();
        System.out.print("Enter the port you want to listen on (any open port between 1 - 50,000): ");
        myPort = Integer.parseInt(scanner.nextLine());
        localIp = InetAddress.getLocalHost().getHostAddress();
        new Thread(new ServerTask(myPort)).start();
        try {
            dht.register(username, localIp, myPort);
            System.out.println("[Phoebe]: Registed as " + username + " at " + localIp + "  : " + myPort);
            } catch (Exception e) {
            System.out.println("[Phoebe-Error]: " + e.getMessage());
            }        
        
        System.out.println("You are listening on port " + myPort);
        System.out.println("Please ensure you have merged your tables with another user before attempting to message them.");
        System.out.println("To connect to other users, type: /connect");  
        System.out.println("To message a specific user, use /message");
        System.out.println("When you wish to send an image or file, use /image or /file");
        System.out.println("To send a message, just type it.");
        System.out.println("-------------------------------------------------");

        while (true) {    // Remember to remove the "[Testing]:" stuff
            String input = scanner.nextLine();
            if (input.trim().isEmpty()) 
                continue;

            String command = input;    
            switch (command.toUpperCase()) {
                case "/CONNECT":   
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

                case "/INITALISE":
                        System.out.println("[Table merging] IP:");
                        String initalisedIP = scanner.nextLine().trim();
                        System.out.println("[Table merging] Port:");
                        int initalisedPort = Integer.parseInt(scanner.nextLine().trim());
                        connectToPeer(initalisedIP, initalisedPort);
                        try{
                            System.out.println("[Phoebe]: Initalised into network.");
                            System.out.println("[Phoebe]: Please remember to do /update regularly for an up to date network.");
                            System.out.println("[Phoebe]: ");
                        } catch (Exception e){
                            System.out.println("[Phoebe]: " + e.getMessage());
                        }
                        break;

                case "/UPDATE":
                    System.out.println("[Phoebe-Update]: Requesting DHT update from all connected peers...");
                    synchronized (peers) {
                        if (peers.isEmpty()){
                            System.out.println("[Phoebe-Update]: No connected peers to request from");
                            break;
                        }
                        for (Peer peer : peers){
                            JSONObject updateRequest = new JSONObject()
                        .put("Oculus", new JSONObject()
                            .put("Type", "update_request")
                            .put("Timestamp", Instant.now().getEpochSecond()))
                        .put("Crow", new JSONObject()
                            .put("Proginator", username)
                            .put( "Receiver", peer.username));
                        peer.out.println(updateRequest.toString());
                        }
                    }
                        System.out.println("[Phoebe-Update]: Update Requested from peers.");
                    break;

                case "/PENDING":
                    synchronized (pendingRequests) {
                        if (pendingRequests.isEmpty()){
                            System.out.println("[Phoebe]: No requests");
                            break;
                        } 
                    List<PendingRequest> toRemove = new ArrayList<>();
                    for (PendingRequest request : pendingRequests){
                    String tableContents = String.join(",", dht.getTable().keySet());    
                    System.out.println("[Phoebe]: A user: " + request.requesterUsername + " requested to merge tables");
                    System.out.println("[Phoebe]: Your table contains: " + tableContents + "Please check you are comfortable with merging...");
                    System.out.println("[Phoebe]: Do you accept? (Yes or no)");
                    String mergeAnswer = scanner.nextLine().trim();
                        if (mergeAnswer.equalsIgnoreCase("yes") || (mergeAnswer.equalsIgnoreCase("y"))){
                            JSONObject response = new JSONObject()
                            .put("Oculus", new JSONObject()
                                .put("Type", "update_response")
                                .put("Timestamp", Instant.now().getEpochSecond())
                                .put("DHT", dht.toJson()))
                            .put("Crow", new JSONObject()
                                .put("Proginator", username)
                                .put("Receiver", request.requesterUsername));   
                        request.requesterOut.println(response.toString());
                    System.out.println("[Phoebe]: Table shared with " + request.requesterUsername);
                    } else {
                        JSONObject denied = new JSONObject()
                        .put("Oculus", new JSONObject()
                            .put("Type", "update_denied")
                            .put("Timestamp", Instant.now().getEpochSecond()))
                        .put("Crow", new JSONObject()
                            .put("Proginator", username)
                            .put("Receiver", request.requesterUsername));    
                        request.requesterOut.println(denied.toString());
                        System.out.println("[Phoebe]: Request from " + request.requesterUsername + "denied");
                        }
                        toRemove.add(request);
                    }
                    pendingRequests.removeAll(toRemove);
                }    
                    break;

                case "/MESSAGE":
                        System.out.print("[Message-Command]Send to:");
                        String receiverUUID = scanner.nextLine().trim();
                        System.out.print("[Message-Command]Message to send:");
                        String content = scanner.nextLine().trim();
                        System.out.println("[Message-Command]Set Policy requirements");
                        System.out.println("[Message-Command]Allow user to read? (yes or no):");  
                        String isReadAllow = scanner.nextLine().trim();
                        boolean read = isReadAllow.isEmpty() || isReadAllow.equalsIgnoreCase("yes") 
                        || isReadAllow.equalsIgnoreCase("y");
                            if (!read) { System.out.println("([Phoebe]: Read isn't allowed, ending command.)"); break;}
                        System.out.println("[Expiration Time] Expiry date/time ( DD/MM/YYYY HH:mm UTC, or leave empty for no expire)");   
                        String expiryInput = scanner.nextLine().trim();
                        StickyPolicy policy = new StickyPolicy.Builder().allowRead(read).expiryFromInput(expiryInput).build();
                            sendTo(receiverUUID, jsonBuilder(username, receiverUUID, content, "Text", "", policy));
                    break;

                    case "/IMAGE": 
                        System.out.println("[Image-Command]Filepath:");
                        String imgPath = scanner.nextLine().trim();
                        System.out.println("[Image-Command]Send To:");
                        String imgReceiver = scanner.nextLine().trim();
                        System.out.println("[Image-Command]Allow user to read? (yes or no):");
                        String imgReadAllow = scanner.nextLine().trim();
                        boolean imgRead = imgReadAllow.isEmpty() || imgReadAllow.equalsIgnoreCase("yes") 
                        || imgReadAllow.equalsIgnoreCase("y");
                        if (!imgRead) { System.out.println("([Phoebe]: Read isn't allowed, ending command.)"); break;}
                        System.out.println("Allow user to download? [Yes or No : Default = Yes]");
                        String imgDownloadAllow = scanner.nextLine().trim();
                        boolean imgDownload = imgDownloadAllow.isEmpty() || imgDownloadAllow.equalsIgnoreCase("yes") 
                        || imgDownloadAllow.equalsIgnoreCase("y");
                        System.out.println("[Expiration Time] Expiry date/time ( DD/MM/YYYY HH:mm UTC, or leave empty for no expire)");
                        String imgExpiry = scanner.nextLine().trim();
                        StickyPolicy imgPolicy = new StickyPolicy.Builder().allowRead(imgRead).allowDownload(imgDownload).expiryFromInput(imgExpiry).build();
                        try {
                            String ext = FileConversions.getExtension(imgPath);
                            String b64 = FileConversions.imageToB64(imgPath);
                            String imgJson = jsonBuilder(username, imgReceiver, b64, "Image", ext, imgPolicy);
                            sendTo(imgReceiver, imgJson);
                        } catch (Exception e) {
                            System.out.println("[Phoebe]: "+ e.getMessage());
                        }
                    break;

                    case "/FILE": 
                        System.out.println("[File-Command]Filepath:");
                        String fileFilePath = scanner.nextLine().trim();
                        System.out.println("[File-Command]Send To:");
                        String fileReceiver = scanner.nextLine().trim();
                        System.out.println("[File-Command]Allow user to read? (yes or no):");
                        String fileReadAllow = scanner.nextLine().trim();
                        boolean fileRead = fileReadAllow.isEmpty() || 
                        fileReadAllow.equalsIgnoreCase("yes") || fileReadAllow.equalsIgnoreCase("y");
                        if (!fileRead) { System.out.println("([Phoebe]: Read isn't allowed, ending command.)"); break;}
                        System.out.println("Allow user to download? [Yes or No : Default = Yes]");
                        String fileDownloadAllow = scanner.nextLine();
                        boolean fileDownload = fileDownloadAllow.isEmpty() || fileDownloadAllow.equalsIgnoreCase("yes")
                        || fileDownloadAllow.equalsIgnoreCase("y"); 
                        System.out.println("[Expiration Time] Expiry date/time ( DD/MM/YYYY HH:mm UTC, or leave empty for no expire)");
                        String fileExpiry = scanner.nextLine().trim();
                        StickyPolicy filePolicy = new StickyPolicy.Builder().allowRead(fileRead).allowDownload(fileDownload).expiryFromInput(fileExpiry).build();
                        try {
                            String ext = FileConversions.getExtension(fileFilePath);
                            String b64 = FileConversions.fileToB64(fileFilePath); 
                            String fileJson = jsonBuilder(username, fileReceiver, b64, "File", ext, filePolicy);
                            sendTo(fileReceiver, fileJson);
                        } catch (Exception e) {
                            System.out.println("[Phoebe]: "+ e.getMessage());
                        }
                        break;  
                        
                    case "/INFO":
                        System.out.println("Your ip is: " + localIp); 
                        System.out.println("Your port is: " + myPort);
                        String tableContents = String.join(",", dht.getTable().keySet()); 
                        System.out.println("Your current table Usernames are:" + tableContents);
                        break;

                    case "/HELP":
                        System.out.println("Below are all the commands you can use, alongside a small description of their uses.");
                        System.out.println("/Initalise - need to do this with a known user to initiate your tables");
                        System.out.println("/Connect - connect to a known user without JSON based security. Only use this feature if you wish to speak to multiple people in a potenitally non-secure way.");
                        System.out.println("/Update - updates all tables you are connected to, provided they accept");
                        System.out.println("/Message - send a message to a known user");
                        System.out.println("/Image - send an image to a known user");
                        System.out.println("/File - send a file to a known user");
                        System.out.println("/Info - tells you your networking infomation (Ip & Port)");
                        System.out.println("/Help - shows this list of commands");
                        System.out.println("/Exit - close Phoebe");                    
                        break;

                    case "/EXIT":
                        System.out.println("Closing Phoebe");
                        System.exit(0);
                        break; 
               default:
                   if (input.startsWith("/")){
                       System.out.println("Command not recognised, type /Help for listed commands");
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
            System.out.println("[Testing]: Looking for " + targetName + " in list");  // remember to remove this .
            for (Peer peer:peers){
                System.out.println("[Testing]: Found peer:" + peer.username + "");  // and this .
                if (peer.username.equals(targetName)){
                    peer.out.println(message);
                    return true;
                }
            }
        }
        try {
            DHT.PeerInfo peerInfo = dht.lookup(targetName);
            System.out.println("[Phoebe]: Not directly connected to " + targetName  + " - connecting via Previous DHT connection knowledge");     
            connectToPeer(peerInfo.ip, peerInfo.port);
            synchronized (peers) {
                for (Peer peer : peers){
                    System.out.println("[Testing]: " + peer.username + " " + peer.ip + ":" + peer.port); // remember to change from [Testing] .  remove after ..
                }
                for (Peer peer : peers) {
                    if (peer.ip.equals(peerInfo.ip) && peer.port == peerInfo.port){
                        peer.out.println(message);
                        return true;
                    }
                }
            }       
        } catch (Exception e) {
        }
        System.out.println("[Phoebe]: User "+ targetName + " Not found");
        return false;
    }


    // -- Json Code below -- 
   public static String jsonBuilder(String senderUsername, String receiverUsername, String message, String typeOfData, String fileName, StickyPolicy policy){
        
    JSONObject Crow = new JSONObject()
    .put("Proginator", senderUsername)
    .put("Receiver", receiverUsername)
    .put("Message", message)
    .put("FileName", fileName);

    JSONObject Oculus = new JSONObject()
    .put("Timestamp", Instant.now().getEpochSecond())
    .put("Type", typeOfData)  
    .put("Policy", policy.toJSON());

    JSONObject Combined = new JSONObject()
    .put("Crow", Crow)
    .put("Oculus", Oculus);
    return Combined.toString(); 
    }
   
    // Client thread not server . 
    private static void connectToPeer(String ip, int port) {
        synchronized (peers) {
            for (Peer peer :peers){
                if (peer.ip.equals(ip) && peer.port == port){
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
                System.out.println("[Testing]: Tables merged - Line 305");  // remove testing & line mention 
            } catch (Exception e){
                System.out.println("[Testing]: Tables failed to merge - 307"); // ditto to above x2
            }
        }
            String ready = input.readLine();
            if (!"READY".equals(ready)){
                System.out.println("[Phoebe]: Handshake failed");
                socket.close();
                return;
            }
            String peerUsername = "defaultName";
            int peerListenPort = port;
            synchronized (dht) {
                for (Map.Entry<String, DHT.PeerInfo> en : dht.getTable().entrySet()) {
                    if (en.getValue().ip.equals(ip)){
                        peerUsername = en.getKey();
                        break;
                    } 
                }
            }
            setupStreams(socket, out, input, true, peerUsername,peerListenPort);
            System.out.println("Connected to peer at " + ip + ":" + port);
        } catch (IOException e) {
            System.out.println("Failed to connect to " + ip + ":" + port);
        }
    }
    // New code processes username from connect->peer (for below)
    private static void setupStreams(Socket socket, PrintWriter out, BufferedReader in, boolean handShakeDone, String knownUsername, int listenport) throws IOException {
        peers.add(new Peer(knownUsername != null ? knownUsername :"defaultName", 
        socket.getInetAddress().getHostAddress(), 
        listenport, 
        out));
        new Thread(new PeerHandler(socket, out, in, handShakeDone, listenport)).start();
    }

    // Helper to send a message to everyone in the peers list
    private static void broadcast(String message) {
        synchronized (peers) {
            for (Peer peer : peers) {
                peer.out.println(message);
            }
        }
    }
    
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
                    PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    setupStreams(socket,out, in, false, null,0);
                    System.out.println("A new peer has connected to you!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // this needs HEAVY editing, its the biggest connection .... 12/04/26 -> This really should have been its own file, can i even actually do that now? Is it too big to *actually* move???
    // i think this is honestly the worst class ive ever written. I'm reading through it and genuinely crying at how messy it is and how it feels so unoptimised
    // not even just the class, this whole project i hate . It cant do what i wanted it to do i didnt understand 
    
    private static class PeerHandler implements Runnable {
        private Socket socket; 
        private BufferedReader in;
        private PrintWriter out;
        private final boolean handShakeDone;
        private final int peerListenPort;

        public PeerHandler(Socket socket, PrintWriter out, BufferedReader in, boolean handShakeDone, int peerListenPort) throws IOException {
            this.socket = socket;
            this.out = out;
            this.in = in;
            this.handShakeDone = handShakeDone;
            this.peerListenPort = peerListenPort;
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
                System.out.println("[Phoebe]: " + senderUsername + " " + "has connected.");
                } else 
                try{
                    String nonDefaultName = "defaultName";
                    for (Map.Entry<String, DHT.PeerInfo> en : dht.getTable().entrySet()) {
                        if (en.getValue().ip.equals(socket.getInetAddress().getHostAddress()) && en.getValue().port == peerListenPort){
                        nonDefaultName = en.getKey();
                        break;
                    }
                }
                final String realUsername = nonDefaultName;
                synchronized (peers) {
                    for (Peer peer : peers){
                        if (peer.out == out){
                            peers.remove(peer);
                            peers.add(new Peer( realUsername, socket.getInetAddress().getHostAddress(), peerListenPort, out));
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
                    JSONObject Oculus = json.getJSONObject("Oculus");
                    JSONObject Crow = json.getJSONObject("Crow");

                    String typeOfData = Oculus.getString("Type");
                    String sender = Crow.getString("Proginator");
                    String receiver = Crow.getString("Receiver");
                    if (!receiver.equals(username) && !receiver.equals("all")) {
                            continue;
                    }
                    switch (typeOfData) {   // < _ maybe change this variable name
                        case "Text":
                            StickyPolicy policy = StickyPolicy.fromJSON(Oculus.getJSONObject("policy"));
                            PolicyEnforcer enforcer = new PolicyEnforcer(policy);
                            if (!enforcer.canRead())        break;
                            System.out.println("[" + sender + "]: " + Crow.getString("message"));
                            break;

                        case "Image":
                            StickyPolicy imgPolicy = StickyPolicy.fromJSON(Oculus.getJSONObject("policy"));
                            PolicyEnforcer imgEnforcer = new PolicyEnforcer(imgPolicy);
                            if (!imgEnforcer.canRead())     break; 
                            System.out.println("["+ sender +"]: " + "send an image");
                            FileConversions.B64ToImage(sender,Crow.getString("message"), Crow.getString("fileName"));
                            break;
                            
                        case "File":   // do same as ^ when this one is made
                            StickyPolicy filePolicy = StickyPolicy.fromJSON(Oculus.getJSONObject("policy"));
                            PolicyEnforcer fileEnforcer = new PolicyEnforcer(filePolicy);
                            if (!fileEnforcer.canRead())    break; 
                            System.out.println("["+ sender +"]: " + "send a file");
                            FileConversions.B64ToFile(sender,Crow.getString("message"),Crow.getString("fileName"));
                            break;

                        case "Update_Request":
                                try {
                                    PendingRequest request = new PendingRequest(
                                        sender,
                                        out,
                                        Instant.now().getEpochSecond()
                                    );
                                    pendingRequests.add(request);
                                    System.out.println("[Phoebe]: " + sender + " has requested a DHT sync. Type /pending to review");
                                } catch (Exception e) {
                                    System.out.println("[Phoebe]: Failed to process Update Request.");
                                }
                                break;
                        case "Update_Response":
                                try {
                                    DHT receivedDHT = DHT.fromJson(Oculus.getJSONObject("dht"));
                                    dht.merge(receivedDHT);
                                    System.out.println("[Phoebe]: DHT table updated from" + sender);
                                } catch (Exception e) {
                                    System.out.println("[Phoebe]: DHT table update failed");
                                }
                                break;    
                        case "update_denied":
                            System.out.println("[Phoebe]: " + sender + " denied DHT sync request");
                            break;
                        
                        default:
                            System.out.println("[Phoebe]: " + sender  + " sent an unknown message type: " + typeOfData);
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
        // Random inner classes that dont really need to be in other classes since they are useful here ^-^
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
        
}
public static class PendingRequest {
        public final String requesterUsername;
        public final PrintWriter requesterOut;
        public final long timestamp;

        public PendingRequest(String requesterUsername, PrintWriter requesterOut, long timestamp){
            this.requesterUsername = requesterUsername;
            this.requesterOut = requesterOut;
            this.timestamp = timestamp;
        }
    }
    
}
