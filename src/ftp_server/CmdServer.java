/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftp_server;

import java.io.File;

/**
 *
 * @author tom
 */
public class CmdServer {
    public static String ls() {
        return "";
    }
    
    public static String ls(String path) {
        String [] s = new File(path).list();
        String result = "";
        
        for (String item : s) {
            result += item + " | ";
        }
        return result;
    }
    
    public static String mkdir(String path, String name) {
       
        return "";
    }
    
    public static void rm(String name) {
        
    }
}
