package itts.volterra.quintab;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;

//importo G e P dal Server
import static itts.volterra.quintab.Server.DEFAULT_G;
import static itts.volterra.quintab.Server.DEFAULT_P;

/**
 * Gestisce le connessioni in arrivo
 */
public class ClientHandler implements Runnable{
    private static int threadCounter = 0;
    public boolean isObjectCreated;
    private final Logger log = LogManager.getLogger(ClientHandler.class);
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private BigInteger serverPrivateKey;
    private BigInteger serverPublicKey;
    private RSA rsa;

    /**
     * Costruttore
     *
     * @param client Socket del client da gestire
     */
    public ClientHandler(Socket client) {
        this.socket = client;
        Thread.currentThread().setName("CltHnd-" + threadCounter);
        threadCounter++;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            rsa = new RSA();

            //genera chiave privata del server
            serverPrivateKey = generatePrivateKey();

            //set the flag to true if everything goes successfully (it won't)
            this.isObjectCreated = true;
        } catch (IOException e) {
            log.error("Errore durante l'inizializzazione del gestore client", e);
            //set the flag to false if initialization fails (as always does)
            this.isObjectCreated = false;
        } finally {
            log.debug("Oggetto CH creato: {}", isObjectCreated);
        }
    }

    @Override
    public void run() {
        if (!isObjectCreated){      //se il costruttore non è stato eseguito
            log.warn("L’oggetto ClientHandler non è stato costruito con successo");
        } else {
            try {
                //gestisce Diffie-Hellman
                runDiffieHellmanAlgorithm();
            } catch (IOException e) {
                log.error("Errore durante lo scambio Diffie-Hellman", e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log.error("Errore durante la chiusura del socket", e);
                }
            }
        }
    }

    /**
     * Genero la chiave privata
     *
     * @return Chiave privata
     */
    private BigInteger generatePrivateKey() {
        return new BigInteger(1024, new SecureRandom());
    }

    private void runDiffieHellmanAlgorithm() throws IOException {
        /*
        Nel messaggio, i primi 3 char servono per capire cosa si vuole fare: DH = si sta eseguendo Diffie-Hellman,
        poi si usa '--' come carattere di separazione
         */

        //invio parametri pubblici al client
        out.println("DH-P--" + DEFAULT_P);
        out.println("DH-G--" + DEFAULT_G);

        //calcolo chiave pubblica del server
        serverPublicKey = DEFAULT_G.modPow(serverPrivateKey, DEFAULT_P);

        //cripto la chiave pubblica con RSA
        //BigInteger encryptedServerPublicKey = rsa.encrypt(serverPublicKey);
        out.println("DH-SERVER_PUBLIC--" + serverPublicKey);

        //attendo la chiave pubblica del client
        String clientPublicKeyStr = null;
        String line;
        while ((line = in.readLine()) != null) {
            if (line.startsWith("DH-CLIENT_PUBLIC--")) {
                clientPublicKeyStr = line.substring(18);
                break;
            }
        }

        if (clientPublicKeyStr != null) {
            //decripto la chiave pubblica del client
            BigInteger clientPublicKey = new BigInteger(clientPublicKeyStr);
            //BigInteger clientPublicKey = rsa.decrypt(encryptedClientPublicKey);

            //calcolo la chiave condivisa
            BigInteger sharedKey = clientPublicKey.modPow(serverPrivateKey, DEFAULT_P);
            log.info("Chiave condivisa calcolata: {}", sharedKey);

            // Invia conferma
            out.println("DH-COMPLETE");
        }
    }

    /**
     * Scrive nell'output stream di un Socket
     *
     * @param socket Socket
     * @param message Messaggio da scrivere
     * @throws IOException Cagati addosso
     */
    private void sendMessageToSocket(Socket socket, String message) {
        PrintWriter pW = null;                                                     //writer per scrivere
        boolean initialized = false;

        while (!initialized) {
            try {
                pW = new PrintWriter(socket.getOutputStream(), true);
                initialized = true;
            } catch (IOException e) {
                log.warn("Errore durante la creazione dell'output stream");
            }
        }

        pW.println(message);                                                //invio conferma ricezione
        pW.close();                                                         //chiudo stream
    }
}
