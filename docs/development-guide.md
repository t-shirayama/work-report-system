# 開発ガイド

## 目的

このガイドは、バックエンドの入口、処理の流れ、新規API追加時の作業順をまとめたものです。

詳細なレイヤ責務は [アーキテクチャ](architecture.md)、API一覧は [API設計](api.md)、DB変更は [DB設計](database.md)、テスト観点は [テスト方針](testing.md) を参照してください。

## エントリーポイント

バックエンドの起動入口は `backend/WorkReport.Api/Program.cs` です。

`Program.cs` は以下だけを担います。

- Controller、OpenAPI、CORS、Cookie認証、CSRF、認可ポリシーの登録
- Application / Infrastructure のDI登録呼び出し
- 例外ハンドリング
- ミドルウェア順序の定義
- Controllerルーティング

主な登録は拡張メソッドへ分けています。

```csharp
builder.Services.AddControllers();
builder.Services.AddOpenApi();
builder.Services.AddApiCors(builder.Configuration);
builder.Services.AddApiAuthentication();
builder.Services.AddApplication();
builder.Services.AddInfrastructure(builder.Configuration);
```

| 登録 | 場所 | 内容 |
|---|---|---|
| `AddApiCors` | `WorkReport.Api/Extensions/ServiceCollectionExtensions.cs` | フロントエンド許可Origin、Cookie送信 |
| `AddApiAuthentication` | `WorkReport.Api/Extensions/ServiceCollectionExtensions.cs` | Cookie認証、CSRF、認可ポリシー |
| `AddApplication` | `WorkReport.Application/DependencyInjection.cs` | Application Service |
| `AddInfrastructure` | `WorkReport.Infrastructure/DependencyInjection.cs` | Repository、Hasher、帳票生成、ファイル保存 |

## 既存の入口一覧

HTTPの入口はすべて `backend/WorkReport.Api/Controllers/` 配下です。まずControllerを見れば、URL、認証要否、CSRF要否、呼び出すApplication Serviceが分かります。

| 機能 | Controller | 主なApplication | 主なInfrastructure |
|---|---|---|---|
| 認証 | `AuthController` | `Auth/AuthService` | `UserRepository`, `BcryptPasswordHasher` |
| ダッシュボード | `DashboardController` | `Dashboard/DashboardService` | `DashboardRepository` |
| 作業日報 | `WorkReportsController` | `WorkReports/WorkReportService`, `WorkReports/WorkReportValidator` | `WorkReportRepository` |
| 月次帳票 | `MonthlyReportsController` | `MonthlyReports/MonthlyReportService` | `MonthlyReportRepository`, `MonthlyReportWorkbookGenerator`, `LocalReportFileStorage`, `ReportHistoryRepository` |
| 帳票履歴 | `ReportHistoriesController` | `ReportHistories/ReportHistoryService`, `MonthlyReports/MonthlyReportService` | `ReportHistoryRepository`, `LocalReportFileStorage` |
| マスタ管理 | `MasterController` | `Masters/MasterDataService`, `DepartmentValidator`, `UserValidator` | `MasterDataRepository`, `BcryptPasswordHasher` |

処理を追うときは、Controller -> Service -> Interface -> Infrastructure実装の順に開きます。Interfaceは `WorkReport.Application/Interfaces/` にあり、実装は `WorkReport.Infrastructure/` 側にあります。

## 処理の流れ

代表的なリクエスト処理は以下の順で進みます。

```text
React SPA
  |
  | HTTP request / Cookie / X-CSRF-TOKEN
  v
WorkReport.Api Controller
  |
  | request DTO / CurrentUser
  v
WorkReport.Application Service
  |
  | validation / use case / port interface
  v
WorkReport.Application Interface
  |
  | DI
  v
WorkReport.Infrastructure Repository or Adapter
  |
  | Dapper / ClosedXML / file system / BCrypt
  v
SQL Server or generated-reports
```

依存方向は以下を守ります。

```text
WorkReport.Api -> WorkReport.Application -> WorkReport.Domain
WorkReport.Infrastructure -> WorkReport.Application -> WorkReport.Domain
```

`WorkReport.Application` は `WorkReport.Infrastructure` を参照しません。DB、ファイル、Excel、パスワードハッシュなどの実装詳細はInterface越しに利用します。

## レイヤ責務

