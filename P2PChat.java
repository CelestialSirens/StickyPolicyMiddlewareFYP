import java.io.*;
import java.net.*;
import java.util.Scanner;

public class P2PChat {

    public static void main(String[] args) {
        Scanner userInput = new Scanner(System.in);
        System.out.println("--- Simple P2P Chat 1 - 1 Only ---");
        System.out.println("1. Host a chat (Server)");
        System.out.println("2. Join a chat (Client)"); 
        System.out.print("Choose an option: ");
        
        int choice = userInput.nextInt();
        userInput.nextLine(); // consume newline

        Socket socket = null;

        try {
            // CONNECTION SETUP
            if (choice == 1) {
                System.out.print("Enter port to listen on (e.g., 5000): ");
                int port = userInput.nextInt();
                userInput.nextLine();
                ServerSocket serverSocket = new ServerSocket(port);
                System.out.println("Waiting for peer to connect...");
                socket = serverSocket.accept(); // Blocks until connection is made
                System.out.println("Peer connected!");
            } else if (choice == 2) {
                System.out.print("Enter peer IP (e.g., 127.0.0.1): ");
                String ip = userInput.nextLine();
                System.out.print("Enter peer Port (e.g., 5000): ");
                int port = userInput.nextInt();
                userInput.nextLine();
                System.out.println("Connecting...");
                socket = new Socket(ip, port);
                System.out.println("Connected to peer!");
            } else {
                System.out.println("Invalid choice. Exiting.");
                return;
            }

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // Used to read data coming from the other person
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));


            Thread receiveThread = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println("Peer: " + msg);
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            });
            receiveThread.start();

            System.out.println("Type a message and press Enter to send (Type 'exit' to quit):");
            while (true) {
                String message = userInput.nextLine();
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
                out.println(message);
            }

            // CLEANUP
            socket.close();
            System.exit(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}