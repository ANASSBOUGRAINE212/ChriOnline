package client;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;

	public class ServerConnection {
	    private Socket socketClient;
	    private DataOutputStream sortieVersServeur;
	    private BufferedReader entreeDepuisServeur;

	    public void connect(String host, int port) throws Exception {
	        socketClient = new Socket(host, port);
	        sortieVersServeur = new DataOutputStream(socketClient.getOutputStream());
	        entreeDepuisServeur = new BufferedReader(
	            new InputStreamReader(socketClient.getInputStream())
	        );
	        System.out.println("Connecte au serveur " + host + ":" + port);
	    }

	    public String sendRequest(String requete) throws Exception {
	        sortieVersServeur.writeBytes(requete + "\n");
	        return entreeDepuisServeur.readLine();
	    }

	    public void disconnect() throws Exception {
	        socketClient.close();
	        System.out.println("Deconnecte du serveur.");
	    }
	}

