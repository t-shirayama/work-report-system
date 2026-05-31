import { expect, test } from "@playwright/test";

test.describe("work report E2E", () => {
  test("login, open dashboard, register a report, and find it", async ({ page }) => {
    await login(page, "sato", "password");

    await expect(page.getByRole("heading", { name: "ダッシュボード" })).toBeVisible();
    await expect(page.locator(".account").getByText("佐藤 花子")).toBeVisible();

    await page.getByRole("button", { name: "日報登録" }).click();
    await page.getByLabel("作業日").fill("2026-05-31");
    await page.getByLabel("プロジェクト名").fill("E2E登録");
    await page.getByLabel("作業分類").selectOption("DEVELOPMENT");
    await page.getByLabel("作業時間").fill("1.5");
    await page.getByLabel("作業内容").fill("フロントからAPIとDBまで通す");
    await page.getByRole("button", { name: "登録", exact: true }).click();
    await expect(page.getByText(/登録しました/)).toBeVisible();

    await page.getByRole("button", { name: "実績検索" }).click();
    await page.getByPlaceholder("プロジェクト").fill("E2E登録");
    await page.getByRole("button", { name: "検索", exact: true }).click();
    await expect(page.getByRole("cell", { name: "E2E登録" })).toBeVisible();
    await expect(page.getByRole("cell", { name: "佐藤 花子" })).toBeVisible();
  });

  test("admin can export report and open report history", async ({ page }) => {
    await login(page, "admin", "password");

    await page.getByRole("button", { name: "帳票出力" }).click();
    await page.getByLabel("対象年月").fill("2026-05");
    await page.getByLabel("対象者").selectOption({ label: "佐藤 花子" });

    const download = page.waitForEvent("download");
    await page.getByRole("button", { name: "出力", exact: true }).click();
    const file = await download;
    expect(file.suggestedFilename()).toContain("monthly-report-202605-sato.xlsx");

    await page.getByRole("button", { name: "履歴" }).click();
    await page.getByPlaceholder("YYYYMM").fill("202605");
    await page.getByRole("button", { name: "検索", exact: true }).click();
    await expect(page.getByRole("cell", { name: "monthly-report-202605-sato.xlsx" })).toBeVisible();
  });

  test("admin can maintain departments and users", async ({ page }) => {
    await login(page, "admin", "password");

    await page.getByRole("button", { name: "マスタ管理" }).click();
    await expect(page.getByRole("heading", { name: "マスタ管理" })).toBeVisible();

    await page.getByLabel("部署コード").fill("E2E");
    await page.getByLabel("部署名").fill("E2E部");
    await page.getByLabel("表示順").fill("20");
    await page.getByRole("button", { name: "追加", exact: true }).first().click();
    await expect(page.getByText("部署を追加しました。")).toBeVisible();
    await expect(page.getByRole("cell", { name: "E2E部" })).toBeVisible();

    await page.locator('select[name="departmentId"]').selectOption({ label: "E2E部" });
    await page.getByLabel("ログインID").fill("e2e-user");
    await page.getByLabel("社員名").fill("E2E ユーザー");
    await page.getByLabel("権限").selectOption("USER");
    await page.getByLabel("パスワード").fill("password");
    await page.getByRole("button", { name: "追加", exact: true }).last().click();
    await expect(page.getByText("ユーザーを追加しました。")).toBeVisible();
    await expect(page.getByRole("cell", { name: "e2e-user" })).toBeVisible();
    await expect(page.getByRole("cell", { name: "E2E ユーザー" })).toBeVisible();
  });

  test("login error and API validation error are shown", async ({ page }) => {
    await page.goto("/");
    await page.getByLabel("ログインID").fill("admin");
    await page.getByLabel("パスワード").fill("wrong-password");
    await page.getByRole("button", { name: "ログイン" }).click();
    await expect(page.getByText("ログインIDまたはパスワードが正しくありません。")).toBeVisible();

    await login(page, "sato", "password");
    await page.getByRole("button", { name: "日報登録" }).click();
    await page.getByLabel("作業日").fill("2026-05-31");
    await page.getByLabel("プロジェクト名").fill("E2E不正");
    await page.getByLabel("作業分類").selectOption("DEVELOPMENT");
    await page.locator('input[name="workHours"]').evaluate((element) => element.removeAttribute("max"));
    await page.getByLabel("作業時間").fill("25");
    await page.getByLabel("作業内容").fill("不正な作業時間");
    await page.getByRole("button", { name: "登録", exact: true }).click();
    await expect(page.getByText("作業時間は24時間以内で入力してください。")).toBeVisible();
  });
});

async function login(page: import("@playwright/test").Page, loginId: string, password: string) {
  await page.goto("/");
  await page.getByLabel("ログインID").fill(loginId);
  await page.getByLabel("パスワード").fill(password);
  await page.getByRole("button", { name: "ログイン" }).click();
}
