// Basic Java program
import java.net.*;
import java.io.*;

public class Server {

    private Socket soc = null;
    private ServerSocket serverSoc = null;
    private DataInputStream input = null;

    // Constructor - Remember this is where objects are initalised 
    public Server(int port) {
        try {
            serverSoc = new ServerSocket();
            System.err.println("Started Server");
            System.err.println("Waiting for connection");

            soc = serverSoc.accept();
            System.err.println("Connected");

        } catch (Exception e) {
        }
    } 
























    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}

/* 

Potentially do this in a socket program with a client class here and a server running on here with the other computers running just the client connecting into the server 

*/