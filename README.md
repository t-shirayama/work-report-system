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
    sqlserver/             DDL and seed data
  docs/                    Architecture and operation docs
  docker-compose.yml       SQL Server for local development
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

## ローカル起動

### 1. SQL Server

```powershell
docker compose up -d
```

`WorkReport` データベースを作成し、以下の順でSQLを実行します。

```text
database/sqlserver/schema.sql
database/sqlserver/seed.sql
```

既定の接続文字列は [appsettings.json](backend/WorkReport.Api/appsettings.json) にあります。

### 2. API

```powershell
dotnet run --project backend/WorkReport.Api/WorkReport.Api.csproj --urls http://localhost:5000
```

### 3. フロントエンド

```powershell
cd frontend
npm install
npm run dev
```

ブラウザで `http://localhost:5173` を開きます。

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

```powershell
dotnet test WorkReport.slnx
```

SQL Serverコンテナを使うAPI結合テストだけを実行する場合:

```powershell
dotnet test backend/WorkReport.IntegrationTests/WorkReport.IntegrationTests.csproj
```

Dockerが起動していない環境では、結合テストはスキップされます。

```powershell
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
