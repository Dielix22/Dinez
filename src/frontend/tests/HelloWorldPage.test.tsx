import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import HelloWorldPage from "../src/pages/HelloWorldPage";
import { MESSAGES } from "../src/i18n/messages";

describe("HelloWorldPage", () => {
  it("shows the hello world message", () => {
    render(<HelloWorldPage />);

    expect(
      screen.getByRole("heading", { name: MESSAGES.helloWorld }),
    ).toBeInTheDocument();
  });
});
