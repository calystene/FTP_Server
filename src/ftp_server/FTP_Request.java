/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftp_server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manage and process an user connection and the differents request
 * to the FTP Server . It creates a new Thread for each new users. That allows
 * the management of multi-users connection
 *
 * @author Thomas PIERARD
 */
public class FTP_Request extends Thread {

    // The differents commands implemented
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

    // The values used to generate the port of passive connection
    private final int PASV1 = 160;
    private final int PASV2 = 175;

    // Connection variables
    /* Communication Channel */
    Socket socketComm;
    DataOutputStream outComm;
    BufferedReader inComm;

    /* Datas channel */
    static ServerSocket sPasvSocket = null;
    Socket socketData;
    DataOutputStream outData;
    BufferedReader inData;

    // Users variable
    String login;
    Client client;
    boolean pasv;
    String pwd;
    private String previousCommand;
    private String basedir;

    /**
     * Constructor of FTP_Request
     *
     * @param connSocket The port where the server is listening
     */
    FTP_Request(Socket connSocket) {
        try {
            socketComm = connSocket;
            client = null;
            outComm = new DataOutputStream(socketComm.getOutputStream());
            inComm = new BufferedReader(new InputStreamReader(socketComm.getInputStream()));
            pasv = false;
            basedir = new File("").getAbsoluteFile().getAbsolutePath();
            this.answer(220, "Server ready");
        } catch (IOException ex) {
            Logger.getLogger(FTP_Request.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Manage the type of request sended by the FTP client
     *
     * @param line The packet received across the port listen by the server
     * @throws IllegalCommandException
     * @throws IOException
     */
    void processRequest(String line) throws IllegalCommandException, IOException {
        String[] command = line.split("\\s");

        if (client == null) {
            switch (command[0].toUpperCase()) {
                case USER:
                    processUSER(command);
                    this.previousCommand = USER;
                    break;
                case PASS:
                    processPASS(command);
                    this.previousCommand = PASS;
                    break;
                case ABOR:
                    processABOR();
                    this.previousCommand = ABOR;
                    break;
                default:
                    this.answer(502, "Not implemented");
                    this.previousCommand = "";
                    break;
            }
        } else {
            switch (command[0].toUpperCase()) {
                case CWD:
                    processCWD(command);
                    this.previousCommand = CWD;
                    break;
                case LIST:
                    processLIST(command);
                    this.previousCommand = LIST;
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
                case ABOR:
                    processABOR();
                    this.previousCommand = ABOR;
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
    }

    /**
     * Run a new Thread
     */
    @Override
    public void run() {
        try {
            String line;
            while ((line = inComm.readLine()) != null) {
                System.out.println(" <-- " + line);
                this.processRequest(line);
            }
        } catch (IOException | IllegalCommandException e) {
        }
    }

    /**
     * Respond a status code and a message to the ftp client over the command
     * channel
     *
     * @param status : three digits status code
     * @param respond : free message explaining the answer
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

    /**
     * Check if the user's login sended by the client exist
     *
     * @param command user's login
     */
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

    /**
     * Check if the password sended by the client is attached to the Login
     * previously received
     *
     * @param command Informations sended by the FTP client
     */
    private void processPASS(String[] command) {
        if (command.length >= 2) {
            if (previousCommand.equals(USER)) {
                try {
                    String pswd = command[1];
                    client = FactoryUser.getInstance().seekClient(login, pswd);
                    pwd = basedir + client.getPathDirectory();
                    this.answer(230, "You're now connected");
                } catch (ClientNotExistException e) {
                    this.answer(530, "Password incorrect");
                }
            } else {
                answer(503, "Bad sequence of commands");
            }
        }
    }

    /**
     * Inform when a problem happen in the transaction
     */
    private void processABOR() {
        this.answer(426, "Transaction anormaly closed");
        this.answer(226, "");
    }

    /**
     * Change the path work directory by newest path sended by the FTP client
     * Commands allowed are 1st : absolute path like this "cd
     * /home/user/{pathToApp}/FTP_Server/users/" etc.. 2nd : access to parent
     * directory like this : "cd .." or "cd ../" 3rd : go forward with relative
     * path like this : "cd tom" or "cd tom/html" etc...
     *
     * @param command The new path work directory
     */
    private void processCWD(String[] command) {
        if (command.length < 2) {
            this.answer(500, "Syntax error");
        }

        String target = "";

        try {
            /* Check if the path begin by /, if it's true then the path is absolute path */
            if (command[1].startsWith("/")) {
                target = new File(command[1]).getCanonicalPath();
                pwd = target;

                /* Check if the path contains ".." (by example : ../), if it's true, pwd become the parent path */
            } else if (command[1].contains("..")) {
                File f = new File(pwd);
                System.out.println("Parent : " + f.getParent());
                target = f.getParent();
                pwd = target;

                /* If previously conditions are false, the value sended by FTP client is a relative path to go forward */
            } else {
                target += pwd + "/" + command[1];
                pwd = target;
            }
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
        } else { // the user have left basedir 
            this.answer(550, "Access denied.");
            return;
        }
        this.answer(500, "Uncatched error");
    }

    /**
     * Send to the FTP client the contents of the path work directory Commands
     * allowed are : 1st : "ls" to list the content of the pwd 2nd : "ls
     * aDirectory" to list the content of aDirectory. It's possible to do "ls
     * aDirectory/anotherDirectory" etc..
     *
     * @param command
     */
    private void processLIST(String[] command) {
        this.answer(125, "Proceed");
        String raw = "";
        // Construct the file list
        File dir = null;
        if (command.length >= 2) {
            dir = new File(pwd + "/" + command[1]);
        } else {
            dir = new File(pwd);
        }

        File[] filesList = dir.listFiles();
        for (File file : filesList) {
            if (file.isDirectory()) {
                raw += "d";
            } else {
                raw += "-";
            }

            raw += file.canRead() ? "r" : "-";
            raw += file.canWrite() ? "w" : "-";
            raw += file.canExecute() ? "x" : "-";
            raw += " ";
            raw += file.length();
            raw += " ";
            raw += file.getName() + "\r\n";
        }

        this.sendData(raw);
        this.answer(226, "Complete");
    }

    /**
     * Open the Data channel for active connection Get the informations about
     * ports to create the socket
     *
     * @param command Information sended by FTP client about ports
     */
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
            inData = new BufferedReader(new InputStreamReader(socketData.getInputStream()));
            this.answer(200, "Active data connection etablished");
        } catch (Exception e) {
            this.answer(500, "failed");
        }
    }

    /**
     * Open the Data channel for passive connection
     *
     * @param command
     */
    private void processPASV(String[] command) {
        try {
            /* Initialize once the sPasvSocket, which is shared between all the Threads (static)*/
            if (sPasvSocket == null) {
                int port = (PASV1 << 8) + PASV2;
                sPasvSocket = new ServerSocket(port);
            }

            this.answer(227, String.format("127,0,0,1,%d,%d", PASV1, PASV2));
            socketData = sPasvSocket.accept();
            outData = new DataOutputStream(socketData.getOutputStream());
            inData = new BufferedReader(new InputStreamReader(socketData.getInputStream()));
        } catch (IOException ex) {
            answer(500, "Passive connection failed");
        }
    }

    /**
     * Send the path work directory to the client
     *
     * @param command
     */
    private void processPWD(String[] command) {
        String raw = String.format("\"%s\" is the current working directory", this.pwd);
        this.answer(257, raw);
    }

    /**
     * Make a new repository at the path brought by the client
     *
     * @param command The path where the repository must be create
     */
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
            this.answer(503, "Name of new repository is required");
        }
    }

    /**
     * Allow the file transfer between the server to the client
     *
     * @param command The file to send
     */
    private void processRETR(String[] command) {
        if (command.length < 2) {
            this.answer(503, "Missing file name");
            return;
        }

        String pathToFile = pwd + "/" + command[1];

        try {
            InputStream ips = new FileInputStream(pathToFile);
            byte[] buffer = new byte[1024];
            int lg;
            this.answer(125, "Starting transfer");

            while ((lg = ips.read(buffer)) != -1) {
                outData.write(buffer, 0, lg);
            }
            outData.close();
            this.answer(226, "Datas transfer successful");
        } catch (FileNotFoundException ex) {
            this.answer(550, "File " + pathToFile + " doesn't exist");
        } catch (IOException ex) {
            this.answer(425, "Transfer failed");
        }

    }

    /**
     * Allow the file transfer between the client to the server
     *
     * @param command The file to store
     */
    private void processSTOR(String[] command) {
        if (command.length < 2) {
            this.answer(500, "Syntax error");
        }

        String filename = command[1];
        try {
            int data;
            FileOutputStream out = new FileOutputStream(pwd + "/" + filename);
            this.answer(110, "Ready to process");

            while ((data = this.inData.read()) != -1) {
                out.write(data);
            }
            out.close();
            this.answer(226, "Transfer completed");
        } catch (FileNotFoundException e) {
            this.answer(550, "File " + filename + " cannot be opened. Maybe it already exists ?");
        } catch (IOException e) {
            this.answer(631, "Cannot store the file");
        }
    }

    /**
     * Check if the type proposed by the client is accepted by the Server
     *
     * @param command
     */
    private void processTYPE(String[] command) {
        assert command.length >= 2;
        if (command[1].equals("I") || command[1].equals("A")) {
            this.answer(200, "Type accepted");
            return;
        }
        this.answer(502, "Type not implemented");
    }

    /**
     * Return the operating system of the server
     *
     * @param command
     */
    private void processSYST(String[] command) {
        this.answer(215, "UNIX FTP SERVER");
    }

    /**
     * Close the connection between the client and the server
     *
     * @param command
     * @throws IOException
     */
    private void processQUIT(String[] command) throws IOException {
        this.answer(221, "Connection closed");
        this.socketComm.close();
    }

}
