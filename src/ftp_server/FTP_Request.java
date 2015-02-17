/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftp_server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierard
 */
public class FTP_Request extends Thread {

    // Commande implémentées
    private final String SYST = "SYST";
    private final String PASV = "PASV";
    private final String USER = "USER";
    private final String PASS = "PASS";
    private final String PORT = "PORT";
    private final String PWD = "PWD";
    private final String LIST = "LIST";
    private final String MKD = "MKD";
    private final String ABOR = "ABOR";
    private final String CWD = "CWD";
    private final String RETR = "RETR";
    private final String STOR = "STOR";
    private final String TYPE = "TYPE";
    private final String QUIT = "QUIT";

    // Variables de connexion
    /* Canal de communication */
    Socket socketComm;
    DataOutputStream outComm;
    BufferedReader inComm;

    /* Canal de transfert de donnée */
    Socket socketData;
    DataOutputStream outData;
    BufferedReader inData;

    // Variables users
    String login;
    Client client;
    boolean pasv;
    String pwd;
    private String previousCommand;
    private String basedir;

    FTP_Request(Socket connSocket) {
        try {
            socketComm = connSocket;
            client = null;
            outComm = new DataOutputStream(socketComm.getOutputStream());
            inComm = new BufferedReader(new InputStreamReader(socketComm.getInputStream()));
            pasv = false;
            basedir = new File("").getAbsoluteFile().getAbsolutePath();
            this.answer(220, "ready");
        } catch (IOException ex) {
            Logger.getLogger(FTP_Request.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void processRequest(String line) throws IllegalCommandException, IOException {
        String[] command = line.split("\\s");

        switch (command[0].toUpperCase()) {
            case CWD:
                processCWD(command);
                this.previousCommand = CWD;
                break;
            case LIST:
                processLIST(command);
                this.previousCommand = LIST;
                break;
            case USER:
                processUSER(command);
                this.previousCommand = USER;
                break;
            case PASS:
                processPASS(command);
                this.previousCommand = PASS;
                break;
            case PORT:
                processPORT(command);
                this.previousCommand = PORT;
                break;
            case PASV:
                processPASV(command);
                this.previousCommand = PASV;
                break;
            case PWD:
                processPWD(command);
                this.previousCommand = PWD;
                break;
            case MKD:
                processMKD(command);
                this.previousCommand = MKD;
                break;
            case RETR:
                processRETR(command);
                this.previousCommand = RETR;
                break;
            case STOR:
                processSTOR(command);
                this.previousCommand = STOR;
                break;
            case SYST:
                processSYST(command);
                this.previousCommand = SYST;
                break;
            case TYPE:
                processTYPE(command);
                this.previousCommand = TYPE;
                break;
            case QUIT:
                processQUIT(command);
                this.previousCommand = QUIT;
                break;
            default:
                this.answer(502, "Not implemented");
                this.previousCommand = "";
                break;
        }

    }

    @Override
    public void run() {
        try {
            System.out.println("Début run");
            String line;
            System.out.println("Ici");
            while ((line = inComm.readLine()) != null) {
                System.out.println("Line : " + line);
                System.out.println(" <-- " + line);
                this.processRequest(line);
            }
        } catch (IOException e) {
            Logger.getLogger(FTP_Request.class.getName()).log(Level.SEVERE, null, e);
        } catch (IllegalCommandException ex) {
            Logger.getLogger(FTP_Request.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Respond a status code and a message to the ftp client over the command
     * channel
     *
     * @param status : three digits status code
     * @param respond : free message explaining the answer
     * @throws IOException : the client cannot be reached
     */
    public void answer(int status, String respond) {
        try {
            String raw = status + " " + respond + "\r\n";
            this.outComm.writeBytes(raw);
            System.out.println(" --> " + raw);
        } catch (IOException e) {
            System.out.println("cannot answer to client !");
        }
    }

    /**
     * Send data to the client over the data channel
     *
     * @param data : a byte array
     */
    private void sendData(byte[] data) {
        try {
            /* Send data as */
            this.outData.write(data, 0, data.length);
            System.out.println("D--> " + data.toString());
            this.socketData.close();
        } catch (IOException e) {
            System.out.println("cannot answer to client !");
        }
    }

    /**
     * Send data to the client over the data channel
     *
     * @param data : string to be sent, will be converted to a byte array
     */
    private void sendData(String data) {
        this.sendData(data.getBytes());
    }

    private void processUSER(String[] command) {
        if (command.length >= 2) {
            try {
                login = command[1];
                if (FactoryUser.getInstance().existLogin(login)) {
                    this.answer(331, login + " OK. Password required.");
                }
            } catch (ClientNotExistException ex) {
                this.answer(530, "Login inconnu");
            }

        } else {
            this.answer(530, "Erreur paramètre login manquant");
        }

    }

    private void processPASS(String[] command) {
        if (command.length >= 2) {
            if (previousCommand.equals(USER)) {
                try {
                    String pswd = command[1];
                    Client c = FactoryUser.getInstance().seekClient(login, pswd);
                    pwd = basedir + c.getPathDirectory();
                    this.answer(230, "You're now connected");
                } catch (ClientNotExistException e) {
                    this.answer(530, "Password incorrect");
                }
            } else {
                answer(503, "Bad sequence of commands");
            }
        }
    }

    private void processABOR() {
        this.answer(426, "Transaction anormaly closed");
        this.answer(226, "");
    }

    private void processCWD(String[] command) {
        if (command.length < 2) {
            this.answer(500, "Syntax error");
        }

        String target = "";

        try {
            target = new File(command[1]).getCanonicalPath();
            pwd = target;
        } catch (IOException e) {
            this.answer(550, "This directory does not exist.");
            return;
        }

        /* Check for path transversal disclosure : we do not allow to go upper than the directory we started the server */
        if (target.startsWith(this.basedir)) {
            // on verifie que le pathname est OK
            if (new File(target).exists()) {
                pwd = target;
                this.answer(250, "Change directory to " + target);
                return;
            }
        } else { // l utilisateur est sorti du basedir
            this.answer(550, "Access denied.");
            return;
        }
        this.answer(500, "Uncatched error");
    }

    private void processLIST(String[] command) {
        this.answer(125, "Proceed");
        String raw = "";
        // Construct the file list
        File dir = null;
        if (command.length >= 2) {
            dir = new File(command[1]);
        } else {
            dir = new File(pwd);
        }
        System.out.println("List directory " + dir);
        String[] filesList = dir.list();
        for (String file : filesList) {
            raw += file + "\r\n";
        }
        // Okay, send it
        this.sendData(raw);
        this.answer(226, "Complete");
    }

    /**
     * Retourne le contenu du répertoire courant à l'utilisateur
     */
    private void processLIST() throws IOException {
        String command[] = {"", pwd};
        processLIST(command);
    }

    private void processPORT(String[] command) {
        String[] netAddress = command[1].split(",");
        if (netAddress.length != 6) {
            this.answer(500, "Syntax error");
        }
        int remote_port = (Integer.parseInt(netAddress[4]) << 8) + Integer.parseInt(netAddress[5]);
        String remote_ip = String.format("%s.%s.%s.%s", netAddress[0], netAddress[1], netAddress[2], netAddress[3]);
        System.out.println("Opening socket to " + remote_ip + ":" + remote_port);
        try {
            this.socketData = new Socket(InetAddress.getByName(remote_ip), remote_port);
            OutputStream os = this.socketData.getOutputStream();
            this.outData = new DataOutputStream(os);
            this.answer(200, "Active data connection etablished");
        } catch (Exception e) {
            this.answer(500, "failed");
        }
    }

    private void processPASV(String[] command) {
        int valeur1 = 195;
        int valeur2 = 149;
        int port = (valeur1 << 8) + valeur2;
        try {
            ServerSocket sSocket = new ServerSocket(port);
            this.answer(227, "127,0,0,1,195,149");
            socketData = sSocket.accept();
            outData = new DataOutputStream(socketData.getOutputStream());
            inData = new BufferedReader(new InputStreamReader(socketData.getInputStream()));
        } catch (IOException ex) {
            answer(500, "failed");
        }
    }

    private void processPWD(String[] command) {
        String raw = String.format("\"%s\" is the current working directory", this.pwd);
        this.answer(257, raw);
    }

    private void processMKD(String[] command) {
        if (command.length >= 2) {
            try {
                File f = new File(pwd + "/" + command[1]);
                f.mkdirs();
                this.answer(257, "\"" + f.getCanonicalPath() + "\" was created");
            } catch (IOException ex) {
                this.answer(451, "Error while creating repository");
            }
        } else {
            this.answer(503, "Name of new directory is required");
        }
    }

    private void processRETR(String[] command) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void processSTOR(String[] command) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void processTYPE(String[] command) {
        assert command.length >= 2;
        if (command[1].equals("I") || command[1].equals("A")) {
            this.answer(200, "Type accepted");
            return;
        }
        this.answer(502, "Type not implemented");
    }

    private void processSYST(String[] command) {
        this.answer(215, "UNIX FTP SERVER");
    }

    private void processQUIT(String[] command) throws IOException {
        this.answer(221, "Goodbye");
        this.socketComm.close();
    }

}
