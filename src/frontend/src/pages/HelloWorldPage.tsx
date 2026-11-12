import { Container, Typography } from "@mui/material";
import { MESSAGES } from "../i18n/messages";

export default function HelloWorldPage() {
  return (
    <Container maxWidth="sm" sx={{ mt: 8 }}>
      <Typography variant="h3" component="h1" align="center">
        {MESSAGES.helloWorld}
      </Typography>
    </Container>
  );
}
