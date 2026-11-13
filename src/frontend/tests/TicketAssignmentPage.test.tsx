import {
  cleanup,
  fireEvent,
  render,
  screen,
  within,
} from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { TicketAssignment, WaitingTicket } from "../src/api/tickets";
import { MESSAGES } from "../src/i18n/messages";
import TicketAssignmentPage from "../src/pages/TicketAssignmentPage";

const TEXT = MESSAGES.tickets;

const WAITING_TICKET: WaitingTicket = {
  ticketDate: "2026-11-12",
  ticketNumber: "T3-014",
  patientId: "P-0999",
  level: 3,
  position: 1,
  arrivalTime: "2026-11-12T14:30:00Z",
};

const NEW_TICKET: WaitingTicket = {
  ticketDate: "2026-11-12",
  ticketNumber: "T3-015",
  patientId: "P-1001",
  level: 3,
  position: 2,
  arrivalTime: "2026-11-12T15:00:00Z",
};

const ASSIGNED: TicketAssignment = { ...NEW_TICKET, immediateCare: false };

const IMMEDIATE_CARE: TicketAssignment = {
  patientId: "P-1001",
  level: 1,
  immediateCare: true,
  ticketNumber: null,
  arrivalTime: null,
  position: null,
};

function jsonResponse(status: number, body: unknown): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: () => Promise.resolve(body),
  } as Response;
}

/**
 * Fake back end. POST answers with `assignment`; GET lists `before` until the
 * first POST and `after` from then on.
 */
function stubBackEnd(
  assignment: Response,
  before: WaitingTicket[],
  after: WaitingTicket[] = before,
) {
  let posted = false;
  const fetchMock = vi.fn((_url: string, init?: RequestInit) => {
    if (init?.method === "POST") {
      posted = true;
      return Promise.resolve(assignment);
    }
    return Promise.resolve(jsonResponse(200, posted ? after : before));
  });
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

function fillAndSubmitForm(level: string) {
  fireEvent.change(screen.getByLabelText(TEXT.patientId), {
    target: { value: "P-1001" },
  });
  fireEvent.change(screen.getByLabelText(TEXT.document), {
    target: { value: "79456123" },
  });
  fireEvent.change(screen.getByLabelText(TEXT.firstName), {
    target: { value: "Jorge" },
  });
  fireEvent.change(screen.getByLabelText(TEXT.lastName), {
    target: { value: "Castaño" },
  });
  fireEvent.change(screen.getByLabelText(TEXT.level), {
    target: { value: level },
  });
  fireEvent.click(screen.getByRole("button", { name: TEXT.submit }));
}

describe("TicketAssignmentPage", () => {
  beforeEach(() => {
    vi.unstubAllGlobals();
  });

  afterEach(() => {
    cleanup();
  });

  it("lists the waiting queue", async () => {
    stubBackEnd(jsonResponse(201, ASSIGNED), [WAITING_TICKET]);

    render(<TicketAssignmentPage />);

    expect(await screen.findByText(WAITING_TICKET.ticketNumber)).toBeVisible();
  });

  it("says so when nobody is waiting", async () => {
    stubBackEnd(jsonResponse(201, ASSIGNED), []);

    render(<TicketAssignmentPage />);

    expect(await screen.findByText(TEXT.emptyQueue)).toBeVisible();
  });

  it("assigns a ticket, shows its number and refreshes the queue", async () => {
    const fetchMock = stubBackEnd(
      jsonResponse(201, ASSIGNED),
      [WAITING_TICKET],
      [WAITING_TICKET, NEW_TICKET],
    );
    render(<TicketAssignmentPage />);
    await screen.findByText(WAITING_TICKET.ticketNumber);

    fillAndSubmitForm("3");

    expect(await screen.findByRole("status")).toHaveTextContent("T3-015");
    expect(
      await within(screen.getByRole("table")).findByText("T3-015"),
    ).toBeVisible();
    const [, init] = fetchMock.mock.calls[1];
    expect(JSON.parse(String(init?.body))).toEqual({
      patientId: "P-1001",
      document: "79456123",
      firstName: "Jorge",
      lastName: "Castaño",
      level: 3,
    });
  });

  it("explains that a level 1 patient goes to immediate care", async () => {
    stubBackEnd(jsonResponse(200, IMMEDIATE_CARE), []);
    render(<TicketAssignmentPage />);
    await screen.findByText(TEXT.emptyQueue);

    fillAndSubmitForm("1");

    expect(await screen.findByRole("status")).toHaveTextContent(
      TEXT.immediateCare("P-1001"),
    );
    expect(screen.queryByRole("table")).not.toBeInTheDocument();
  });

  it("explains that the patient already has a waiting ticket", async () => {
    const duplicate = jsonResponse(409, { code: "PACIENTE_DUPLICADO" });
    stubBackEnd(duplicate, [WAITING_TICKET]);
    render(<TicketAssignmentPage />);
    await screen.findByText(WAITING_TICKET.ticketNumber);

    fillAndSubmitForm("3");

    expect(await screen.findByRole("alert")).toHaveTextContent(
      MESSAGES.errors.PACIENTE_DUPLICADO,
    );
  });
});