| レイヤ | 主な場所 | 書くもの | 書かないもの |
|---|---|---|---|
| API | `WorkReport.Api/Controllers/` | HTTP入力、認証/認可属性、CSRF検証、HTTPレスポンス | SQL、業務判断、DB例外処理 |
| Application | `WorkReport.Application/{Feature}/` | ユースケース、入力検証、正規化、権限判断、Interface | Dapper、SqlConnection、ClosedXML、ファイルI/O |
| Domain | `WorkReport.Domain/` | 業務モデル、固定コード | HTTP、DB、画面都合 |
| Infrastructure | `WorkReport.Infrastructure/` | Repository、外部実装、DB制約例外の変換 | Controller、HTTPレスポンス |

## 認証とCSRFの流れ

### ログイン

```text
AuthController.Login
  |
  | LoginRequest
  v
AuthService.AuthenticateAsync
  |
  | IUserRepository.FindByLoginIdAsync
  | IPasswordHasher.Verify
  v
UserRepository / BcryptPasswordHasher
  |
  v
AuthController.SignInAsync
  |
  | CookieAuthenticationDefaults.AuthenticationScheme
  v
WORKREPORT-AUTH Cookie
```

`AuthService` はユーザー照合だけを行います。Cookie発行はHTTPの責務なので `AuthController` にあります。

### 現在ユーザー

認証済みユーザーはCookieのClaimsから復元します。

```text
Controller
  |
  | User.RequireCurrentUser()
  v
CurrentUserPrincipal
  |
  v
WorkReport.Domain.Models.Identity.CurrentUser
```

`CurrentUserPrincipal` は `WorkReport.Api/Security/CurrentUserPrincipal.cs` にあります。Application Serviceへ渡すログインユーザー情報は `CurrentUser` です。

### CSRF

更新系APIではControllerで以下を呼びます。

```csharp
await antiforgery.ValidateRequestAsync(HttpContext);
```

対象は主に `POST`、`PUT`、`DELETE` です。フロントエンドは事前に `GET /api/auth/csrf` でトークンを取得し、`X-CSRF-TOKEN` ヘッダーで送ります。

## 新規API追加手順

### 1. API仕様を決める

以下を先に決めます。

- Method
- Path
- 認証要否
- 管理者権限要否
- CSRF要否
- Request DTO
- Response DTO
- 主なエラー

更新先:

- `docs/api.md`
- 必要なら `docs/integration-test-cases.md`

### 2. Contractsを追加する

Request / Response DTO は `backend/WorkReport.Application/Contracts/` 配下に機能単位で追加します。

例:

```text
WorkReport.Application/
  Contracts/
    WorkReports/
      WorkReportRegisterRequest.cs
      WorkReportRegisterResponse.cs
```

namespace は現時点では統一して `WorkReport.Application.Contracts` にします。

```csharp
namespace WorkReport.Application.Contracts;
```

### 3. Domainに必要な業務概念を追加する

業務上の固定コードや型として扱うものは `WorkReport.Domain/` に置きます。

例:

- `Codes/WorkCategory.cs`
- `Codes/RoleCode.cs`
- `Models/Identity/CurrentUser.cs`
- `Models/Reporting/MonthlyReportModels.cs`

単なるHTTP入力チェックだけならApplicationのValidatorで十分です。

### 4. Application Serviceを追加または更新する

機能単位のフォルダへ置きます。

```text
WorkReport.Application/
  WorkReports/
    WorkReportService.cs
    WorkReportValidator.cs
```

Serviceは以下を担当します。

- ユースケースの流れ
- Validator呼び出し
- 権限判断
- Interface経由のRepository呼び出し
- Infrastructureから変換された制約エラーの扱い

入力検証と正規化はServiceへ抱え込まず、Validatorへ寄せます。

### 5. Interfaceを追加する

DBや外部実装が必要な場合、ApplicationにPort Interfaceを追加します。

```text
WorkReport.Application/
  Interfaces/
    IWorkReportRepository.cs
```

Application Serviceは具象RepositoryではなくInterfaceに依存します。

### 6. Infrastructure実装を追加する

DBアクセスは `WorkReport.Infrastructure/Persistence/` に置きます。

```text
WorkReport.Infrastructure/
  Persistence/
    WorkReportRepository.cs
```

ルール:

- Dapperを使う
- SQL文字列へユーザー入力を直接連結しない
- バインド変数を使う
- SQL Server固有の制約例外はInfrastructureで受け、Application向け例外へ変換する

例:

```csharp
catch (SqlException ex) when (IsUniqueConstraint(ex))
{
    throw new RepositoryConstraintException(RepositoryConstraint.Unique);
}
```

### 7. DI登録を追加する

Application Serviceは `WorkReport.Application/DependencyInjection.cs` へ登録します。

