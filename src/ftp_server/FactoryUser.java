/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftp_server;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pierard
 */
public class FactoryUser {
    HashMap<Integer,Client> collection;
    static private FactoryUser singleton;

    private FactoryUser() {
        collection = new HashMap<Integer,Client>();
        
        // Ajout manuel de Clients pour les tests
        Client c = new Client("tom","tom","/users/tom");
        collection.put(c.hashCode(),c);
        c = new Client("test","123","/users/test");
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
        
        if(collection.containsKey(c.hashCode())) return collection.get(c.hashCode());
        
        throw new ClientNotExistException("Login/password combination unknown\n");
    }
    
    public boolean existLogin(String login) throws ClientNotExistException {
        for(Map.Entry<Integer,Client> e : collection.entrySet()) {
            if(e.getValue().getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }
}


