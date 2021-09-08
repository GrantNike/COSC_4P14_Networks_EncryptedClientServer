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
Dec 6th
Server class begins listening for a client connection on given port, if no port is given default is 4000. 
Once connected the client and server will perform a Diffie-Hellman key exchange to produce a shared secret key. The Server
initiates the key exchange.The Server will then listen for AES encrypted strings from the client, decrypt them, and send back
the same String in all uppercase and AES encrypted.
*/
public class Server {
    public static void main(String[] args) throws Exception {
        //Set port to arguement if one was given, otherwise default port is 4000
        int port = (args.length != 1)? 4000 : Integer.parseInt(args[0]);
         
        ServerSocket serverSocket = new ServerSocket(port);//Create new server socket for connection with a client
        System.out.println("Server listening on port: " + port); //Lets user on server side know server is running
        try (
            Socket clientSocket = serverSocket.accept();
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
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String decodedMessage = decrypt(Base64.getDecoder().decode(inputLine),key,IV);
                System.out.println("Client Encoded: "+inputLine);
                System.out.println("Client: " + decodedMessage);//Print what client said to server side
                decodedMessage = decodedMessage.toUpperCase();
                out.println(Base64.getEncoder().encodeToString(encrypt(decodedMessage.getBytes(),key,IV)));//Echos back everthing the client says
            }
            serverSocket.close();//Make sure to close socket when done with it
        } catch (IOException e) { //Couldn't listen on port provided
            System.out.println("Couldn't listen on port: "+ port);
            System.out.println(e.getMessage());
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
        //Encrypt message
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