// Client of the ticket API (back end: TicketController).

/** Body of POST /api/tickets: what Registration and Triage would send. */
export interface AssignTicketRequest {
  patientId: string;
  document: string;
  firstName: string;
  lastName: string;
  /** Triage level, 1 to 5. */
  level: number;
}

/** Answer of POST /api/tickets. Level 1 gets no ticket: immediateCare is true. */
export interface TicketAssignment {
  patientId: string;
  level: number;
  immediateCare: boolean;
  ticketNumber: string | null;
  /** ISO-8601 instant set by the server clock. */
  arrivalTime: string | null;
  position: number | null;
}

/** A waiting ticket, as listed by GET /api/tickets. */
export interface WaitingTicket {
  /** Day of the ticket (YYYY-MM-DD). The number restarts every day, so number + date identify it. */
  ticketDate: string;
  ticketNumber: string;
  patientId: string;
  level: number;
  position: number;
  arrivalTime: string;
}

export const CONNECTION_ERROR = "CONNECTION_ERROR";
export const UNEXPECTED_ERROR = "UNEXPECTED_ERROR";

const TICKETS_URL = "/api/tickets";

/** Failure with the stable error code returned by the back end. */
export class ApiError extends Error {
  readonly code: string;

  constructor(code: string) {
    super(code);
    this.code = code;
  }
}

export function toApiError(error: unknown): ApiError {
  return error instanceof ApiError ? error : new ApiError(UNEXPECTED_ERROR);
}

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  let response: Response;
  try {
    response = await fetch(url, init);
  } catch {
    throw new ApiError(CONNECTION_ERROR);
  }
  const body: unknown = await response.json().catch(() => null);
  if (!response.ok) {
    const failure = body as { code?: string } | null;
    throw new ApiError(failure?.code ?? UNEXPECTED_ERROR);
  }
  return body as T;
}

export function assignTicket(
  triageResult: AssignTicketRequest,
): Promise<TicketAssignment> {
  return request<TicketAssignment>(TICKETS_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(triageResult),
  });
}

export function listWaitingTickets(): Promise<WaitingTicket[]> {
  return request<WaitingTicket[]>(TICKETS_URL);
}
