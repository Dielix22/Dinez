# Configuración Local del Backend

## Requisitos
- **Java**: Versión 25 instalada.
- **Base de datos**: PostgreSQL para ejecutar la aplicación (ver `database_migrations.md`).
- **Docker** (opcional): las pruebas que cargan la aplicación usan un contenedor PostgreSQL (Testcontainers, ADR-0007). Sin Docker esas pruebas se omiten; las pruebas unitarias del dominio se ejecutan igual.

## Variables de entorno (.env)
Debes crear un archivo `.env` directamente dentro de la carpeta **`src/backend/`** (al mismo nivel que el archivo `pom.xml`).
Allí se ubicarán las credenciales locales de PostgreSQL una vez configuremos la persistencia real.

## Comandos principales

Asegúrate de estar posicionado en la carpeta `src/backend/` antes de ejecutar los comandos en tu terminal.

### Ejecutar el proyecto en local
Levanta la aplicación en el puerto 8080:
```bash
# En Mac/Linux:
./mvnw spring-boot:run

# En Windows (CMD/PowerShell):
mvnw.cmd spring-boot:run