package ppc.practica1.common;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Construye y envía una respuesta HTTP/1.1.
 * Uso típico:
 *   HttpResponse.ok(htmlBody)
 *               .setCookie("historial", valor, "/")
 *               .write(socket.getOutputStream());
 */
public class HttpResponse {

    private static final DateTimeFormatter HTTP_DATE =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
                             .withZone(ZoneOffset.UTC);

    private final int statusCode;
    private final String statusMessage;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private byte[] body = new byte[0];

    private HttpResponse(int statusCode, String statusMessage) {
        this.statusCode    = statusCode;
        this.statusMessage = statusMessage;
        // Cabeceras por defecto
        headers.put("Date",       HTTP_DATE.format(ZonedDateTime.now(ZoneOffset.UTC)));
        headers.put("Server",     "PPC-Practica1/1.0");
        headers.put("Connection", "close");
    }

    // ------------------------------------------------------------------ //
    //  Métodos de fábrica                                                  //
    // ------------------------------------------------------------------ //

    public static HttpResponse ok(String htmlBody) {
        HttpResponse r = new HttpResponse(200, "OK");
        r.setHtmlBody(htmlBody);
        return r;
    }

    public static HttpResponse error(int code, String message) {
        HttpResponse r = new HttpResponse(code, message);
        String html = "<html><body><h1>" + code + " " + message + "</h1></body></html>";
        r.setHtmlBody(html);
        return r;
    }

    // ------------------------------------------------------------------ //
    //  Configuración                                                       //
    // ------------------------------------------------------------------ //

    private void setHtmlBody(String html) {
        this.body = html.getBytes(StandardCharsets.UTF_8);
        headers.put("Content-Type",   "text/html; charset=UTF-8");
        headers.put("Content-Length", String.valueOf(body.length));
    }

    public HttpResponse setHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Añade una cabecera Set-Cookie con path.
     * El atributo "secure" se añade solo si secure=true (para HTTPS).
     */
    public HttpResponse setCookie(String name, String value,
                                  String path, boolean secure) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value);
        sb.append("; path=").append(path);
        if (secure) sb.append("; secure");
        headers.put("Set-Cookie", sb.toString());
        return this;
    }

    // ------------------------------------------------------------------ //
    //  Serialización y envío                                              //
    // ------------------------------------------------------------------ //

    /**
     * Escribe la respuesta completa (cabeceras + cuerpo) en el OutputStream.
     */
    public void write(OutputStream out) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusMessage).append("\r\n");
        for (Map.Entry<String, String> h : headers.entrySet()) {
            sb.append(h.getKey()).append(": ").append(h.getValue()).append("\r\n");
        }
        sb.append("\r\n");

        byte[] head = sb.toString().getBytes(StandardCharsets.US_ASCII);
        out.write(head);
        out.write(body);
        out.flush();
    }

    /** Devuelve la respuesta como String (cabeceras + body) para logging/debug. */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusMessage).append("\r\n");
        for (Map.Entry<String, String> h : headers.entrySet()) {
            sb.append(h.getKey()).append(": ").append(h.getValue()).append("\r\n");
        }
        sb.append("\r\n");
        sb.append(new String(body, StandardCharsets.UTF_8));
        return sb.toString();
    }
}
