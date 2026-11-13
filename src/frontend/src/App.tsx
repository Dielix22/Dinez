import { CssBaseline } from "@mui/material";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import HelloWorldPage from "./pages/HelloWorldPage";
import TicketAssignmentPage from "./pages/TicketAssignmentPage";

export default function App() {
  return (
    <BrowserRouter>
      <CssBaseline />
      <Routes>
        <Route path="/" element={<HelloWorldPage />} />
        <Route path="/tickets" element={<TicketAssignmentPage />} />
      </Routes>
    </BrowserRouter>
  );
}
