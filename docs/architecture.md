# アーキテクチャ

## 全体像

```text
Browser
  |
  v
React SPA
  |
  | JSON API / Blob Download
  v
ASP.NET Core Web API
  |
  | Dapper / Microsoft.Data.SqlClient
  v
SQL Server

ASP.NET Core Web API
  |
  | ClosedXML
  v
generated-reports/
```

## レイヤ構成

| レイヤ | 主な場所 | 責務 |
|---|---|---|
| UI | `frontend/src/App.tsx` | 画面表示、フォーム状態、画面切替 |
| API Client | `frontend/src/api.ts` | API URL、CSRF、Cookie送信、Blobダウンロード |
| Endpoint | `backend/WorkReport.Api/Program.cs` | HTTP入力、認証/認可、レスポンス形式 |
| Contracts | `backend/WorkReport.Api/Contracts/` | Request/Response DTO |
| Application | `backend/WorkReport.Api/Application/` | 業務判断、入力チェック、権限判断 |
| Infrastructure | `backend/WorkReport.Api/Infrastructure/` | SQL Server接続、Dapper SQL |
| Reporting | `backend/WorkReport.Api/Reporting/` | Excel生成、帳票データ構造 |

## 認証とCSRF

- `POST /api/auth/login` でCookie認証を開始する。
- `POST /api/auth/logout` でCookieを破棄する。
- `GET /api/auth/me` で現在のログインユーザーを返す。
- `GET /api/auth/csrf` でCSRFトークンを取得する。
- ReactはPOST系リクエストで `X-CSRF-TOKEN` を送信する。

## 権限

- `ADMIN` は全ユーザーの作業実績検索、帳票出力対象者取得ができる。
- `USER` は自分の作業実績と帳票出力を基本にする。
- 表示制御だけに依存せず、API側で認可判断を行う。

## 帳票

月次帳票はAPI側で集計データを取得し、ClosedXMLで `.xlsx` を生成します。生成ファイルは `generated-reports/` 配下に保存し、`report_output_histories` に履歴を登録します。
