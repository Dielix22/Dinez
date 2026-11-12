import { CssBaseline } from "@mui/material";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import HelloWorldPage from "./pages/HelloWorldPage";

export default function App() {
  return (
    <BrowserRouter>
      <CssBaseline />
      <Routes>
        <Route path="/" element={<HelloWorldPage />} />
      </Routes>
    </BrowserRouter>
  );
}
