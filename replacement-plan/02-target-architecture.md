# 02. 目標アーキテクチャ

## 全体像

```text
Browser
  |
  | HTTPS
  v
React SPA
  |
  | JSON API / Blob Download
  v
ASP.NET Core Web API
  |
  | Microsoft.Data.SqlClient / ORM or SQL Mapper
  v
SQL Server

ASP.NET Core Web API
  |
  | Excel template read/write
  v
Report storage
```

## 技術構成案

| 分類 | 方針 |
|---|---|
| フロントエンド | React SPA。ルーティング、フォーム、API通信、認証状態を管理 |
| バックエンド | ASP.NET Core Web API。Controller、Application Service、Repository、Domain Modelを分離 |
| DB | SQL Server。日本語文字列は原則 `nvarchar`、日時は用途に応じて `date` / `datetime2` |
| 認証 | 初期案はCookie認証 + ASP.NET Core Antiforgery。JWT採用は未決事項 |
| 認可 | ロール `ADMIN` / `USER` をPolicyまたはAuthorizationHandlerで制御 |
| 帳票 | 既存Excelテンプレート互換を優先。C#側のExcelライブラリで値差し込み |
| ファイル保存 | `generated-reports` 相当の保存領域をAPIサーバーまたは外部ストレージに配置 |
| ログ | アプリログ、認証ログ、帳票出力ログ、例外ログを分離して追跡可能にする |

## レイヤ構成

| 現行 | 移行後 | 役割 |
|---|---|---|
| Spring MVC Controller | ASP.NET Core Controller | HTTP、認証ユーザー、入力DTO、レスポンス |
| Service | Application Service | 業務判断、入力チェック、トランザクション境界 |
| DAO | Repository / Query Service | SQL Serverアクセス、検索、集計 |
| Form | Request DTO | API入力値 |
| DTO | Response DTO / Application DTO | APIレスポンス、レイヤ間データ |
| Entity | Domain Model / Data Model | DB行、業務モデル |
| JSP | React Page / Component | 画面表示、入力、状態管理 |

## プロジェクト構成案

```text
replacement-system/
  frontend/
    src/
      app/
      components/
      features/
      routes/
      services/
      styles/
  backend/
    WorkReport.Api/
      Controllers/
      Contracts/
      Application/
      Domain/
      Infrastructure/
      Security/
      Reporting/
    WorkReport.Tests/
  database/
    sqlserver/
      migrations/
      seed/
  docs/
```

## 認証方式

初期案はCookie認証 + Antiforgeryです。現行のSpring Security Form LoginとCSRFの移行イメージに近く、社内業務システムとしてセッション失効、ログアウト、SameSite Cookieを扱いやすいためです。

| 項目 | 方針 |
|---|---|
| ログイン | `POST /api/auth/login` でCookie発行 |
| ログアウト | `POST /api/auth/logout` でCookie破棄 |
| ログインユーザー | `GET /api/auth/me` で取得 |
| CSRF | `GET /api/auth/csrf` でトークン取得、unsafe methodに `X-CSRF-TOKEN` を付与 |
| CORS | ReactとAPIのホスト分離時のみ許可オリジンを限定 |
| 権限 | API側で必ず検証し、React側の表示制御だけに依存しない |

JWT/Bearer認証にする場合は、ログアウト、トークン失効、保存場所、XSSリスク、運用ルールを別途評価します。

現行 `users.password` はBCryptハッシュです。ASP.NET Core標準 `PasswordHasher` とは形式が異なるため、既存ハッシュを継続利用する場合は `BCrypt.Net` などで照合します。標準Hasherへ寄せる場合は、初回ログイン時の再ハッシュ移行またはパスワード再設定計画を用意します。

## DBアクセス方針

未決事項として、以下を比較して決定します。

| 選択肢 | 特徴 | 判断観点 |
|---|---|---|
| EF Core | モデル管理とMigrationに強い | SQLを明示したい帳票集計との相性 |
| Dapper | SQL明示、軽量。現行SQL資産を移植しやすい | Repository実装量、Migration別管理 |
| ADO.NET | 完全制御 | 保守性、実装量 |

現行はSpring JDBCで明示SQLを重視しているため、集計SQLや帳票SQLはSQLを明示できる方式を優先します。初期候補は `Dapper + SQLファイル管理` です。日付表示や曜日名など画面表示寄りの整形は、SQLではなくAPIまたはReactへ寄せます。

## 移行単位

| フェーズ | 内容 |
|---|---|
| 1 | 認証、ユーザー情報、共通レイアウト、API基盤 |
| 2 | SQL Server DDL、初期データ、データ移行リハーサル |
| 3 | ダッシュボード、作業日報登録、作業実績検索 |
| 4 | 月次帳票出力、帳票履歴、再ダウンロード |
| 5 | 現行比較テスト、性能確認、運用手順、切替 |

## 非機能方針

- 秘密情報は環境変数、Secret Manager、CI/CD変数で管理します。
- APIは入力検証、認可、SQLパラメータ化、ファイルパストラバーサル対策を必須にします。
- 帳票出力は長時間化を見越して、同期ダウンロードと非同期履歴化のどちらを採用するか決定します。
- 監査が必要な操作は、ログイン、ログアウト、帳票出力、再ダウンロードを優先して記録します。
