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

```sh
docker compose up -d --build
```

フロントエンドは `http://localhost:5173`、APIは `http://localhost:5000/api` で公開されます。DB初期化は `db-init` サービスが行います。

初期データをサンプルなしにする場合:

```sh
DB_SEED_MODE=empty docker compose up -d --build
```

データリセット:

```sh
docker compose --profile reset run --rm db-reset
docker compose up -d --build
```

停止:

```sh
docker compose down
```

DBと帳票ファイルのvolumeも削除して初期化:

```sh
docker compose down -v
```

## 結合テスト環境

API結合テストは Testcontainers がSQL Serverコンテナを起動します。事前にDocker Desktopを起動してください。

```sh
dotnet test backend/WorkReport.IntegrationTests/WorkReport.IntegrationTests.csproj
```

フロントE2EはAPIとDBを事前に起動してから実行します。

```sh
docker compose up -d --build
cd frontend
npm run test:e2e
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
