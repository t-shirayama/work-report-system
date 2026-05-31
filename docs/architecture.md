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
| Application | `backend/WorkReport.Application/` | ユースケース、入力検証、入力正規化、固定コード、Request/Response DTO、Port Interface |
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

DI登録は各レイヤの拡張メソッドへ分けます。Application Serviceは `WorkReport.Application/DependencyInjection.cs` の `AddApplication()`、Infrastructure実装は `WorkReport.Infrastructure/DependencyInjection.cs` の `AddInfrastructure(configuration)` に集約します。API固有のCORS、Cookie認証、CSRF、認可ポリシーは `WorkReport.Api/Extensions/ServiceCollectionExtensions.cs` に置き、`Program.cs` は起動設定とミドルウェアの流れを読む場所にします。

外部公開するJSON形は `WorkReport.Application/Contracts/`、内部で受け渡す業務データは `WorkReport.Domain/Models/`、処理結果モデルは `WorkReport.Application/Models/` に置きます。現時点の入口はReact SPA + Web APIに限定されるため、Controllerを薄く保つ目的でApplication ContractsをAPI DTOとして共有します。

将来、同じApplicationをバッチ、CLI、別API、メッセージ処理など複数の入口から利用する場合は、HTTP Request/Response DTOを `WorkReport.Api/Contracts/` へ分離し、`WorkReport.Application` にはUseCase専用のInput/Outputモデルを置く方針へ見直します。その場合、ControllerまたはMapperでAPI DTOとApplicationモデルを変換します。

Contractsは機能単位のフォルダへ分けます。例: `Auth/`、`Dashboard/`、`WorkReports/`、`MonthlyReports/`、`ReportHistories/`、`Masters/`、`Common/`。namespaceは `WorkReport.Application.Contracts` に統一し、利用側は機能別フォルダ構成に依存しないようにします。

入力検証と正規化はServiceへ抱え込まず、機能単位のValidatorへ分けます。作業日報は `WorkReports/WorkReportValidator.cs`、マスタ管理は `Masters/DepartmentValidator.cs` と `Masters/UserValidator.cs` に置き、固定コードは `WorkCategory.cs` や `RoleCode.cs` で管理します。Serviceはユースケースの流れ、Repository呼び出し、外部制約エラーの扱いを中心にします。

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
