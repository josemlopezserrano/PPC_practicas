package ppc.practica1.server;

import ppc.practica1.common.Constants;

import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.net.Socket;

/**
 * Hilo que escucha conexiones HTTPS (TLSv1.2) en el puerto definido en Constants.
 * Delega la creación del SSLServerSocket en SslContextFactory.
 * La lógica de negocio es idéntica a HTTP: reutiliza ConnectionHandler con secure=true.
 */
public class HttpsListener implements Runnable {

    @Override
    public void run() {
        try {
            SSLServerSocket serverSocket = SslContextFactory.createServerSocket(Constants.HTTPS_PORT);
            System.out.println("[HTTPS] Escuchando en puerto " + Constants.HTTPS_PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ConnectionHandler(socket, true)).start();
            }
        } catch (Exception e) {
            System.err.println("[HTTPS] Error en el listener: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
