import java.io.*;
import java.net.*;
import java.util.*;

/*
Grant Nike 
6349302
Sept 17th
Client class Requests a connection with a given host on a given port. Once connected it can
send strings to the server and recieve strings from the server.
*/
public class client {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) { //User must include host name and port number of server as arguments
            System.err.println(
                "Usage: java client <host name> <port number>");
            System.exit(1);
        }
        String host = args[0];//Name of host is first argument
        int port = Integer.parseInt(args[1]);//Port number is second argument
 
        try (
            Socket socket = new Socket(host, port);//Create new socket for connection with server
            PrintWriter out = //Sends strings to server
                new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in =//Recieves strings from server
                new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn =//Allows user to type a continuous stream of input
                new BufferedReader(
                    new InputStreamReader(System.in))
        ) {
            System.out.println("Connection made with server!");//Lets user know a connection has been established
            //Run performance analysis if test parameter was entered
            if(args.length == 3){
                long averageDelay = 0;
                int test_plaintext_size = Integer.parseInt(args[2]);
                for(int i = 0;i<30;i++){
                    byte[] array = new byte[test_plaintext_size]; 
                    new Random().nextBytes(array);
                    String testInput = new String(array, "UTF-8");
                    long start = System.nanoTime();
                    out.println(testInput);
                    String serverMessage = in.readLine();
                    long stop = System.nanoTime();
                    long Delay = stop-start;
                    averageDelay += Delay;
                    System.out.println("Delay: "+Delay/1000000.0+" milliseconds");
                }
                System.out.println("Average End-End Delay over 30 runs: "+(averageDelay/30.0)/1000000.0+" milliseconds");
            }
            String userInput;
            while ((userInput = stdIn.readLine()) != null) { //Allow user to send strings to server continuously
                long start = System.nanoTime();
                out.println(userInput);
                String serverMessage = in.readLine();
                long stop = System.nanoTime();
                long RTT = stop-start;
                System.out.println("End-End Delay: "+RTT/1000000.0+" milliseconds");
                System.out.println("Server: " + serverMessage);
                if(userInput.equals("quit") || userInput.equals("n")){//Stop sending input if user quits connection
                    break;
                }
            }
        } catch (UnknownHostException e) { //Host can't be found
            System.err.println("Host: " + host + " is unknown.");
            System.exit(1);
        } catch (IOException e) { //No IO from our side or host
            System.err.println("Couldn't get I/O for the connection to " +
                host);
            System.exit(1);
        } 
    }
}