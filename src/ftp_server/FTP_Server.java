package ftp_server;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is an implementation of a basic FTP server
 * It's possible to connect it only if the user is known by the server.
 * Existing users are : login : tom / pswd : tom  ||  login : test / pswd : 123
 * To ficilitate the users management only for this project, users are created in the Constructor of FactoryUser
 * and a directory must be created manually in the directory "users" of this application.
 * FTP Server allows many commands which are visible in the class FTP_Request
 * @author Thomas Pierard
 */
public class FTP_Server {

    final int PORT_SERVER = 4000;
    ServerSocket sock = null;

    public void start() {
        try {
            sock = new ServerSocket(PORT_SERVER);
            System.out.println("Server FTP started on port : " + PORT_SERVER);
            
            // waiting for a new connection
            while (true) {
                Socket connSocket = sock.accept();
                FTP_Request r = new FTP_Request(connSocket);
                r.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(FTP_Server.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        FTP_Server s = new FTP_Server();
        s.start();
    }

}
