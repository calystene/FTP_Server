/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftp_server;

import java.util.HashMap;

/**
 *
 * @author pierard
 */
public class FactoryUser {
    HashMap<Integer,Client> collection;
    static private FactoryUser singleton;

    private FactoryUser() {
        collection = new HashMap<Integer,Client>();
        
        // Ajout manuel d'un Client pour les tests
        Client c = new Client("tom","tom");
        collection.put(c.hashCode(),c);
    }

    static public FactoryUser getInstance() {
        if(singleton==null) {
            singleton = new FactoryUser();
        }
        
        return singleton;
    }
    
    
    public Client seekClient(String login, String pswd) throws ClientNotExistException {
        Client c = new Client (login, pswd);
        
        if(collection.containsKey(c.hashCode())) return c;
        
        throw new ClientNotExistException("Login/password combination unknown\n");
    }
}

