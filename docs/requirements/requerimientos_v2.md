# Requerimientos

## Modificaciones desde la version 1

- Cambiar el nombre del requerimiento 11 para que sea mas claro
- Cambiar REQ-07 y REQ-09 ("puede" -> "debe")

## Requisitos funcionales

| ID      | Descripción                                                                                                                                                                                                                                                             |
|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| RF-01   | El sistema debe permitir al personal de recepción capturar el registro inicial de los pacientes que se presentan en las instalaciones del servicio de urgencias, gestionando sus datos demográficos y la apertura formal de la solicitud de atención.                   |
| RF-02   | El sistema debe permitir al personal de enfermería registrar los signos vitales y síntomas del paciente para asignarle una prioridad clínica.                                                                                                                           |
| RF-03   | El sistema debe clasificar a un paciente registrado en categorías I a V con criterios configurables, que dependen de la gravidad de sus heridas.                                                                                                                        |
| RF-04   | Usando el resultado del triage, el sistema debe asignar un numero de turno para el paciente                                                                                                                                                                             |
| RF-05   | Cuando una actualizacion de turno succede para un paciente, el sistema debe calcular el nuevo tiempo de espera que debe ser tan preciso que la primera estimacion. El sistema debe indicar claramente al paciente que una persona necesitaba una atencion prioritaria   |
| RF-06   | Mostrar al paciente o acompañante la categoria, el area de tratamiento y el tiempo estimado de espera.                                                                                                                                                                  |
| RF-07   | El sistema debe cancelar un turno si un paciente ahora no lo necesita. De facto, el sistema actualiza los turnos y la estimacion de espera para los otros pacientes                                                                                                    |
| RF-08   | El sistema debe quedar una traza del estado de un paciente durante el ciclo de atencion: ingreso, triage, esperando, consulta y egreso. Ademas, el sistema actualiza este estado cuando el paciente cambia de situacion                                                 |
| RF-09   | El sistema debe registrar pacientes que no tienen documentos de identificacion al momento del ingreso.                                                                                                                                                                 |
| RF-10   | El sistema permite configurar el tiempo de espera maximo para cada nivel de triage (Excluyendo el nivel 1 donde debe ser una atencion inmediata)                                                                                                                        |
| RF-11   | El sistema debe quitar el paciente mas priorizado de la cola para darle atencion medica                                                                                                                                          |

## Requisitos no funcionales

| ID | Descripción |
|---|---|
| | |