```csharp
services.AddScoped<WorkReportService>();
```

Infrastructure実装は `WorkReport.Infrastructure/DependencyInjection.cs` へ登録します。

```csharp
services.AddScoped<IWorkReportRepository, WorkReportRepository>();
```

`Program.cs` へ個別登録を増やさないでください。

### 8. Controllerを追加または更新する

HTTP入口は `WorkReport.Api/Controllers/` に置きます。

Controllerの責務:

- Route定義
- `Authorize` 属性
- CSRF検証
- Query / Body の受け取り
- Application Service呼び出し
- HTTPステータスへ変換

ControllerにSQLや重い業務判断を書かないでください。

POST / PUT / DELETE は原則CSRF検証を行います。

```csharp
await antiforgery.ValidateRequestAsync(HttpContext);
```

### 9. フロントエンドAPIクライアントを更新する

フロントエンドから呼ぶ場合は `frontend/src/api.ts` に関数と型を追加します。

画面を追加する場合は `frontend/src/App.tsx` と必要なCSSを更新します。

### 10. テストを追加する

変更内容に応じて以下を追加します。

| 変更 | 追加先 |
|---|---|
| Validator、固定コード、帳票生成 | `backend/WorkReport.Tests/` |
| Controller、認証、CSRF、DBを含むAPI挙動 | `backend/WorkReport.IntegrationTests/` |
| 画面操作 | `frontend/e2e/` |
| フロントエンド純粋関数 | `frontend/src/**/*.test.*` |

最低限、以下を確認します。

```sh
dotnet test WorkReport.slnx
```

フロントエンド変更がある場合:

```sh
cd frontend
npm run build
npm run test
```

画面フロー変更がある場合:

```sh
npm run test:e2e
```

### 11. ドキュメントを更新する

| 変更内容 | 更新先 |
|---|---|
| エンドポイント追加 | `docs/api.md` |
| DB / SQL / seed変更 | `docs/database.md` |
| 処理フローや責務変更 | `docs/architecture.md`, `docs/development-guide.md` |
| テストケース追加 | `docs/testing.md`, `docs/integration-test-cases.md` |
| 起動・運用手順変更 | `docs/operations.md`, `README.md` |

## 例: 作業日報APIの流れ

### 登録

```text
WorkReportsController
  |
  | WorkReportRegisterRequest
  v
WorkReportService
  |
  | WorkReportValidator.ValidateRegister
  v
IWorkReportRepository
  |
  | DI
  v
WorkReportRepository
  |
  | INSERT INTO work_reports
  v
SQL Server
```

ポイント:

- `WorkReportsController.Register` がCSRFを検証する
- `WorkReportService.RegisterAsync` がユースケースを進める
- `WorkReportValidator.ValidateRegister` が入力検証を行う
- `WorkCategory` は `WorkReport.Domain/Codes/WorkCategory.cs` にある
- DB登録は `WorkReportRepository.InsertAsync` が担当する

### 検索

```text
WorkReportsController.Search
  |
  | query -> WorkReportSearchRequest
  v
WorkReportService.ValidateSearch
  |
  v
WorkReportService.SearchAsync
  |
  v
IWorkReportRepository.SearchAsync
  |
  v
WorkReportRepository.SearchAsync
  |
  | SELECT work_reports ...
  v
SQL Server
```

検索条件の組み立てとSQLはRepositoryにあります。ControllerやServiceでSQL条件を組み立てないでください。

## 例: 月次帳票出力の流れ

```text
MonthlyReportsController
  |
  | MonthlyReportExportRequest
  v
MonthlyReportService
  |
  | IMonthlyReportRepository
  | IMonthlyReportWorkbookGenerator
  | IReportFileStorage
  | IReportHistoryRepository
  v
MonthlyReportRepository / MonthlyReportWorkbookGenerator / LocalReportFileStorage
  |
  v
SQL Server / generated-reports
```

ポイント:

- `MonthlyReportsController.Export` がCSRFを検証する
- `MonthlyReportService.ValidateExport` が対象年月と対象ユーザーを検証する
- 一般ユーザーが他人の帳票を出そうとした場合は `MonthlyReportService.ExportAsync` で拒否する
- 帳票データ取得は `MonthlyReportRepository.BuildMonthlyReportAsync`
- Excel生成は `MonthlyReportWorkbookGenerator`
- ファイル保存は `LocalReportFileStorage`
- 履歴登録は `ReportHistoryRepository.InsertSuccessAsync`

## 例: マスタ管理APIの流れ

