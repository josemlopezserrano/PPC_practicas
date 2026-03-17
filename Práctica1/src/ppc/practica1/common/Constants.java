package ppc.practica1.common;

public final class Constants {

    private Constants() {}

    // Puertos
    public static final int HTTP_PORT  = 8080;
    public static final int HTTPS_PORT = 4430;

    // Separadores del historial en la cookie
    public static final String ENTRY_SEP = "|";   // entre entradas
    public static final String COUNT_SEP = ":";   // entre recurso y contador

    // Nombre de la cookie de historial
    public static final String COOKIE_NAME = "historial";

    // Keystores / truststores
    public static final String SV_STORE    = "cert/servidor.ks";
    public static final String CA_STORE    = "cert/ca.ks";
    public static final String CL_STORE    = "cert/cliente.ks";

    // Contraseñas (mismas para todos los almacenes en esta práctica)
    public static final char[] SV_PWD      = "changeit".toCharArray();
    public static final char[] SV_CERT_PWD = "changeit".toCharArray();
    public static final char[] CA_PWD      = "changeit".toCharArray();
    public static final char[] CL_PWD      = "changeit".toCharArray();
    public static final char[] CL_CERT_PWD = "changeit".toCharArray();

    // Alias dentro de los keystores
    public static final String SV_ALIAS = "servidor";
    public static final String CL_ALIAS = "cliente";

    // Fichero de persistencia de cookies del cliente
    public static final String COOKIE_FILE = "cookies.dat";
}
