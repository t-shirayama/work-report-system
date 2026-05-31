# 運用メモ

## 設定

API設定は [appsettings.json](../backend/WorkReport.Api/appsettings.json) を基準にします。

| 設定 | 説明 |
|---|---|
| `ConnectionStrings:WorkReport` | SQL Server接続文字列 |
| `Cors:AllowedOrigins` | React SPAの許可オリジン |
| `ReportStorage:BasePath` | 帳票保存先 |

本番では接続文字列、パスワード、Cookie関連設定を環境変数またはSecret Managerへ外出しします。

## 起動

API:

```powershell
dotnet run --project backend/WorkReport.Api/WorkReport.Api.csproj --urls http://localhost:5000
```

Frontend:

```powershell
cd frontend
npm run dev
```

## 帳票保存

月次帳票は `ReportStorage:BasePath` 配下に保存します。既定値は `generated-reports` です。

運用時に決めること:

- 保存先ディスクまたは外部ストレージ
- 保管期間
- バックアップ対象
- 再ダウンロード権限
- ファイル削除時の履歴表示ルール

## ログ

ASP.NET Coreの標準Loggingを使用します。運用環境では以下を分けて追跡できるようにします。

- 認証失敗
- 権限拒否
- 帳票出力成功/失敗
- SQL例外
- ファイル保存失敗
