# FTP_Server
Java FTP Server

-- Author Thomas Pierard - M1 MIAGE

## Informations about the server
* To run the FTP server, execute in a shell : java -jar FTP_Server.jar
* To ensure the good running, the directory "users" which contains both directory "tom" and "test" must be in the same directory that the executable file "FTP_Server.jar"
* The server doesn't allow anonymous connection, so login and password are always required to access at the server. 2 accounts are created (login / pswd) : ("tom" / "tom") and ("test" / "123")
* Implemented Commands can be seen in the javadoc, in the class FTP_Request