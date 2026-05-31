# テスト方針

## C#

テストプロジェクトは `backend/WorkReport.Tests` です。

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

## DB確認

DB関連変更では以下を確認します。

- `schema.sql` が空DBに適用できること。
- `seed.sql` がDDL適用後に実行できること。
- APIの検索、登録、帳票履歴登録でSQL Server固有関数が期待どおり動くこと。
