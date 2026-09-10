# ADR-0001: Selección del stack tecnológico

Autores:
- @BLlontopSec
- @elDIEGO12
- @Dielix22
- @hoverwars

Fecha: 2026-09-09

## Estado

Propuesto

## Contexto

El proyecto **Adres en Urgencias** necesita un stack tecnológico que permita construir una aplicación mantenible para gestionar la atención en los servicios de urgencias. La selección debe considerar la madurez de las tecnologías, la documentación disponible, la experiencia y el aprendizaje del equipo, así como las restricciones de ejecución y de pruebas del proyecto.

La evaluación de las alternativas y el razonamiento detallado se encuentran en la [RFC-0001: Selección del stack tecnológico](RFC-0001-stack-tecnologico.md).

## Decisión

Adoptaremos PostgreSQL como base de datos, Java con Spring Boot como tecnología de backend y React como tecnología de frontend para **Adres en Urgencias**.

La aplicación se organizará como un monolito. Java con Spring Boot implementará la lógica de negocio, las validaciones, la persistencia y las operaciones necesarias para la interfaz. React implementará las vistas y los flujos de interacción de los usuarios.

## Alternativas consideradas

- **Base de datos:** se evaluaron MySQL y Oracle. MySQL es una alternativa madura y ampliamente utilizada, pero PostgreSQL ofrece un equilibrio más conveniente entre capacidades relacionales, apertura y costo. Oracle se descartó por su costo y complejidad para el contexto académico.
- **Backend:** se evaluaron C# con ASP.NET Core y Python con FastAPI. Java con Spring Boot se eligió por ser un framework bien documentado y probado, además de coincidir con el aprendizaje actual de la mayoría del equipo. Python se consideró una opción reciente y con menos documentación conocida internamente.
- **Frontend:** se evaluaron Vue.js y Angular. Vue.js ofrece una configuración más simple y una curva de aprendizaje menor, mientras que Angular implica un aprendizaje demasiado largo para el plazo disponible. React se eligió porque algunos miembros del equipo ya conocen el framework y porque ofrece más posibilidades de evolución.

## Consecuencias

### Consecuencias positivas

- El equipo trabajará con tecnologías maduras y ampliamente documentadas.
- Spring Boot proporciona una base sólida para separar la lógica de negocio de la infraestructura y probarla de forma aislada.
- React permite reutilizar el conocimiento de algunos integrantes y construir la interfaz mediante componentes.
- PostgreSQL ofrece transacciones, integridad referencial y consultas adecuadas para las reglas del dominio.
- El stack elegido permite documentar una arquitectura coherente y evolucionar el producto durante el semestre.

### Consecuencias negativas y compromisos

- La flexibilidad de React obliga a definir convenciones para componentes, navegación, estado y consumo de datos antes de avanzar demasiado.
- Spring Boot y su ecosistema pueden aumentar la complejidad inicial para quienes están aprendiendo Java.
- El equipo deberá controlar las versiones y dependencias de tres ecosistemas tecnológicos.
- Será necesario documentar y automatizar la configuración local para que todos los integrantes puedan ejecutar la aplicación de forma reproducible.

