# Guía de Configuración y Ejecución del Backend — Dinez

Este documento describe los pasos necesarios para compilar, probar y ejecutar localmente el servicio backend de **Dinez** (Java 25 + Spring Boot 4.1.2).

---

## Requisitos Previos

1. **Java Development Kit (JDK) 25 (LTS):**
   * El proyecto está configurado para compilar exclusivamente con **Java 25**.
   * Verifica tu versión activa ejecutando `java -version` y `javac -version`.
   * **Importante:** Si el equipo tiene versiones anteriores instaladas (como Java 17 o 21), asegúrate de que la variable de entorno `JAVA_HOME` apunte a la carpeta de instalación del **JDK 25**.
2. **Maven Wrapper (incluido):**
   * No es necesario instalar Maven de forma global; el repositorio incluye `./mvnw` (Linux/macOS) y `.\mvnw.cmd` (Windows).
3. **PostgreSQL (solo para levantar el servidor):**
   * Necesario únicamente si vas a ejecutar la aplicación con `spring-boot:run`, ya que **Flyway** valida la conexión y ejecuta migraciones al iniciar.
   * *Nota:* Para correr los tests automatizados **no** necesitas tener PostgreSQL instalado ni encendido.

---

## 1. Ejecutar Pruebas Automatizadas (Sin PostgreSQL)

La suite de pruebas utiliza una base de datos **H2 en memoria**, por lo que cualquier integrante del equipo puede validar que el proyecto compila sin configurar credenciales externas.

1. Ubícate en la carpeta del backend:
   ```bash
   cd src/backend
   ```
2. Ejecuta los tests:
   * **Windows (PowerShell / CMD):**
     ```powershell
     .\mvnw.cmd clean test
     ```
   * **Linux / macOS:**
     ```bash
     ./mvnw clean test
     ```
3. Verifica que el resultado final indique `BUILD SUCCESS`.

---

## 2. Levantar el Servidor Local (Requiere PostgreSQL)

1. **Enciende PostgreSQL:** Asegúrate de que el servicio de PostgreSQL esté activo en `localhost:5432`.
2. **Verifica la configuración:** Revisa que la base de datos, usuario y contraseña coincidan con los definidos en `src/backend/src/main/resources/application.properties`.
3. **Inicia el servidor** desde `src/backend`:
   * **Windows:**
     ```powershell
     .\mvnw.cmd spring-boot:run
     ```
   * **Linux / macOS:**
     ```bash
     ./mvnw spring-boot:run
     ```
4. **Comprueba el funcionamiento:** Abre en tu navegador o cliente HTTP la ruta de salud:
   ```text
   GET http://localhost:8080/api/helloworld
   ```

---

## Solución de Problemas Frecuentes

* **`Fatal error compiling: error: release version 25 not supported`**
  * **Causa:** La terminal está usando una versión antigua de Java (ej. Java 21) en lugar de Java 25.
  * **Solución (PowerShell):** Apunta temporalmente la sesión hacia tu carpeta de JDK 25:
    ```powershell
    $env:JAVA_HOME = "C:\Ruta\A\Tu\jdk-25"
    $env:Path = "$env:JAVA_HOME\bin;$env:Path"
    ```
* **`Connection to localhost:5432 refused`**
  * **Causa:** El servidor intenta conectarse a PostgreSQL al arrancar, pero el servicio está detenido.
  * **Solución:** Inicia el servicio de PostgreSQL desde los servicios del sistema (`services.msc` en Windows) antes de correr `spring-boot:run`.
* **Errores en rojo en VS Code (`package ... does not exist`) aunque `mvnw test` funciona**
  * **Causa:** Caché desactualizada de la extensión de Java en VS Code.
  * **Solución:** Presiona `Ctrl + Shift + P`, busca **`Java: Clean Java Language Server Workspace`** y selecciona *Restart and delete*.