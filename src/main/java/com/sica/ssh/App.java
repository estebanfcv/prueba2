package com.sica.ssh;

import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.Scanner;

public class App {

    private static SshClient ssh;

    public static void main(String[] args) throws IOException, InterruptedException {
        MySSHClient sshc = new MySSHClient("192.168.120.230", "ubnt", "ubnt");
        
        if (sshc.establecerConexion()) {
            File f = new File("/home/ingenieria4/WMAN/plantillas/system.cfg");
            sshc.copiarArchivoServidorRemoto("192.168.120.230", "ubnt", "ubnt", f, f.length(), f.getName(), "/tmp/system.cfg");
            sshc.execCmd("cfgmtd -f /tmp/system.cfg -w");
            System.out.println(sshc.execCmd("/usr/etc/rc.d/rc.softrestart save"));
            sshc.desconectar();
        } else {
            System.out.println("No se establecio la conexion");
        }
        leerArchivoServidorRemoto("192.168.120.3", "root", "sica2012");
    }

    public static void leerArchivoServidorRemoto(String hostName, String user, String password) throws IOException {
        ssh = new SshClient();
        PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
        ssh.connect(hostName, new IgnoreHostKeyVerification());
        pwd.setUsername(user);
        pwd.setPassword(password);
        int ret = ssh.authenticate(pwd);
        if (ret == 4) {
            ScpChannel scp = new ScpChannel("");
            try (InputStream s = scp.get("/root/troll.png", ssh)) {
//                System.out.println("s vale::::: " + convertStreamToString(s));
                crearArchivo(s);
            }
            ssh.disconnect();
        } else {
            throw new ConnectException("Error en la autenticacion de usuario");
        }
    }

    public static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().trim() : "";
    }

    public static void crearArchivo(InputStream is) {
        try {
            File f = new File("/home/ingenieria4/Videos/y_si_hacemos_un_muneco.png");
            try (OutputStream out = new FileOutputStream(f)) {
                byte buf[] = new byte[1024];
                int len;
                while ((len = is.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            is.close();
            System.out.println("File is created...................................");
        } catch (IOException e) {
           e.printStackTrace();
        }
    }
}
