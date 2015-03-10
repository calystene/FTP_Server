/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftp_server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tom
 */
public class FTP_RequestTest {

    static private FTP_Server server;
    static private OutputStream out;
    static private BufferedReader in;

    public FTP_RequestTest() {
        try {

            // Server
            //server = new FTP_Server();
            //server.start();
            // Client
            Socket socket = new Socket("127.0.0.1", 4000);
            out = new DataOutputStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //out.write(new String("").getBytes());
            //in.readLine();
        } catch (IOException ex) {
            Logger.getLogger(FTP_RequestTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    /*
     @Test
     public void testProcessRequest() throws Exception {
     System.out.println("processRequest");
     String line = "";
     FTP_Request instance = null;
     instance.processRequest(line);
     assertTrue(true);
     }
     */

    @Test
    public void testProcessRequestUser() throws Exception {
        System.out.println("processRequest(USER tom)");

        String toSend = "";
        out.write(toSend.getBytes());

        String toReceive = in.readLine();
        System.out.println(toReceive);

        if (toReceive.contains("220")) {
            System.out.println("What are you waiting for ?");
            out.write(toSend.getBytes());
            toReceive = in.readLine();
            
            assertTrue(toReceive.contains("331"));
        }
        /*toSend = "USER tom";
         out.write(toSend.getBytes());
         toReceive = in.readLine();
         assertTrue(toReceive.contains("331"));*/
    }

}
