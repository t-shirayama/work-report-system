import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import App from "./App";

const user = {
  userId: 1,
  departmentId: 1,
  departmentName: "開発部",
  loginId: "admin",
  employeeName: "管理 太郎",
  roleCode: "ADMIN"
};

const dashboard = {
  todayWorkReportCount: 2,
  currentMonthTotalHours: 12.5,
  notOutputMonthlyReportCount: 1,
  recentActivities: []
};

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("App", () => {
  it("shows login screen when current user is not authenticated", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => new Response("", { status: 401 })));

    render(<App />);

    expect(await screen.findByRole("heading", { name: "Work Report" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "ログイン" })).toBeInTheDocument();
  });

  it("logs in and opens the dashboard", async () => {
    vi.stubGlobal("fetch", vi.fn(async (input: RequestInfo | URL) => {
      const url = input instanceof Request ? input.url : String(input);
      if (url.endsWith("/auth/me")) {
        return new Response("", { status: 401 });
      }
      if (url.endsWith("/auth/csrf")) {
        return json({ token: "csrf-token" });
      }
      if (url.endsWith("/auth/login")) {
        return json(user);
      }
      if (url.endsWith("/dashboard")) {
        return json(dashboard);
      }
      return new Response("", { status: 404 });
    }));

    render(<App />);

    fireEvent.click(await screen.findByRole("button", { name: "ログイン" }));

    await waitFor(() => expect(screen.getByText("管理 太郎")).toBeInTheDocument());
    expect(screen.getByRole("heading", { name: "ダッシュボード" })).toBeInTheDocument();
    expect(await screen.findByText("12.5h")).toBeInTheDocument();
  });
});

function json(value: unknown) {
  return new Response(JSON.stringify(value), {
    status: 200,
    headers: { "Content-Type": "application/json" }
  });
}
