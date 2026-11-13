// User-facing text is Spanish and centralised here (docs/dev/03_coding_conventions.md §2).
export const MESSAGES = {
  helloWorld: "¡Hola mundo!",
  home: {
    goToTickets: "Probar la asignación de turno",
  },
  tickets: {
    title: "Asignación de turno",
    subtitle: "Simula el resultado que entregaría Triage",
    formTitle: "Resultado de Triage",
    patientId: "N.º de registro del paciente",
    document: "Documento",
    firstName: "Nombres",
    lastName: "Apellidos",
    level: "Nivel de Triage",
    levels: {
      1: "Nivel 1 (atención inmediata)",
      2: "Nivel 2",
      3: "Nivel 3",
      4: "Nivel 4",
      5: "Nivel 5",
    },
    submit: "Asignar turno",
    submitting: "Asignando…",
    queueTitle: "Cola de espera",
    emptyQueue: "No hay pacientes en espera.",
    loading: "Cargando…",
    columns: {
      ticketNumber: "Turno",
      level: "Nivel",
      position: "Posición en su nivel",
      patientId: "N.º de registro",
      arrivalTime: "Llegada",
    },
    assigned: (ticketNumber: string, position: number) =>
      `Turno asignado: ${ticketNumber}. Posición ${position} en la cola de su nivel.`,
    immediateCare: (patientId: string) =>
      `Paciente ${patientId} en nivel 1: pasa a atención inmediata, sin turno.`,
  },
  // Keys are the error codes of the back end.
  errors: {
    DATOS_INVALIDOS:
      "Faltan datos o alguno no es válido. Revisa el formulario.",
    PACIENTE_DUPLICADO:
      "Este paciente ya tiene un turno en espera. No se asignó un segundo turno.",
    CONNECTION_ERROR: "No se pudo contactar al servidor. Intenta de nuevo.",
    UNEXPECTED_ERROR: "Ocurrió un error inesperado. Intenta de nuevo.",
  },
} as const;

/** Maps a stable back-end error code to the text shown to the user. */
export function errorMessage(code: string): string {
  const errors: Record<string, string> = MESSAGES.errors;
  return errors[code] ?? MESSAGES.errors.UNEXPECTED_ERROR;
}
