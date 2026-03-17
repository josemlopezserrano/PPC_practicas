package ppc.practica1.client;

import ppc.practica1.common.Constants;

import java.io.*;
import java.nio.file.*;

/**
 * Persistencia de la cookie de historial en disco.
 *
 * Guarda y carga el valor de la cookie en el fichero definido en
 * Constants.COOKIE_FILE (cookies.dat). Permite que el cliente mantenga
 * el historial entre ejecuciones.
 */
public class CookieStore {

    /**
     * Carga el valor de la cookie desde disco.
     * Devuelve null si el fichero no existe o está vacío.
     */
    public static String load() {
        Path path = Paths.get(Constants.COOKIE_FILE);
        if (!Files.exists(path)) return null;
        try {
            String value = Files.readString(path).trim();
            return value.isEmpty() ? null : value;
        } catch (IOException e) {
            System.err.println("[CookieStore] No se pudo leer " + Constants.COOKIE_FILE + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Guarda el valor de la cookie en disco, sobreescribiendo el anterior.
     */
    public static void save(String cookieValue) {
        try {
            Files.writeString(Paths.get(Constants.COOKIE_FILE), cookieValue);
        } catch (IOException e) {
            System.err.println("[CookieStore] No se pudo guardar " + Constants.COOKIE_FILE + ": " + e.getMessage());
        }
    }
}
