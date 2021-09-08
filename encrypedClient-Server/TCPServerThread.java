import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
/*
Grant Nike 
6349302
Sept 17th
Thread class handles a single client who has requested connection with the main class server. Server will recieve strings
from client and send strings to client in response. Client can also end the connection using keyword 'quit' 
*/

public class TCPServerThread extends Thread {
    private Socket clientSocket = null;

    public TCPServerThread(Socket socket) throws Exception{ //Thread takes socket connected to client
        super("TCPServerThread");//Calls runnable constructor 
        this.clientSocket = socket;
    }
    
    public void run(){
        try (
            OutputStream outputStream = clientSocket.getOutputStream();
            InputStream inputStream = clientSocket.getInputStream();
            PrintWriter out =
                new PrintWriter(outputStream, true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(inputStream));
        ) {
            System.out.println("Connection made with client on port: "+clientSocket.getPort());//Lets server side know a connection has been made with a new client
            // Create public and private key pair for Diffie-Hellman key exchange
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("EC");
            keyGenerator.initialize(128);
            KeyPair kp = keyGenerator.genKeyPair();
            PublicKey publickey = kp.getPublic();
            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(kp.getPrivate());
            // Create initial vector for encryption
            byte[] IV = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(IV);
            //Send public key to client
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(publickey);
            //Receive client's public key
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            PublicKey clientPublicKey = (PublicKey) objectInputStream.readObject();
            //Create new shared secret key which will serve as the symmetric key for AES encryption
            keyAgreement.doPhase(clientPublicKey, true);
            byte[] key = keyAgreement.generateSecret();
            //Send initial vector to client
            out.println(Base64.getEncoder().encodeToString(IV));
            //Start reading client input
            String inputLine; //The next line of input from the client
            while ((inputLine = in.readLine()) != null) {//Read next line of input from client
                String decodedMessage = decrypt(Base64.getDecoder().decode(inputLine),key,IV);
                //If the client enters 'quit', let client know the connection is being terminated, close
                //the socket with the client, and stop taking new input. Since this only happens for a single
                //thread,the server will still be running and waiting for new clients
                if(decodedMessage.equals("quit") || decodedMessage.equals("n")){
                    String endConnectionMessage = "Closing Connection";
                    out.println(Base64.getEncoder().encodeToString(encrypt(endConnectionMessage.getBytes(),key,IV)));
                    clientSocket.close();
                    System.out.println("Closed connection with client on port: " + clientSocket.getPort());
                    break;
                }
                //Otherwise simply print the client's response to the server side, 
                //and send thier response back to them all uppercase + thier port number 
                else{
                    System.out.println("Client Encoded: "+inputLine);
                    System.out.println("Client: " + decodedMessage);//Print what client said to server side
                    decodedMessage = decodedMessage.toUpperCase();
                    out.println(Base64.getEncoder().encodeToString(encrypt(decodedMessage.getBytes(),key,IV)));//Echos back everthing the client says
                }
            }
            //If the socket is still open after input is done, close the socket
            if(!clientSocket.isClosed()) clientSocket.close();  
        } catch (Exception e) { //Catch IO exceptions for Printwriter and BufferedReader
            e.printStackTrace();
        }
    }

    public static byte[] encrypt (byte[] plaintext,byte[] key,byte[] iv ) throws Exception{
        //Create instance of cipher to encrypt message
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //Create key spec and initial vector spec
        SecretKeySpec key_spec = new SecretKeySpec(key, "AES");
        IvParameterSpec iv_spec = new IvParameterSpec(iv);
        //Put cipher in encrypt mode 
        c.init(Cipher.ENCRYPT_MODE, key_spec, iv_spec);
        //Encrypt text
        byte[] ciphertext = c.doFinal(plaintext);
        
        return ciphertext;
    }
    
    public static String decrypt (byte[] ciphertext,byte[] key,byte[] iv) throws Exception{
        //Create instance of cipher to encrypt text
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //Create key spec and initial vector spec
        SecretKeySpec key_spec = new SecretKeySpec(key, "AES");
        IvParameterSpec iv_spec = new IvParameterSpec(iv);
        //Put cipher in decrypt mode 
        c.init(Cipher.DECRYPT_MODE, key_spec, iv_spec);
        //Decrypt text
        byte[] plaintext = c.doFinal(ciphertext);
        
        return new String(plaintext);
    }
}