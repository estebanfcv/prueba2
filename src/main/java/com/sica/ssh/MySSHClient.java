/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sica.ssh;

/**
 *
 * @author ingenieria4
 */
import com.sshtools.j2ssh.ScpClient;
import com.sshtools.j2ssh.io.IOStreamConnector;
import com.sshtools.j2ssh.connection.*;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;
import com.sshtools.j2ssh.util.InvalidStateException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
//import org.apache.log4j.PropertyConfigurator;

public class MySSHClient {

    private SshClient ssh = null;
    private SshConnectionProperties properties = null;
    private SessionChannelClient session = null;
    private String hostName;
    private String userName;
    private String password;

    public MySSHClient(String hostName, String userName, String password) {
        this.hostName = hostName;
        this.userName = userName;
        this.password = password;
//        PropertyConfigurator.configure("/home/ingenieria4/Videos/e");
    }

    public boolean establecerConexion() {
        boolean conexion = false;
        try {
// Make a client connection
            ssh = new SshClient();
            properties = new SshConnectionProperties();
            properties.setHost(hostName);
// Connect to the host
            ssh.connect(properties, new IgnoreHostKeyVerification());
// Create a password authentication instance
            PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
            pwd.setUsername(userName);
            pwd.setPassword(password);
// Try the authentication
            int result = ssh.authenticate(pwd);
// Evaluate the result
            if (result == AuthenticationProtocolState.COMPLETE) {
                conexion = true;
                System.out.println("Connection Authenticated");
            }
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
        }
        return conexion;
    }//end of method.

    /**
     *
     * @param cmd
     * @return el resultado que arroja el comando
     */
    public String execCmd(String cmd) {
        String theOutput = "";
        try {
// The connection is authenticated we can now do some real work!
            session = ssh.openSessionChannel();
            if (session.executeCommand(cmd)) {
                IOStreamConnector output = new IOStreamConnector();
                java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                output.connect(session.getInputStream(), bos);
                session.getState().waitForState(ChannelState.CHANNEL_CLOSED);
                theOutput = bos.toString();
            } else {
            }
        } catch (InvalidStateException | IOException | InterruptedException e) {
            System.out.println("Exception : " + e.getMessage());
        }

        return theOutput;
    }

    public boolean copiarArchivoServidorRemoto(String hostname, String user, String password, File file, long size,
            String nombreLocal, String nombreRemoto) throws IOException {
        PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
        boolean operacionCorrecta;
        try {
            InputStream f = new FileInputStream(file);
            ssh.connect(hostname, new IgnoreHostKeyVerification());
            pwd.setUsername(user);
            pwd.setPassword(password);
            int ret = ssh.authenticate(pwd);
            if (ret == 4) {
                ScpClient scpClient = ssh.openScpClient();
                scpClient.put(f, size, nombreLocal, nombreRemoto);
                operacionCorrecta = true;
            } else {
                operacionCorrecta = false;
                throw new ConnectException("Error en la autenticacion de usuario");
            }
        } catch (IOException e) {
            e.printStackTrace();
            operacionCorrecta = false;
            throw new ConnectException("No se pudo conectar al Servidor = " + hostname);
        }
        return operacionCorrecta;
    }

    public void desconectar() throws IOException {
        session.close();
        ssh.disconnect();
    }
}
