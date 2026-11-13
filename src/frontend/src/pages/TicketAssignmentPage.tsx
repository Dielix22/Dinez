import {
  Alert,
  Button,
  Container,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import { useEffect, useState } from "react";
import { assignTicket, listWaitingTickets, toApiError } from "../api/tickets";
import type { TicketAssignment, WaitingTicket } from "../api/tickets";
import { MESSAGES, errorMessage } from "../i18n/messages";

const TEXT = MESSAGES.tickets;
const LEVELS = Object.entries(TEXT.levels);
const DEFAULT_LEVEL = "3";
const LOCALE = "es-CO";

const EMPTY_FORM = {
  patientId: "",
  document: "",
  firstName: "",
  lastName: "",
  level: DEFAULT_LEVEL,
};

interface Feedback {
  severity: "success" | "warning" | "error";
  text: string;
}

function formatTime(instant: string): string {
  return new Date(instant).toLocaleTimeString(LOCALE, { hour12: false });
}

function toFeedback(assignment: TicketAssignment): Feedback {
  if (assignment.immediateCare || assignment.ticketNumber === null) {
    return {
      severity: "warning",
      text: TEXT.immediateCare(assignment.patientId),
    };
  }
  return {
    severity: "success",
    text: TEXT.assigned(assignment.ticketNumber, assignment.position ?? 0),
  };
}

/**
 * REQ-04: plays the role of Triage. It sends a patient and a triage level to
 * the ticket assigner and shows the assigned ticket and the waiting queue.
 */
export default function TicketAssignmentPage() {
  const [form, setForm] = useState(EMPTY_FORM);
  const [submitting, setSubmitting] = useState(false);
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const [queue, setQueue] = useState<WaitingTicket[]>([]);
  const [loading, setLoading] = useState(true);
  const [queueError, setQueueError] = useState<string | null>(null);

  async function loadQueue() {
    try {
      setQueue(await listWaitingTickets());
      setQueueError(null);
    } catch (error) {
      setQueueError(errorMessage(toApiError(error).code));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadQueue();
  }, []);

  function handleChange(event: { target: { name: string; value: string } }) {
    const { name, value } = event.target;
    setForm((previous) => ({ ...previous, [name]: value }));
  }

  async function handleSubmit(event: { preventDefault: () => void }) {
    event.preventDefault();
    setSubmitting(true);
    setFeedback(null);
    try {
      const assignment = await assignTicket({
        ...form,
        level: Number(form.level),
      });
      setFeedback(toFeedback(assignment));
      setForm({ ...EMPTY_FORM, level: form.level });
      await loadQueue();
    } catch (error) {
      setFeedback({
        severity: "error",
        text: errorMessage(toApiError(error).code),
      });
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Typography variant="h4" component="h1">
        {TEXT.title}
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        {TEXT.subtitle}
      </Typography>

      <Paper variant="outlined" sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" component="h2" sx={{ mb: 2 }}>
          {TEXT.formTitle}
        </Typography>
        <form onSubmit={handleSubmit} noValidate>
          <Stack spacing={2}>
            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <TextField
                fullWidth
                autoFocus
                name="patientId"
                label={TEXT.patientId}
                value={form.patientId}
                onChange={handleChange}
                autoComplete="off"
              />
              <TextField
                fullWidth
                name="document"
                label={TEXT.document}
                value={form.document}
                onChange={handleChange}
                autoComplete="off"
              />
            </Stack>
            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <TextField
                fullWidth
                name="firstName"
                label={TEXT.firstName}
                value={form.firstName}
                onChange={handleChange}
                autoComplete="off"
              />
              <TextField
                fullWidth
                name="lastName"
                label={TEXT.lastName}
                value={form.lastName}
                onChange={handleChange}
                autoComplete="off"
              />
            </Stack>
            <TextField
              select
              name="level"
              label={TEXT.level}
              value={form.level}
              onChange={handleChange}
              slotProps={{ select: { native: true } }}
            >
              {LEVELS.map(([level, name]) => (
                <option key={level} value={level}>
                  {name}
                </option>
              ))}
            </TextField>
            <Button
              type="submit"
              variant="contained"
              disabled={submitting}
              sx={{ alignSelf: "flex-start" }}
            >
              {submitting ? TEXT.submitting : TEXT.submit}
            </Button>
            {feedback && (
              <Alert
                severity={feedback.severity}
                role={feedback.severity === "error" ? "alert" : "status"}
              >
                {feedback.text}
              </Alert>
            )}
          </Stack>
        </form>
      </Paper>

      <Paper variant="outlined" sx={{ p: 3 }}>
        <Typography variant="h6" component="h2" sx={{ mb: 2 }}>
          {TEXT.queueTitle}
        </Typography>
        {queueError && <Alert severity="error">{queueError}</Alert>}
        {!queueError && loading && (
          <Typography color="text.secondary">{TEXT.loading}</Typography>
        )}
        {!queueError && !loading && queue.length === 0 && (
          <Typography color="text.secondary">{TEXT.emptyQueue}</Typography>
        )}
        {queue.length > 0 && (
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>{TEXT.columns.ticketNumber}</TableCell>
                  <TableCell>{TEXT.columns.level}</TableCell>
                  <TableCell>{TEXT.columns.position}</TableCell>
                  <TableCell>{TEXT.columns.patientId}</TableCell>
                  <TableCell>{TEXT.columns.arrivalTime}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {queue.map((ticket) => (
                  <TableRow key={ticket.ticketNumber}>
                    <TableCell>{ticket.ticketNumber}</TableCell>
                    <TableCell>{ticket.level}</TableCell>
                    <TableCell>{ticket.position}</TableCell>
                    <TableCell>{ticket.patientId}</TableCell>
                    <TableCell>{formatTime(ticket.arrivalTime)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Paper>
    </Container>
  );
}
