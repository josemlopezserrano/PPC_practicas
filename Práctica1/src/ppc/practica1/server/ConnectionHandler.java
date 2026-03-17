package ppc.practica1.server;

import ppc.practica1.common.Constants;
import ppc.practica1.common.CookieManager;
import ppc.practica1.common.HttpRequest;
import ppc.practica1.common.HttpResponse;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;

/**
 * Hilo que procesa una única conexión HTTP/HTTPS.
 * Recibe el socket ya aceptado, parsea la petición, genera la respuesta y cierra.
 *
 * Es compartido por HttpListener y HttpsListener: la lógica de negocio
 * no depende de si la conexión es cifrada o no.
 */
public class ConnectionHandler implements Runnable {

    private final Socket socket;
    private final boolean secure;   // true → HTTPS (añade atributo secure a la cookie)

    public ConnectionHandler(Socket socket, boolean secure) {
        this.socket = socket;
        this.secure = secure;
    }

    @Override
    public void run() {
        try {
            handle();
        } catch (IOException e) {
            System.err.println("[Handler] Error procesando conexión: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private void handle() throws IOException {
        // 1. Parsear petición
        HttpRequest req = HttpRequest.parse(socket.getInputStream());
        if (req == null) return;

        System.out.println("[" + (secure ? "HTTPS" : "HTTP") + "] "
                + socket.getInetAddress().getHostAddress()
                + " → " + req);

        // 2. Solo procesamos GET (por ahora)
        if (!req.getMethod().equals("GET")) {
            HttpResponse.error(405, "Method Not Allowed")
                        .write(socket.getOutputStream());
            return;
        }

        // 3. Leer cookie de historial y actualizar
        String cookieValue   = req.getCookieValue(Constants.COOKIE_NAME);
        String newCookieValue = CookieManager.updateHistory(cookieValue, req.getPath());

        // 4. Construir HTML con el historial actualizado
        LinkedHashMap<String, Integer> history = CookieManager.parse(newCookieValue);
        String html = CookieManager.buildHtml(history);

        // 5. Enviar respuesta con Set-Cookie
        HttpResponse resp = HttpResponse.ok(html)
                .setCookie(Constants.COOKIE_NAME, newCookieValue, "/", secure);

        resp.write(socket.getOutputStream());

        // Log completo de la respuesta (útil para el vídeo de la práctica)
        System.out.println(resp);
    }
}
