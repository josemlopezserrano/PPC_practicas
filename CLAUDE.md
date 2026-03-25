# CLAUDE.md — Práctica 1 PPC

Contexto completo del proyecto para retomar el trabajo sin perder información.

---

## Qué es este proyecto

Práctica 1 de **Programación para las Comunicaciones**.
Implementar desde cero un servidor y un cliente HTTP/HTTPS en Java usando **sockets TCP directamente** (sin librerías de alto nivel).
La aplicación es un **servicio de Historial de accesos** que usa cookies como único mecanismo de estado.

El servidor **no guarda nada en memoria entre peticiones** — todo el estado viaja en la cookie.

---

## Flujo de la aplicación

```
Cliente Java / Navegador
        |
        | GET /recurso HTTP/1.1
        | Cookie: historial=recurso1:3|recurso2:1   (si ya hay historial)
        v
   Servidor Java
   (hilo HTTP puerto 8080 / hilo HTTPS puerto 4430)
        |
        | Extrae /recurso de la petición
        | Lee cookie → actualiza historial → genera HTML + Set-Cookie
        v
   HTTP/1.1 200 OK
   Set-Cookie: historial=recurso1:3|recurso2:1|recurso3:1; path=/

   <html>Último: recurso3 | Total: 5 | Tabla: recurso1×3, recurso2×1, recurso3×1</html>
```

---

## Formato de la cookie

```
historial=recurso1:3|recurso2:1|recurso3:2
           ^nombre ^count  ^ENTRY_SEP(|)  ^COUNT_SEP(:)
```

Separadores definidos en `Constants.java`:
- `ENTRY_SEP = "|"` — entre entradas
- `COUNT_SEP = ":"` — entre nombre de recurso y contador

---

## Estructura del proyecto

```
Práctica1/
├── cert/
│   └── demoCA/          (archivos OpenSSL de la CA)
└── src/ppc/practica1/
    ├── common/           Fase 1 — clases compartidas servidor+cliente
    │   ├── Constants.java
    │   ├── HttpRequest.java
    │   ├── HttpResponse.java
    │   └── CookieManager.java
    ├── server/           Fase 2 — servidor HTTP/HTTPS
    │   ├── Server.java
    │   ├── HttpListener.java
    │   ├── HttpsListener.java
    │   ├── ConnectionHandler.java
    │   └── SslContextFactory.java
    └── client/           Fase 3 — cliente Java de consola
        ├── Client.java
        └── CookieStore.java
```

---

## Puertos y configuración (Constants.java)

| Constante | Valor |
|---|---|
| `HTTP_PORT` | 8080 |
| `HTTPS_PORT` | 4430 |
| `COOKIE_NAME` | `"historial"` |
| `COOKIE_FILE` | `"cookies.dat"` |
| `SV_STORE` | `"cert/servidor.ks"` |
| `CA_STORE` | `"cert/ca.ks"` |
| `CL_STORE` | `"cert/cliente.ks"` |
| Contraseñas | `"changeit"` (todos los keystores) |
| `SV_ALIAS` | `"servidor"` |
| `CL_ALIAS` | `"cliente"` |

---

## Arquitectura de procesamiento (detalle importante)

Los **Listeners no leen ningún byte** de la conexión. Solo hacen `accept()` y entregan el `Socket` a `ConnectionHandler`. Esto es deliberado: si el Listener procesara él mismo la petición, no podría aceptar nuevas conexiones mientras tanto.

```
HttpListener / HttpsListener
  └── accept() → entrega Socket a ConnectionHandler (nuevo hilo)
        └── HttpRequest.parse(socket.getInputStream())   ← aquí se leen los bytes
              └── CookieManager.parse / updateHistory / buildHtml
                    └── HttpResponse.ok().setCookie().write(socket.getOutputStream())
                          └── socket.close()
```

El flag `secure` (true/false) que recibe `ConnectionHandler` solo afecta al atributo `secure` de la cookie `Set-Cookie`. La lógica de negocio es idéntica para HTTP y HTTPS.

---

## Responsabilidad de cada clase

### Paquete `common` (Fase 1)

