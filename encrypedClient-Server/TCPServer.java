import java.net.*;
import java.io.*;
/*
Grant Nike
6349302
Dec 6th
Server class begins listening for a client connection on given port, if no port is given default is 4000. 
Once connected the client and server will perform a Diffie-Hellman key exchange to produce a shared secret key. The Server
initiates the key exchange.The Server will then listen for AES encrypted strings from the client, decrypt them, and send back
the same String in all uppercase and AES encrypted.
*/
public class TCPServer{
    public static void main(String[] args) throws Exception {
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