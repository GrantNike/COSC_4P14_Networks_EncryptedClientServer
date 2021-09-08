import java.net.*;
import java.io.*;
/*
Grant Nike 
6349302
Sept 17th
Main class creates a TCP ServerSocket, listens for new clients, and creates a new thread to handle each new client.
*/
public class TCPServer{
    public static void main(String[] args) throws IOException {
        //Set port to arguement if one was given, otherwise default port is 4000
        int port = (args.length != 1)? 4000 : Integer.parseInt(args[0]);

        boolean listening = true;//True while the server is still listening for a new client to request a connection
        
        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("Server listening on port: " + port);
            //Waits for client to request connection
            while (listening) {
                new TCPServerThread(ss.accept()).start(); //Create new thread to handle each client
            }
        } catch (IOException e) { //Happens when user gives wrong port number 
            System.err.println("Unable to listen on port " + port);
            System.exit(-1);
        }
    }
}