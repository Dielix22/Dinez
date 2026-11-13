import { Button, Container, Stack, Typography } from "@mui/material";
import { Link } from "react-router-dom";
import { MESSAGES } from "../i18n/messages";

export default function HelloWorldPage() {
  return (
    <Container maxWidth="sm" sx={{ mt: 8 }}>
      <Stack spacing={4} sx={{ alignItems: "center" }}>
        <Typography variant="h3" component="h1" align="center">
          {MESSAGES.helloWorld}
        </Typography>
        <Button variant="contained" component={Link} to="/tickets">
          {MESSAGES.home.goToTickets}
        </Button>
      </Stack>
    </Container>
  );
}
