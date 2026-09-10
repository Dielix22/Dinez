# RFC-0001: Selección del stack tecnológico

Autores:
- @BLlontopSec
- @elDIEGO12
- @Dielix22
- @hoverwars

Fecha: 2026-09-09

Estado: Propuesto

## 1. TL;DR

Proponemos utilizar PostgreSQL como base de datos, Java con Spring Boot para el backend y React para el frontend de **Adres en Urgencias**. Esta combinación ofrece tecnologías maduras, documentación amplia y oportunidades de aprendizaje para el equipo, aunque requiere validar la estrategia de ejecución de la base de datos y definir desde el inicio la arquitectura de integración entre frontend y backend.

## 2. Motivación

El proyecto necesita un stack tecnológico que permita construir una aplicación mantenible para mejorar la gestión de la atención en los servicios de urgencias. La solución debe permitir coordinar diferentes actores, gestionar permisos, prioridades y estados, y aplicar reglas de negocio de forma verificable.

La selección debe considerar la experiencia actual del equipo, la disponibilidad de documentación, la facilidad para probar la lógica de negocio, la mantenibilidad y la posibilidad de entregar incrementos funcionales durante el semestre.

## 3. Propuesta de implementación

Se propone la siguiente combinación tecnológica:

- **Base de datos:** PostgreSQL.
- **Backend:** Java con Spring Boot.
- **Frontend:** React.

La aplicación se organizará como un monolito. El backend será responsable de la lógica de negocio, las validaciones, la persistencia y la exposición de las operaciones necesarias para la interfaz. El frontend ofrecerá las vistas y los flujos de interacción para los usuarios del sistema.

### 3.1 Base de datos

| Tecnología | Ventajas | Desventajas |
|---|---|---|
| **PostgreSQL** | Código abierto; sistema relacional maduro; buen soporte para integridad, transacciones y consultas complejas; amplia documentación y comunidad; permite representar de forma rigurosa las relaciones y reglas del dominio. | Requiere configurar y ejecutar un servidor si no se utiliza una alternativa embebida; puede exigir más administración que una base de datos ligera; el equipo debe aprender sus herramientas y configuración. |
| **MySQL** | Código abierto; ampliamente utilizado; documentación y comunidad extensas; instalación y administración conocidas por muchos desarrolladores; buen rendimiento para aplicaciones relacionales comunes. | Algunas decisiones históricas de compatibilidad y configuración pueden generar diferencias frente al estándar SQL; ciertas capacidades avanzadas pueden ser menos adecuadas para reglas complejas; también requiere un servidor separado en su configuración habitual. |
| **Oracle** | Plataforma madura; capacidades empresariales avanzadas; herramientas robustas de administración; buen soporte para alta disponibilidad y grandes volúmenes de datos. | Licenciamiento y operación más costosos; mayor complejidad para un proyecto académico; menor adecuación al tamaño actual del proyecto; puede aumentar la dependencia de herramientas específicas. |

**Elección:** PostgreSQL, por su madurez, sus capacidades relacionales y su equilibrio entre potencia, apertura y costo. Antes de la implementación deberá definirse cómo se resolverá la exigencia de iniciar la aplicación sin configurar un servidor de base de datos por separado.

### 3.2 Backend

| Tecnología | Ventajas | Desventajas |
|---|---|---|
| **C# + ASP.NET Core** | Framework moderno y de buen rendimiento; herramientas integradas; tipado estático; buena experiencia para construir APIs y aplicaciones web; soporte sólido de Microsoft. | El equipo tiene menor experiencia relativa con el ecosistema; puede requerir aprender C# y sus herramientas; introduce una plataforma distinta de la que la mayoría del equipo está aprendiendo en paralelo. |
| **Java + Spring Boot** | Framework bien documentado y probado; ecosistema amplio y maduro; tipado estático; buen soporte para pruebas, persistencia y aplicaciones web; Java es un lenguaje que la mayoría del equipo está aprendiendo en paralelo. | La cantidad de opciones del ecosistema puede aumentar la complejidad inicial; la configuración y la estructura del proyecto pueden resultar pesadas para quienes comienzan; requiere controlar cuidadosamente las dependencias y versiones. |
| **Python + FastAPI** | Sintaxis accesible; desarrollo rápido; buen soporte para APIs; tipado opcional mediante anotaciones; documentación automática de endpoints. | Python es una opción reciente para el equipo y cuenta con menos documentación conocida internamente; el tipado es menos estricto que en Java o C#; el equipo deberá establecer convenciones para mantener la estructura y calidad del código. |

