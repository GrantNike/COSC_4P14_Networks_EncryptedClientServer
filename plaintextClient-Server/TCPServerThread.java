import java.net.*;
import java.io.*;
/*
Grant Nike 
6349302
Dec 6th
Thread class handles a single client who has requested connection with the main class server. Server will recieve strings
from client and send strings to client in response. Client can also end the connection using keyword 'quit' 
*/

public class TCPServerThread extends Thread {
    private Socket clientSocket = null;

    public TCPServerThread(Socket socket) { //Thread takes socket connected to client
        super("TCPServerThread");//Calls runnable constructor 
        this.clientSocket = socket;
    }
    
    public void run() {
        try (
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); //out writes to client via socket
            BufferedReader in = new BufferedReader( //Takes input from client via socket
                new InputStreamReader(
                    clientSocket.getInputStream()));
        ) {
            System.out.println("Connection made with client on port: "+clientSocket.getPort());//Lets server side know a connection has been made with a new client
            String inputLine; //The next line of input from the client
            while ((inputLine = in.readLine()) != null) {//Read next line of input from client
                //If the client enters 'quit', let client know the connection is being terminated, close
                //the socket with the client, and stop taking new input. Since this only happens for a single
                //thread,the server will still be running and waiting for new clients
                if(inputLine.equals("quit") || inputLine.equals("n")){
                    out.println("Closing connection");
                    clientSocket.close();
                    System.out.println("Closed connection with client on port: " + clientSocket.getPort());
                    break;
                }
                //Otherwise simply print the client's response to the server side, 
                //and send thier response back to them all uppercase + thier port number 
                else{
                    System.out.println("Client: " + inputLine);
                    //out.println(inputLine.toUpperCase() + " " + clientSocket.getPort());
                    out.println(inputLine.toUpperCase());
                }
            }
            //If the socket is still open after input is done, close the socket
            if(!clientSocket.isClosed()) clientSocket.close();  
        } catch (IOException e) { //Catch IO exceptions for Printwriter and BufferedReader
            e.printStackTrace();
        }
    }
}