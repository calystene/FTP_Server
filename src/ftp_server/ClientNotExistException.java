/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftp_server;

/**
 *
 * @author pierard
 */
class ClientNotExistException extends Exception {

    public ClientNotExistException(String msg) {
        super(msg);
    }
    
}