部署追加:

```text
MasterController.CreateDepartment
  |
  | DepartmentUpsertRequest
  v
MasterDataService.CreateDepartmentAsync
  |
  | DepartmentValidator.Normalize
  | DepartmentValidator.Validate
  v
IMasterDataRepository.InsertDepartmentAsync
  |
  v
MasterDataRepository.InsertDepartmentAsync
```

ユーザー追加:

```text
MasterController.CreateUser
  |
  | MasterUserCreateRequest
  v
MasterDataService.CreateUserAsync
  |
  | UserValidator.Normalize
  | UserValidator.Validate
  | IPasswordHasher.Hash
  v
IMasterDataRepository.InsertUserAsync
  |
  v
MasterDataRepository.InsertUserAsync
```

ポイント:

- マスタ管理は `Authorize(Policy = "Admin")` が必要
- `RoleCode` は `WorkReport.Domain/Codes/RoleCode.cs` にある
- 重複や外部キー制約はInfrastructureで `RepositoryConstraintException` に変換し、Serviceで日本語エラーへ変換する
- 削除APIはまだありません。FK整合性や業務履歴の扱いを決めてから追加してください。

## 例: 帳票履歴APIの流れ

履歴一覧:

```text
ReportHistoriesController.Search
  |
  v
ReportHistoryService.SearchAsync
  |
  v
IReportHistoryRepository.SearchAsync
  |
  v
ReportHistoryRepository.SearchAsync
```

再ダウンロード:

```text
ReportHistoriesController.Download
  |
  v
MonthlyReportService.DownloadHistoryAsync
  |
  | IReportHistoryRepository.FindDetailAsync
  | IReportFileStorage.ReadAsync
  v
ReportHistoryRepository / LocalReportFileStorage
```

再ダウンロードは帳票履歴の詳細と保存済みファイルの両方が必要なので、`MonthlyReportService` を使っています。

## DBアクセスの追い方

DBアクセスは `WorkReport.Infrastructure/Persistence/` に集約しています。

| Repository | 主なテーブル |
|---|---|
| `UserRepository` | `users`, `departments` |
| `DashboardRepository` | `work_reports`, `report_output_histories`, `users` |
| `WorkReportRepository` | `work_reports`, `users`, `departments` |
| `MonthlyReportRepository` | `users`, `departments`, `work_reports` |
| `ReportHistoryRepository` | `report_output_histories`, `users`, `departments` |
| `MasterDataRepository` | `departments`, `users` |

接続生成は `SqlConnectionFactory` が担当します。接続文字列は `ConnectionStrings:WorkReport` です。

DB変更時は以下も確認します。

- `database/sqlserver/schema.sql`
- `database/sqlserver/seed.sql`
- `database/sqlserver/seed-empty.sql`
- `docs/database.md`
- `docs/integration-test-cases.md`

## 例外とエラーレスポンス

API全体の例外ハンドリングは `Program.cs` の `UseExceptionHandler` にあります。

| 例外 | HTTP |
|---|---|
| `BadHttpRequestException` | 400 |
| `AntiforgeryValidationException` | 400 |
| `UnauthorizedAccessException` | 403 |
| `KeyNotFoundException` | 404 |
| その他 | 500 |

入力検証エラーは例外ではなく、`ErrorResponse` を返します。

```json
{
  "errors": [
    "作業日は必須です。"
  ]
}
```

DB制約違反のようなInfrastructure固有の例外は、InfrastructureでApplication向け例外に変換します。Controllerで `SqlException` を扱わないでください。

## フロントエンドから見た流れ

フロントエンドのAPI呼び出しは `frontend/src/api.ts` に集約しています。

画面追加時は以下を順に確認します。

1. `frontend/src/api.ts` にAPI関数を追加する
2. `frontend/src/App.tsx` に画面、状態、イベントを追加する
3. 必要なら `frontend/src/styles.css` を更新する
4. E2Eが必要な画面なら `frontend/e2e/` にテストを追加する

Cookie認証を使うため、API呼び出しではCookie送信が必要です。更新系APIではCSRFトークンも送ります。

## 判断基準

- HTTP、Cookie、CSRF、ステータスコードならAPI層
- ユースケース、入力検証、権限判断ならApplication層
- 業務概念、固定コード、業務上守るべき型ならDomain層
- SQL、ファイル、Excel、ハッシュなど実装詳細ならInfrastructure層
- `Program.cs` に個別ServiceやRepository登録を増やさない
- Serviceが長くなったらValidator、Mapper、Domainモデルへ責務を移す
