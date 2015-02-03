/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftp_server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierard
 */
public class FTP_Request extends Thread {
    Socket socket;
    Client client;
    DataOutputStream out;
    BufferedReader in;
    
    FTP_Request(Socket connSocket) {
        try {
            socket = connSocket;
            client = null;
            out = new DataOutputStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(FTP_Request.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void processRequest() throws IllegalCommandException {
        try {
            String login = "";
            String pswd = "";
            String msg = "";
            boolean identKO = true;
            
            try {
                while(identKO) {
                    
                    // On vérifie que le msg est de type USER et on le stock
                    out.writeBytes("220 Login required\n");
                    msg = in.readLine();
                    if(!msg.contains("USER")) throw new IllegalCommandException();
                    login = msg.substring(5);
                    
                    
                    // On vérifie que le msg est de type PASS et on le stock
                    out.writeBytes("331 " + login + "'s password required\n");
                    msg = in.readLine();
                    if(!msg.contains("PASS")) throw new IllegalCommandException();
                    pswd = msg.substring(5);
                    
                    
                    // On vérifie l'existence du client, si KO génère une exception
                    client = FactoryUser.getInstance().seekClient(login,pswd);
                    out.writeBytes("230 You're now connected !\n");
                    
                    identKO = false;
                    run();
                }
            } catch (ClientNotExistException ex) {
                out.writeBytes("530 " + ex.getMessage());
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(FTP_Request.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        try {
            if(client != null) {
                String msg = "";

                msg = in.readLine();
                System.out.println(msg);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(FTP_Request.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
