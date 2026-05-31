import { defineConfig, devices } from "@playwright/test";

const apiBaseUrl = process.env.VITE_API_BASE_URL ?? "http://localhost:5000/api";

export default defineConfig({
  testDir: "./e2e",
  timeout: 30_000,
  expect: {
    timeout: 10_000
  },
  use: {
    baseURL: "http://localhost:5173",
    trace: "on-first-retry"
  },
  webServer: {
    command: `npm run dev -- --host localhost`,
    url: "http://localhost:5173",
    reuseExistingServer: true,
    env: {
      VITE_API_BASE_URL: apiBaseUrl
    }
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] }
    }
  ]
});
