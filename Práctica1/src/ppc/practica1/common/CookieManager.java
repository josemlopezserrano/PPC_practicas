package ppc.practica1.common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gestiona el historial de recursos almacenado en la cookie.
 *
 * Formato de la cookie (valor):
 *   recurso1:3|recurso2:1|recurso3:2
 *   ^nombre  ^count ^sep_entradas
 *
 * Separadores definidos en Constants:
 *   ENTRY_SEP = "|"   (entre entradas)
 *   COUNT_SEP = ":"   (entre nombre y contador)
 */
public class CookieManager {

    /**
     * Parsea el valor de la cookie historial y añade/incrementa el recurso dado.
     * Si cookieValue es null o vacío se crea un historial nuevo.
     *
     * @param cookieValue  valor actual de la cookie (puede ser null)
     * @param newResource  recurso a añadir (la ruta de la petición)
     * @return nuevo valor de cookie con el historial actualizado
     */
    public static String updateHistory(String cookieValue, String newResource) {
        LinkedHashMap<String, Integer> history = parse(cookieValue);
        history.merge(newResource, 1, Integer::sum);
        return format(history);
    }

    /**
     * Parsea el valor de la cookie y devuelve el mapa ordenado recurso→contador.
     * Si el valor es null o vacío devuelve un mapa vacío.
     */
    public static LinkedHashMap<String, Integer> parse(String cookieValue) {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        if (cookieValue == null || cookieValue.isEmpty()) return map;

        for (String entry : cookieValue.split("\\" + Constants.ENTRY_SEP)) {
            String[] kv = entry.split(Constants.COUNT_SEP, 2);
            if (kv.length == 2) {
                try {
                    map.put(kv[0], Integer.parseInt(kv[1]));
                } catch (NumberFormatException e) {
                    map.put(kv[0], 1);
                }
            } else if (!kv[0].isEmpty()) {
                map.put(kv[0], 1);
            }
        }
        return map;
    }

    /**
     * Serializa el mapa de historial al formato de cookie.
     */
    public static String format(LinkedHashMap<String, Integer> history) {
        if (history.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : history.entrySet()) {
            if (sb.length() > 0) sb.append(Constants.ENTRY_SEP);
            sb.append(e.getKey()).append(Constants.COUNT_SEP).append(e.getValue());
        }
        return sb.toString();
    }

    /**
     * Genera el cuerpo HTML de la respuesta a partir del historial.
     *
     * @param history      mapa actualizado recurso→contador
     * @param lastResource recurso de la petición actual (se muestra como "último solicitado")
     */
    public static String buildHtml(LinkedHashMap<String, Integer> history, String lastResource) {
        if (lastResource == null || lastResource.isEmpty()) lastResource = "(ninguno)";
        int totalRequests   = history.values().stream().mapToInt(Integer::intValue).sum();

        StringBuilder rows = new StringBuilder();
        for (Map.Entry<String, Integer> e : history.entrySet()) {
            rows.append("    <tr><td>").append(e.getKey())
                .append("</td><td>").append(e.getValue())
                .append("</td></tr>\n");
        }

        return "<!DOCTYPE html>\n"
             + "<html lang=\"es\">\n"
             + "<head><meta charset=\"UTF-8\">"
             + "<title>Historial de Accesos</title>"
             + "<style>body{font-family:sans-serif;margin:2em}"
             + "table{border-collapse:collapse}td,th{border:1px solid #ccc;padding:6px 12px}"
             + "th{background:#eee}</style></head>\n"
             + "<body>\n"
             + "<h1>Historial de Accesos</h1>\n"
             + "<p><b>Último recurso solicitado:</b> " + lastResource + "</p>\n"
             + "<p><b>Total de peticiones:</b> " + totalRequests + "</p>\n"
             + "<h2>Lista de recursos</h2>\n"
             + "<table>\n"
             + "  <tr><th>Recurso</th><th>Peticiones</th></tr>\n"
             + rows
             + "</table>\n"
             + "</body></html>\n";
    }
}
