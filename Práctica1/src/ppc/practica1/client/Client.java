package ppc.practica1.client;

import ppc.practica1.common.Constants;
import ppc.practica1.common.HttpResponse;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Cliente HTTP/HTTPS de consola.
 *
 * El usuario introduce host, puerto y ruta por separado.
 * El cliente construye la petición GET, la envía al servidor,
 * vuelca la respuesta completa por pantalla y persiste la cookie.
 *
 * HTTPS: pendiente de activar en Fase 5 (requiere certificados de Fase 4).
 */
public class Client {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String cookieValue = CookieStore.load();

        if (cookieValue != null) {
            System.out.println("[Cliente] Cookie cargada desde disco: " + cookieValue);
        }

        System.out.println("=== Cliente HTTP PPC ===");
        System.out.println("Escribe 'salir' en la ruta para terminar.\n");

        while (true) {
            // --- Pedir datos al usuario ---
            System.out.print("Host   [localhost]: ");
            String host = scanner.nextLine().trim();
            if (host.isEmpty()) host = "localhost";

            System.out.print("Puerto [" + Constants.HTTP_PORT + "]: ");
            String portStr = scanner.nextLine().trim();
            int port;
            try {
                port = portStr.isEmpty() ? Constants.HTTP_PORT : Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                System.out.println("Puerto no válido, usando " + Constants.HTTP_PORT);
                port = Constants.HTTP_PORT;
            }

            System.out.print("Ruta   [/]: ");
            String path = scanner.nextLine().trim();
            if (path.isEmpty()) path = "/";
            if (path.equalsIgnoreCase("salir")) break;
            if (!path.startsWith("/")) path = "/" + path;

            // --- Construir y mostrar la petición ---
            String request = buildRequest(host, port, path, cookieValue);
            System.out.println("\n--- Petición enviada ---");
            System.out.print(request);

            // --- Enviar petición y recibir respuesta ---
            try (Socket socket = new Socket(host, port)) {
                // Enviar
                OutputStream out = socket.getOutputStream();
                out.write(request.getBytes(StandardCharsets.US_ASCII));
                out.flush();
                socket.shutdownOutput();

                // Recibir: leer todo hasta que el servidor cierre la conexión
                String response = readResponse(socket.getInputStream());

                System.out.println("--- Respuesta recibida ---");
                System.out.println(response);

                // Extraer Set-Cookie y persistir
                cookieValue = extractSetCookie(response, Constants.COOKIE_NAME);
                if (cookieValue != null) {
                    CookieStore.save(cookieValue);
                    System.out.println("[Cliente] Cookie actualizada: " + cookieValue);
                }

            } catch (IOException e) {
                System.err.println("[Cliente] Error de conexión: " + e.getMessage());
            }

            System.out.println();
        }

        System.out.println("Cliente terminado.");
        scanner.close();
    }

    // ------------------------------------------------------------------ //
    //  Construcción de la petición HTTP                                   //
    // ------------------------------------------------------------------ //

    /**
     * Construye el texto de la petición GET.
     * Añade la cabecera Cookie si hay historial previo.
     *
     * Ejemplo:
     *   GET /ruta HTTP/1.1\r\n
     *   Host: localhost:8080\r\n
     *   Cookie: historial=...\r\n
     *   Connection: close\r\n
     *   \r\n
     */
    private static String buildRequest(String host, int port,
                                       String path, String cookieValue) {
        StringBuilder sb = new StringBuilder();
        sb.append("GET ").append(path).append(" HTTP/1.1\r\n");
        sb.append("Host: ").append(host).append(":").append(port).append("\r\n");
        if (cookieValue != null && !cookieValue.isEmpty()) {
            sb.append("Cookie: ").append(Constants.COOKIE_NAME)
              .append("=").append(cookieValue).append("\r\n");
        }
        sb.append("Connection: close\r\n");
        sb.append("\r\n");
        return sb.toString();
    }

    // ------------------------------------------------------------------ //
    //  Lectura de la respuesta                                            //
    // ------------------------------------------------------------------ //

    /**
     * Lee la respuesta completa (cabeceras + cuerpo) hasta que el servidor
     * cierra la conexión (Connection: close).
     */
    private static String readResponse(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int read;
        while ((read = in.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    // ------------------------------------------------------------------ //
    //  Extracción de Set-Cookie                                           //
    // ------------------------------------------------------------------ //

    /**
     * Busca la cabecera Set-Cookie en la respuesta y devuelve el valor
     * de la cookie con el nombre indicado, o null si no existe.
     *
     * Formato esperado: Set-Cookie: historial=valor; path=/
     */
    private static String extractSetCookie(String response, String cookieName) {
        for (String line : response.split("\r\n")) {
            if (line.toLowerCase().startsWith("set-cookie:")) {
                String cookiePart = line.substring("set-cookie:".length()).trim();
                // cookiePart = "historial=valor; path=/"
                for (String segment : cookiePart.split(";")) {
                    String[] kv = segment.trim().split("=", 2);
                    if (kv.length == 2 && kv[0].trim().equalsIgnoreCase(cookieName)) {
                        return kv[1].trim();
                    }
                }
            }
        }
        return null;
    }
}
