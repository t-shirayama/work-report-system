# work-report-system

作業日報登録と月次報告書Excel出力を行う業務システムです。

このリポジトリは React + ASP.NET Core Web API + SQL Server 構成です。

## 技術スタック

| 分類 | 採用技術 |
|---|---|
| フロントエンド | React 19, TypeScript, Vite, lucide-react |
| バックエンド | C# ASP.NET Core Web API (.NET 10) |
| DB | SQL Server 2022 |
| DBアクセス | Dapper + Microsoft.Data.SqlClient |
| 認証 | Cookie認証 + ASP.NET Core Antiforgery |
| パスワード | BCrypt.Net-Next |
| 帳票 | ClosedXML |
| テスト | xUnit, Microsoft.AspNetCore.Mvc.Testing |

## ディレクトリ構成

```text
work-report-system/
  backend/
    WorkReport.Api/        ASP.NET Core Web API
    WorkReport.Tests/      xUnit tests
  frontend/                React SPA
  database/
    sqlserver/             DDL, seed data, DB init script
  docs/                    Architecture and operation docs
  docker-compose.yml       App, API, SQL Server for local development
  WorkReport.slnx          .NET solution
```

## 機能

- ログイン、ログアウト、ログインユーザー取得
- CSRFトークン発行
- ダッシュボード集計
- 作業日報登録
- 作業実績検索
- 月次報告書Excel出力
- 帳票作成履歴検索
- 作成済み帳票の再ダウンロード
- 部署、ユーザーのマスタ管理

## ローカル起動

### 1. Docker Compose

```sh
docker compose up -d --build
```

以下がすべてDocker上で起動します。

| Service | URL / 役割 |
|---|---|
| `frontend` | `http://localhost:5173` |
| `backend` | `http://localhost:5000/api` |
| `sqlserver` | `localhost:1433` |
| `db-init` | `WorkReport` DB作成、DDL/seed投入 |

`db-init` は `departments` テーブルが存在しない場合だけ `schema.sql` と `seed.sql` を投入します。既に初期化済みのDBはスキップします。

初期データは `DB_SEED_MODE` で切り替えます。

| 値 | 内容 |
|---|---|
| `sample` | 部署、ユーザー、日報、帳票履歴のサンプルを投入 |
| `empty` | 管理者ログインに必要な最小マスタだけ投入 |
| `none` | DDLのみ投入 |

例:

```sh
DB_SEED_MODE=empty docker compose up -d --build
```

ブラウザで `http://localhost:5173` を開きます。

### 2. データリセット

現在の `WorkReport` DBを作り直し、`DB_SEED_MODE` に応じた初期データを再投入します。

```sh
docker compose --profile reset run --rm db-reset
docker compose up -d --build
```

初期データなし相当でリセットする場合:

```sh
DB_SEED_MODE=empty docker compose --profile reset run --rm db-reset
docker compose up -d --build
```

### 3. 停止

```sh
docker compose down
```

DBデータと帳票ファイルのDocker volumeも削除して初期状態へ戻す場合:

```sh
docker compose down -v
```

## サンプルアカウント

初期パスワードは全ユーザー `password` です。

| ログインID | 権限 |
|---|---|
| `admin` | 管理者 |
| `sato` | 一般ユーザー |
| `suzuki` | 一般ユーザー |
| `tanaka` | 一般ユーザー |
| `yamada` | 一般ユーザー |

## 検証

```sh
dotnet test WorkReport.slnx
```

SQL Serverコンテナを使うAPI結合テストだけを実行する場合:

```sh
dotnet test backend/WorkReport.IntegrationTests/WorkReport.IntegrationTests.csproj
```

Dockerが起動していない環境では、結合テストはスキップされます。

```sh
cd frontend
npm run build
npm run test
npm run test:e2e
```

`npm run test:e2e` は、APIが `http://localhost:5000/api` で起動済みで、テスト用SQL Serverデータが投入されている前提です。

## ドキュメント

- [ドキュメント索引](docs/README.md)
- [アーキテクチャ](docs/architecture.md)
- [API設計](docs/api.md)
- [DB設計](docs/database.md)
- [テスト方針](docs/testing.md)
- [ITテストケース一覧](docs/integration-test-cases.md)
- [運用メモ](docs/operations.md)
