package ppc.practica1.server;

import ppc.practica1.common.Constants;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.net.Socket;
import java.security.KeyStore;

/**
 * Crea el SSLServerSocket con autenticación mutua (cliente + servidor).
 * Los keystores se cargan desde las rutas definidas en Constants.
 *
 * Se completa en la Fase 5, cuando tengamos los certificados generados.
 */
public class SslContextFactory {

    public static SSLServerSocket createServerSocket(int port) throws Exception {
        // Keystore del servidor (clave privada + certificado del servidor)
        KeyStore serverKs = loadKeyStore(Constants.SV_STORE, Constants.SV_PWD);
        // Truststore: certificado de la CA (para verificar el certificado del cliente)
        KeyStore caKs     = loadKeyStore(Constants.CA_STORE, Constants.CA_PWD);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(serverKs, Constants.SV_CERT_PWD);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);

        SSLContext ctx = SSLContext.getInstance("TLSv1.2");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        SSLServerSocketFactory factory = ctx.getServerSocketFactory();
        SSLServerSocket serverSocket   = (SSLServerSocket) factory.createServerSocket(port);

        // Exigir autenticación del cliente (autenticación mutua)
        serverSocket.setNeedClientAuth(true);

        return serverSocket;
    }

    public static Socket createClientSocket(String host, int port) throws Exception {
        // Keystore del cliente (clave privada + certificado del cliente)
        KeyStore clientKs = loadKeyStore(Constants.CL_STORE, Constants.CL_PWD);
        // Truststore: certificado de la CA (para verificar el certificado del servidor)
        KeyStore caKs     = loadKeyStore(Constants.CA_STORE, Constants.CA_PWD);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(clientKs, Constants.CL_CERT_PWD);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);

        SSLContext ctx = SSLContext.getInstance("TLSv1.2");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        SSLSocket socket = (SSLSocket) ctx.getSocketFactory().createSocket(host, port);
        socket.setEnabledProtocols(new String[]{"TLSv1.2"});
        return socket;
    }

    private static KeyStore loadKeyStore(String path, char[] password) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(path)) {
            ks.load(fis, password);
        }
        return ks;
    }
}
