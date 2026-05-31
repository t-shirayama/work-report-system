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
WorkReport.Api
  |
  | Controller / Cookie Auth / CSRF
  v
WorkReport.Application
  |
  | Use Case / Port Interface
  v
WorkReport.Domain

WorkReport.Api
  |
  | DI
  v
WorkReport.Infrastructure
  |
  | Dapper / Microsoft.Data.SqlClient
  v
SQL Server

WorkReport.Infrastructure
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
| API | `backend/WorkReport.Api/` | HTTP入口、Controller、Cookie認証、CSRF、CORS、DI、例外レスポンス |
| Application | `backend/WorkReport.Application/` | ユースケース、入力チェック、権限判断、Request/Response DTO、Port Interface |
| Domain | `backend/WorkReport.Domain/` | 認証ユーザー、帳票データなどの業務モデル |
| Infrastructure | `backend/WorkReport.Infrastructure/` | SQL Server接続、Dapper Repository、パスワードハッシュ、Excel生成、ファイル保存 |

## 依存方向

```text
WorkReport.Api
  -> WorkReport.Application
  -> WorkReport.Domain

WorkReport.Infrastructure
  -> WorkReport.Application
  -> WorkReport.Domain
```

`WorkReport.Application` は `WorkReport.Infrastructure` を参照しません。ApplicationはRepository、パスワードハッシュ、帳票生成、ファイル保存をInterfaceとして定義し、Infrastructureが実装します。`WorkReport.Api` はDIでInterfaceと実装を結び、ControllerからApplication Serviceを呼び出します。

外部公開するJSON形は `WorkReport.Application/Contracts/`、内部で受け渡す業務データは `WorkReport.Domain/Models/`、処理結果モデルは `WorkReport.Application/Models/` に置きます。

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
