import java.io.*;
import java.net.*;

public class Peer {

    private int port;
    private ServerSocket serverSocket;

    public Peer(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
    }

    // --------------------------
    // SERVER PART (Accept connections)
    // --------------------------
    public void startServer() throws IOException{

        System.out.println("Peer started on port" + port);
      
                while (true) {
                    Socket socket = serverSocket.accept();
                   new Thread(new ClientHandler(socket)).start();
                }
        };

    
    class ClientHandler implements Runnable{
        private Socket socket;

        public ClientHandler(Socket socket){
            this.socket = socket;
        }
        public void run(){
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true)) {
                String message;
                while ((message = in.readLine()) !=null) {
                    System.out.println("Recived something:" + message);
                    out.println(message);
                }
            } catch (IOException exception){
                exception.printStackTrace();
            } finally{
                try{
                    socket.close();
                } catch (IOException exception){
                    exception.printStackTrace();
                }
            }

        }
    }
    // --------------------------
    // CLIENT PART (Connect to another peer)
    // --------------------------
    public void connectToPeer(String host, int port) {
        try (Socket socket = new Socket(host, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true); 
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))){
       out.println("Hello from:" + this.port);
       String respondString = in.readLine();
       System.out.println("Response :" +respondString);
        } catch (IOException exception){
            exception.printStackTrace();
        }
    }

    // --------------------------
    // MAIN
    // --------------------------
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Use Java peer <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        try{
            Peer peer = new Peer(port);
            peer.startServer();
        } catch (IOException exception){
            exception.printStackTrace();
        }
    }
}
