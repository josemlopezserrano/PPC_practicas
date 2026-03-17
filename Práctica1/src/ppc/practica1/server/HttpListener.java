package ppc.practica1.server;

import ppc.practica1.common.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Hilo que escucha conexiones HTTP (no cifradas) en el puerto definido en Constants.
 * Por cada conexión acepta lanza un ConnectionHandler en un hilo separado.
 */
public class HttpListener implements Runnable {

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(Constants.HTTP_PORT)) {
            System.out.println("[HTTP] Escuchando en puerto " + Constants.HTTP_PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ConnectionHandler(socket, false)).start();
            }
        } catch (IOException e) {
            System.err.println("[HTTP] Error en el listener: " + e.getMessage());
        }
    }
}
