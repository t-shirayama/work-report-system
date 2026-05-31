# テスト方針

## C#

テストプロジェクトは以下です。

| プロジェクト | 種別 |
|---|---|
| `backend/WorkReport.Tests` | UT / API境界の軽量テスト |
| `backend/WorkReport.IntegrationTests` | SQL Serverコンテナを使うAPI結合テスト |

```powershell
dotnet test WorkReport.slnx
```

現在の主なテスト観点:

| テスト | 観点 |
|---|---|
| `WorkReportValidationTests` | 日報登録、検索条件の入力チェック |
| `CurrentUserTests` | ClaimsPrincipalからログインユーザー情報を復元できること |
| `MonthlyReportWorkbookTests` | ClosedXMLで帳票ヘッダー、明細、分類集計が生成されること |
| `AuthEndpointTests` | CSRF endpoint、未認証時の保護API拒否 |

### API結合テスト

```powershell
dotnet test backend/WorkReport.IntegrationTests/WorkReport.IntegrationTests.csproj
```

`WorkReport.IntegrationTests` は Testcontainers で SQL Server 2022 を起動し、`database/sqlserver/schema.sql` と `seed.sql` を適用してから API を検証します。Dockerが起動していない場合は25件すべてスキップします。

詳細ケースは [ITテストケース一覧](integration-test-cases.md) にまとめています。

| テスト | ケース |
|---|---|
| `DatabaseInitializationTests` | DDL/seed適用、DB制約による不正データ拒否 |
| `AuthIntegrationTests` | ログイン成功、ログイン失敗、未認証拒否、CSRFなしPOST拒否、CORS |
| `AuthorizationIntegrationTests` | ADMIN/USERの対象ユーザー一覧権限 |
| `WorkReportsIntegrationTests` | 日報登録DB反映、登録入力エラー、検索全フィルタ、USERスコープ、日付範囲エラー |
| `DashboardIntegrationTests` | 集計値取得、最近の活動の降順 |
| `MonthlyReportsIntegrationTests` | USER自分の帳票出力、ADMIN任意ユーザー出力、他人出力拒否、不正年月、存在しないユーザー、明細0件、再DL404 |
| `ReportHistoriesIntegrationTests` | 履歴検索条件、履歴詳細、存在しない履歴404 |

## フロントエンド

```powershell
cd frontend
npm run test
npm run build
```

テストは Vitest + Testing Library で実行します。

現在の主なテスト観点:

| テスト | 観点 |
|---|---|
| `App.test.tsx` | 未認証時にログイン画面を表示すること |
| `App.test.tsx` | ログイン成功後にダッシュボードへ遷移し、集計値を表示すること |

### E2E

```powershell
cd frontend
npm run test:e2e
```

E2Eは Playwright で実行します。APIが `http://localhost:5000/api` で起動済みで、DBにseedデータがある前提です。

詳細ケースは [ITテストケース一覧](integration-test-cases.md) にまとめています。

| テスト | ケース |
|---|---|
| `work-report-flow.spec.ts` | 一般ユーザーのログイン、ダッシュボード表示、日報登録、検索反映 |
| `work-report-flow.spec.ts` | 管理者の帳票出力、ダウンロード、履歴表示 |
| `work-report-flow.spec.ts` | ログイン失敗表示、API入力エラー表示 |

## DB確認

DB関連変更では以下を確認します。

- `schema.sql` が空DBに適用できること。
- `seed.sql` がDDL適用後に実行できること。
- APIの検索、登録、帳票履歴登録でSQL Server固有関数が期待どおり動くこと。
