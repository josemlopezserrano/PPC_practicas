package ppc.practica1.server;

/**
 * Punto de entrada del servidor.
 * Lanza dos hilos: uno para HTTP y otro para HTTPS.
 */
public class Server {

    public static void main(String[] args) {
        Thread httpThread  = new Thread(new HttpListener(),  "HTTP-Listener");
        Thread httpsThread = new Thread(new HttpsListener(), "HTTPS-Listener");

        httpThread.setDaemon(false);
        httpsThread.setDaemon(false);

        httpThread.start();
        httpsThread.start();

        System.out.println("Servidor arrancado. Pulsa Ctrl+C para detener.");
    }
}
