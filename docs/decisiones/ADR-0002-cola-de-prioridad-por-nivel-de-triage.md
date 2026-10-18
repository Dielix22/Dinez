# ADR-0002: Cola de prioridad por nivel de Triage y límites de espera aislados

Autores:
- @BLlontopSec
- @elDIEGO12
- @Dielix22
- @hoverwars
- @sereypretel

Fecha: 2026-10-17

## Estado

Propuesto

## Contexto

El sistema de gestión de turnos debe ordenar a los pacientes según su nivel de Triage (REQ-04), permitir priorizar a un paciente y cambiar el turno de quienes estaban delante (REQ-06), permitir editar el tiempo límite de espera de cada nivel de Triage (REQ-12) y garantizar que ningún paciente espere más que el límite configurado para su nivel (REQ-13).

Estos requisitos obligan a decidir dos cosas del diseño descrito en [01_Architectural_and_Detailed_Design.md](../01_Architectural_and_Detailed_Design.md):

- **Dónde viven los límites de espera.** Si estuvieran dentro de la cola de prioridad, cambiar una configuración (REQ-12) implicaría tocar la lógica de ordenamiento (REQ-04, REQ-13).
- **Cómo se estructura la cola.** Debe permitir ordenar por nivel, reubicar pacientes cuando cambia su prioridad y detectar a quien excedió su límite.

## Decisión

Aislamos los límites de espera en un componente propio, *Gestionador de tiempo de espera*, que solo ofrece ver y cambiar el límite de cada nivel de Triage. *Gestionador de cola de prioridad* lo consulta, sin modificarlo.

Representamos la cola como una `ColaPorNivel` por cada nivel de Triage, administradas por `GestionadorColaPrioridad`. `siguientePaciente()` recorre los niveles en orden y atiende antes a un turno que haya excedido su límite de espera.

## Alternativas consideradas

- **Límites de espera dentro del gestionador de cola:** se descartó porque mezcla configuración (REQ-12) con ordenamiento (REQ-04, REQ-13), de modo que cualquier cambio en la configuración obliga a tocar la lógica de la cola.
- **Una única lista ordenada por un comparador:** se descartó porque reubicar a un paciente (REQ-06) obligaría a reordenar toda la lista, y detectar a quien excedió su límite (REQ-13) obligaría a recorrerla completa. Con una cola por nivel, el orden de llegada dentro de un nivel se cumple por construcción y basta revisar el primer turno de cada cola.

## Consecuencias

### Consecuencias positivas

- La cola solo necesita la capacidad "ver un tiempo límite de espera para cada nivel de Triage"; la configuración (REQ-12) puede evolucionar sin tocarla.
- Encolar al final de la cola del nivel respeta el orden de llegada dentro de ese nivel.
- Reubicar a un paciente por cambio de nivel (REQ-06) consiste en retirarlo de una cola y encolarlo en otra, sin reordenar el resto.
- Verificar REQ-13 solo requiere revisar el primer turno de cada una de las cuatro colas.

### Consecuencias negativas y compromisos

- Hay un componente adicional y una consulta entre componentes cada vez que la cola decide quién es el siguiente paciente. Con cuatro niveles el costo es bajo, pero hay que mantener la interfaz entre ambos.
- Se necesita lógica adicional para fusionar las colas al listar (`verListaOrdenada`) y para aplicar la excepción de REQ-13.
- Hay más objetos que con una sola lista.
- `GestionadorColaPrioridad` conoce la existencia de varios niveles, lo que lo acopla a `NivelTriage`.
