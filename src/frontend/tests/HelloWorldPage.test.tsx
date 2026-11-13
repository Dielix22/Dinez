import { cleanup, render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it } from "vitest";
import HelloWorldPage from "../src/pages/HelloWorldPage";
import { MESSAGES } from "../src/i18n/messages";

function renderPage() {
  render(
    <MemoryRouter>
      <HelloWorldPage />
    </MemoryRouter>,
  );
}

describe("HelloWorldPage", () => {
  afterEach(cleanup);

  it("shows the hello world message", () => {
    renderPage();

    expect(
      screen.getByRole("heading", { name: MESSAGES.helloWorld }),
    ).toBeInTheDocument();
  });

  it("links to the tickets page", () => {
    renderPage();

    expect(
      screen.getByRole("link", { name: MESSAGES.home.goToTickets }),
    ).toHaveAttribute("href", "/tickets");
  });
});
