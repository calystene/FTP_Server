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
public class Client {
    String login;
    String pswd;
    String pathDirectory;

    public Client(String login, String pswd) {
        this.login = login;
        this.pswd = pswd;
    }

    public Client(String login, String pswd, String path) {
        this.login = login;
        this.pswd = pswd;
        this.pathDirectory = path;
    }
    
    public String getLogin() {
        return login;
    }
    
    public String getPathDirectory() {
        return pathDirectory;
    }
    
    public void setPathDirectory(String path) {
        this.pathDirectory = path;
    }
    
    public void setLogin(String login) {
        this.login = login;
    }

    public String getPswd() {
        return pswd;
    }

    public void setPswd(String pswd) {
        this.pswd = pswd;
    }

    @Override
    public int hashCode() {
        return Math.abs(login.hashCode() * pswd.hashCode());
    }
    
}
