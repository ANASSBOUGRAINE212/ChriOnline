package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AuthMenu {
	 private ServerConnection conn;
	 private BufferedReader entreeUtilisateur;
	 private String userId = null;

	 public AuthMenu(ServerConnection conn) {
	        this.conn = conn;
	        this.entreeUtilisateur = new BufferedReader(
	            new InputStreamReader(System.in)
	        );
	    }

	    // Retourne le userId si connecte, null sinon
	    public String afficherMenu() throws Exception {
	        String choix;
	        do {
	            System.out.println("   CHRIONLINE - BIENVENUE");
	            System.out.println("1. Se connecter");
	            System.out.println("2. S'inscrire");
	            System.out.println("3. Quitter");
	            System.out.print("Votre choix: ");
	            choix = entreeUtilisateur.readLine();

	            if (choix.equals("1")) {
	                userId = seConnecter();
	                if (userId != null) return userId;
	            } else if (choix.equals("2")) {
	                sInscrire();
	            } else if (choix.equals("3")) {
	                System.out.println("Au revoir !");
	                conn.disconnect();
	                System.exit(0);
	            } else {
	                System.out.println("Choix invalide. Reessayez.");
	            }
	        } while (true);
	    }

	    private String seConnecter() throws Exception {
	        System.out.print("Email: ");
	        String email = entreeUtilisateur.readLine();
	        System.out.print("Mot de passe: ");
	        String motDePasse = entreeUtilisateur.readLine();

	        String reponse = conn.sendRequest("LOGIN:" + email + ":" + motDePasse);
	        System.out.println("Recu du serveur: " + reponse);

	        if (reponse.startsWith("SUCCESS:")) {
	            String uid = reponse.split(":")[1];
	            System.out.println("Connexion reussie ! Bienvenue (ID: " + uid + ")");
	            return uid;
	        } else {
	            System.out.println("Echec connexion: " + reponse);
	            return null;
	        }
	    }

	    private void sInscrire() throws Exception {
	        System.out.print("Nom d'utilisateur: ");
	        String username = entreeUtilisateur.readLine();
	        System.out.print("Email: ");
	        String email = entreeUtilisateur.readLine();
	        System.out.print("Mot de passe: ");
	        String motDePasse = entreeUtilisateur.readLine();

	        String reponse = conn.sendRequest(
	            "REGISTER:" + username + ":" + email + ":" + motDePasse
	        );
	        System.out.println("Recu du serveur: " + reponse);

	        if (reponse.equals("SUCCESS")) {
	            System.out.println("Inscription reussie ! Vous pouvez vous connecter.");
	        } else {
	            System.out.println("Echec inscription: " + reponse);
	        }
	    }
	}


