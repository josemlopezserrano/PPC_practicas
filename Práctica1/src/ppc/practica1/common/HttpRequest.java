package ppc.practica1.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Representa una petición HTTP/1.1 recibida por el servidor.
 * Se construye leyendo línea a línea del InputStream del socket.
 */
public class HttpRequest {

    private final String method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;

    private HttpRequest(String method, String path, String version,
                        Map<String, String> headers) {
        this.method  = method;
        this.path    = path;
        this.version = version;
        this.headers = Collections.unmodifiableMap(headers);
    }

    // ------------------------------------------------------------------ //
    //  Parsing                                                             //
    // ------------------------------------------------------------------ //

    /**
     * Lee y parsea una petición HTTP desde el InputStream del socket.
     * Devuelve null si la conexión se cerró antes de recibir datos.
     */
    public static HttpRequest parse(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        // Línea de petición: "GET /ruta HTTP/1.1"
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            return null;
        }

        String[] parts = requestLine.split(" ", 3);
        if (parts.length < 3) {
            throw new IOException("Línea de petición malformada: " + requestLine);
        }
        String method  = parts[0].toUpperCase();
        String path    = parts[1];
        String version = parts[2];

        // Cabeceras: "Nombre: valor" hasta línea vacía
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                String name  = line.substring(0, colon).trim().toLowerCase();
                String value = line.substring(colon + 1).trim();
                headers.put(name, value);
            }
        }

        return new HttpRequest(method, path, version, headers);
    }

    // ------------------------------------------------------------------ //
    //  Getters                                                             //
    // ------------------------------------------------------------------ //

    public String getMethod()  { return method; }
    public String getPath()    { return path; }
    public String getVersion() { return version; }

    /** Devuelve el valor de una cabecera (case-insensitive) o null si no existe. */
    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }

    /**
     * Devuelve el valor de una cookie específica del encabezado Cookie,
     * o null si no existe.
     * Formato esperado: "nombre1=valor1; nombre2=valor2; ..."
     */
    public String getCookieValue(String cookieName) {
        String cookieHeader = headers.get("cookie");
        if (cookieHeader == null) return null;

        for (String part : cookieHeader.split(";")) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length == 2 && kv[0].trim().equals(cookieName)) {
                return kv[1].trim();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return method + " " + path + " " + version;
    }
}