**Elección:** Java con Spring Boot, debido a que es un framework bien documentado y probado, y porque la mayoría del equipo está aprendiendo Java en paralelo. Esta decisión también facilita aplicar una arquitectura clara y separar la lógica de negocio de la infraestructura.

### 3.3 Frontend

| Tecnología | Ventajas | Desventajas |
|---|---|---|
| **React** | Algunos miembros del equipo ya conocen el framework; ofrece muchas posibilidades de composición y extensión; cuenta con un ecosistema amplio; permite construir interfaces por componentes y evolucionar gradualmente la aplicación. | Exige tomar decisiones adicionales sobre estructura, estado, navegación y librerías antes de iniciar; su flexibilidad puede producir soluciones inconsistentes si no se definen convenciones. |
| **Vue.js** | Curva de aprendizaje más sencilla; configuración simple; permite comenzar rápidamente; ofrece una estructura progresiva y clara para interfaces por componentes. | Hay menos experiencia previa en el equipo que con React; algunas decisiones de ecosistema y escalabilidad pueden requerir investigación adicional; ofrece menos oportunidades de reutilizar el conocimiento existente del equipo. |
| **Angular** | Framework completo con convenciones integradas; incluye soluciones oficiales para varias necesidades comunes; facilita una estructura uniforme en aplicaciones grandes. | La curva de aprendizaje es demasiado larga para el tiempo disponible; su cantidad de conceptos y convenciones puede retrasar los primeros incrementos; requiere aprender un ecosistema amplio antes de obtener productividad. |

**Elección:** React, porque algunos miembros del equipo ya conocen el framework y ofrece mayores posibilidades de evolución. Se deberán definir desde el comienzo las convenciones de componentes, gestión de estado, navegación y consumo de datos para reducir la incertidumbre inicial.

## 4. Métricas

/

## 5. Riesgos e inconvenientes

- PostgreSQL normalmente requiere un servidor independiente, lo que puede entrar en conflicto con la exigencia de ejecutar la aplicación con un único comando y utilizar una base de datos embebida.
- El uso de tres tecnologías principales aumenta la necesidad de definir contratos claros entre frontend y backend.
- Spring Boot y React ofrecen muchas opciones, y una configuración sin convenciones puede producir inconsistencias entre módulos.
- La experiencia desigual del equipo puede concentrar el conocimiento en pocos integrantes.
- El tiempo de aprendizaje de Java, Spring Boot y React puede reducir el tiempo disponible para validar las reglas de negocio.
- La evolución de dependencias puede introducir incompatibilidades si no se fijan versiones y se automatizan las pruebas.

Para mitigar estos riesgos, se documentará el proceso de ejecución, se definirán convenciones de código, se crearán pruebas automatizadas desde el primer incremento y se revisará la compatibilidad de PostgreSQL con las restricciones de despliegue antes de cerrar la implementación.

## 6. Alternativas

Visto antes

## 7. Impacto potencial y dependencias

/

## 8. Preguntas sin resolver

/

## 9. Conclusión

Se propone adoptar PostgreSQL, Java con Spring Boot y React para desarrollar **Adres en Urgencias**. La selección prioriza tecnologías maduras, documentadas y con posibilidades de aprendizaje y crecimiento para el equipo.

La decisión queda condicionada a resolver la compatibilidad de PostgreSQL con el requisito de ejecución reproducible y base de datos embebida. Una vez aclarado ese punto, el equipo podrá fijar las versiones, documentar la configuración y comenzar la implementación de la primera funcionalidad vertical.
