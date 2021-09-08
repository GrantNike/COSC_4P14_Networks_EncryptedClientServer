import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/*
Grant Nike
6349302
Dec 6th
Client class requests a connection with a given host on a given port. Once connected the client and server will perform 
a Diffie-Hellman key exchange to produce a shared secret key. The client can then send AES encrypted strings to the server, 
and recieve and decrypt strings from the server.
*/
public class client {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) { //User must include host name and port number of server as arguments
            System.err.println(
                "Usage: java client <host name> <port number>");
            System.exit(1);
        }
        String host = args[0];//Name of host is first argument
        int port = Integer.parseInt(args[1]);//Port number is second argument

        try (
            Socket socket = new Socket(host, port);//Create new socket for connection with server
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            PrintWriter out = //Sends strings to server
                new PrintWriter(outputStream,true);
            BufferedReader in =//Recieves strings from server
                new BufferedReader(
                    new InputStreamReader(inputStream));
            BufferedReader stdIn =//Allows user to type a continuous stream of input
                new BufferedReader(
                    new InputStreamReader(System.in));
        ) {
            System.out.println("Connection made with server!");//Lets user know a connection has been established
            // Create public and private key pair for Diffie-Hellman key exchange
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("EC");
            keyGenerator.initialize(128);
            KeyPair kp = keyGenerator.genKeyPair();
            PublicKey publickey = kp.getPublic();
            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(kp.getPrivate());
            //Receive server's public key
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            PublicKey serverPublicKey = (PublicKey) objectInputStream.readObject();
            //Send public key to server
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(publickey);
            //Create new shared secret key which will serve as the symmetric key for AES encryption
            keyAgreement.doPhase(serverPublicKey, true);
            byte[] key = keyAgreement.generateSecret();
            //Get initial vector from server
            byte[] IV = Base64.getDecoder().decode(in.readLine());
            //Run performance analysis if test parameter was entered
            if(args.length == 3){
                long averageDelay = 0;
                int test_plaintext_size = Integer.parseInt(args[2]);
                for(int i = 0;i<30;i++){
                    //Create test string using letters and numbers
                    int L = 48; int R = 122;
                    String testInput = new Random().ints(L, R + 1)
                    .filter(j -> (j <= 57 || j >= 65) && (j <= 90 || j >= 97))
                    .limit(test_plaintext_size)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
                    //Start timing delay
                    long start = System.nanoTime();
                    String encryptedMessage = Base64.getEncoder().encodeToString(encrypt(testInput.getBytes(),key,IV));
                    //Send test string to server
                    out.println(encryptedMessage);
                    //Read and decrypt server response
                    String serverMessage = in.readLine();
                    String decryptedMessage = decrypt(Base64.getDecoder().decode(serverMessage),key,IV);
                    //Stop timing delay
                    long stop = System.nanoTime();
                    long delay = stop-start;
                    averageDelay += delay;
                    System.out.println("End-End Delay: "+delay/1000000.0+" milliseconds");
                }
                System.out.println("Average End-End Delay over 30 runs: "+(averageDelay/30.0)/1000000.0+" milliseconds");
            }
            String userInput;
            while ((userInput = stdIn.readLine()) != null) { //Allow user to send strings to server continuously
                long start = System.nanoTime();
                String encryptedMessage = Base64.getEncoder().encodeToString(encrypt(userInput.getBytes(),key,IV));
                out.println(encryptedMessage);
                String serverMessage = in.readLine();
                if(serverMessage == null){
                    System.out.println("Server closed connnection.");
                    break;
                }
                String decryptedMessage = decrypt(Base64.getDecoder().decode(serverMessage),key,IV);
                long stop = System.nanoTime();
                long RTT = stop-start;
                System.out.println("Server: " + decryptedMessage);
                System.out.println("RTT: "+RTT/1000000.0+" milliseconds");
                System.out.println("Server Encoded: "+serverMessage);
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
            System.err.println(e.getMessage());
            System.exit(1);
        } 
    }

    public static byte[] encrypt (byte[] plaintext,byte[] key,byte[] iv ) throws Exception{
        //Create instance of cipher to encrypt text
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
    
    public static String decrypt (byte[] ciphertext, byte[] key,byte[] iv) throws Exception{
        //Create instance of cipher to decrypt given text
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