- **`Constants.java`** — constantes globales (puertos, separadores, rutas, contraseñas, aliases)
- **`HttpRequest.java`** — parsea una petición HTTP/1.1 leyendo byte a byte del `InputStream`. Extrae método, ruta, versión, cabeceras y ofrece `getCookieValue(name)`
- **`HttpResponse.java`** — construye y serializa una respuesta HTTP/1.1. Métodos de fábrica `ok()` y `error()`. Soporta `setCookie(name, value, path, secure)` y escribe al `OutputStream`
- **`CookieManager.java`** — lógica del historial: `parse()`, `updateHistory()`, `format()`, `buildHtml()`

### Paquete `server` (Fase 2)

- **`Server.java`** — `main`. Lanza dos hilos no-daemon: `HttpListener` y `HttpsListener`
- **`HttpListener.java`** — bucle `accept()` en puerto 8080. Por cada conexión lanza `new Thread(new ConnectionHandler(socket, false))`
- **`HttpsListener.java`** — igual pero con `SSLServerSocket` en puerto 4430 y `ConnectionHandler(socket, true)`
- **`ConnectionHandler.java`** — orquesta el procesamiento: parsea con `HttpRequest`, lee/actualiza cookie con `CookieManager`, construye `HttpResponse` y envía. Rechaza no-GET con 405
- **`SslContextFactory.java`** — crea el `SSLServerSocket` con autenticación mutua (TLSv1.2, `setNeedClientAuth(true)`). Carga `servidor.ks` y `ca.ks`. Los keystores están disponibles desde la Fase 4 (pendiente activar en Fase 5)

### Paquete `client` (Fase 3)

- **`Client.java`** — `main` del cliente de consola. Pide URL al usuario, abre conexión HTTP o HTTPS, envía GET, recibe respuesta y la vuelca (cabeceras + HTML). Usa `CookieStore` para persistencia
- **`CookieStore.java`** — lee/escribe la cookie en `cookies.dat`. Permite mantener el historial entre ejecuciones del cliente

---

## Estado de las fases

| Fase | Contenido | Estado |
|------|-----------|--------|
| 1 | Clases comunes: `HttpRequest`, `HttpResponse`, `CookieManager` | ✅ Completada |
| 2 | Servidor: `Server`, `HttpListener`, `HttpsListener`, `ConnectionHandler`, `SslContextFactory` | ✅ Completada |
| 3 | Cliente Java de consola: `Client`, `CookieStore` | ✅ Completada |
| 4 | Certificados X.509: CA raíz, cert servidor (CN=localhost), cert cliente (CN=nombre alumno) + keystores JKS | ✅ Completada |
| 5 | Activar HTTPS: completar `SslContextFactory` con keystores + probar con navegador | ⏳ Pendiente |
| 6 | Pulir y entregar: vídeo (3 escenarios), memoria técnica PDF, zip de entrega | ⏳ Pendiente |

---

## Requisitos opcionales ya implementados

- [x] Conteo de veces por recurso (formato `recurso:N` en la cookie)
- [x] Persistencia de cookies en disco (`CookieStore` — Fase 3)

## Requisitos opcionales pendientes

- [ ] Soporte POST (además de GET)

---

## Entrega

Zip con:
- `/doc` → Memoria técnica PDF (máx. 5 páginas) + vídeo (máx. 4 min)
- `/src` → Código fuente
- `/cert` → Certificados X.509, claves privadas, CA, keystores

**Escenarios obligatorios en el vídeo:**
1. HTTP: Cliente Java ↔ Servidor Java (≥3 peticiones, mostrar cabeceras + HTML)
2. HTTP: Navegador ↔ Servidor Java
3. HTTPS: Navegador ↔ Servidor Java + mostrar certificados en el navegador

---

## Notas de colaboración

- El alumno es **José María López Serrano** (CN del certificado cliente = su nombre)
- Los commits se hacen **solo a nombre del alumno**, sin línea Co-Authored-By
- Antes de hacer un commit, configurar git si es necesario:
  ```
  git config user.name "Jose Maria Lopez Serrano"
  git config user.email "josemlopezserrano@correo.ugr.es"
  ```
- Repositorio: `https://github.com/josemlopezserrano/PPC_practicas.git` (rama `master`)
- `.gitignore` excluye: `out/`, `*.class`, `conversaciones/`, `Pr_cticas/`
