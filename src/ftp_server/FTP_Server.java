package ftp_server;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierard
 */
public class FTP_Server {

    final int PORT_SERVER = 4000;
    ServerSocket sock = null;

    public void start() {
        try {
            sock = new ServerSocket(PORT_SERVER);
            System.out.println("Server FTP started on port : " + PORT_SERVER);

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
