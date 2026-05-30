# 10. バックエンドレイヤ別シーケンス図

## 目的

ASP.NET Core Web APIへ移行した後のバックエンド内部処理を、レイヤごとの責務が分かる形で整理します。Reactから見たAPI呼び出しの流れは [09-api-processing-flow.md](09-api-processing-flow.md) を参照し、このドキュメントではAPIサーバー内部の Controller、Application Service、Repository、SQL Server、帳票、ファイル保存の分担に焦点を当てます。

## レイヤ構成

| レイヤ | 主な責務 |
|---|---|
| Controller | HTTP入出力、認証ユーザー取得、DTOバインド、ステータスコード |
| Request Validator | DataAnnotationsまたはFluentValidationによる形式チェック |
| Application Service | 業務ルール、権限補正、トランザクション、補償処理 |
| Repository / Query Service | SQL Serverアクセス、SQL、バインド変数、行マッピング |
| Domain / Policy | ロール判定、業務状態判定、値オブジェクト |
| Reporting Service | Excelテンプレート読み込み、セル差し込み、`.xlsx` 生成 |
| File Storage | 保存先解決、ファイル名検証、保存、読み込み、削除 |

## 認証

```mermaid
sequenceDiagram
    autonumber
    participant C as AuthController
    participant V as LoginRequestValidator
    participant S as AuthApplicationService
    participant U as UserRepository
    participant P as PasswordVerifier
    participant DB as SQL Server
    participant A as CookieAuth

    C->>V: Validate(LoginRequest)
    alt 入力エラー
        V-->>C: Validation errors
        C-->>C: 400 ValidationProblem
    else 入力OK
        C->>S: LoginAsync(loginId, password)
        S->>U: FindByLoginIdAsync(loginId)
        U->>DB: SELECT users WHERE login_id = @loginId
        DB-->>U: UserRecord
        U-->>S: User
        S->>P: Verify(password, user.passwordHash)
        alt 認証失敗
            P-->>S: false
            S-->>C: AuthenticationFailed
            C-->>C: 401 Unauthorized
        else 認証成功
            P-->>S: true
            S-->>C: CurrentUser
            C->>A: SignInAsync(claims)
            A-->>C: Auth cookie
            C-->>C: 200 OK
        end
    end
```

## ダッシュボード取得

```mermaid
sequenceDiagram
    autonumber
    participant C as DashboardController
    participant S as DashboardApplicationService
    participant Q as DashboardQueryService
    participant DB as SQL Server

    C->>C: GetCurrentUser()
    C->>S: GetDashboardAsync(currentUser)
    S->>Q: CountTodayReportsAsync(currentUser)
    Q->>DB: SELECT COUNT(*) FROM work_reports
    DB-->>Q: todayCount
    S->>Q: SumThisMonthHoursAsync(currentUser)
    Q->>DB: SELECT SUM(work_hours) FROM work_reports
    DB-->>Q: monthHours
    S->>Q: CountNotOutputReportsAsync(currentUser)
    Q->>DB: SELECT 未出力件数
    DB-->>Q: notOutputCount
    S->>Q: FindRecentActivitiesAsync(currentUser)
    Q->>DB: SELECT 最近の活動
    DB-->>Q: activities
    Q-->>S: query results
    S-->>C: DashboardResponse
    C-->>C: 200 OK
```

## 作業日報登録

```mermaid
sequenceDiagram
    autonumber
    participant C as WorkReportsController
    participant V as WorkReportRequestValidator
    participant S as WorkReportApplicationService
    participant R as WorkReportRepository
    participant DB as SQL Server

    C->>C: GetCurrentUser()
    C->>V: Validate(CreateWorkReportRequest)
    alt 入力エラー
        V-->>C: Validation errors
        C-->>C: 400 ValidationProblem
    else 入力OK
        C->>S: RegisterAsync(request, currentUser)
        S->>S: 業務チェック（分類、作業時間、文字数）
        S->>S: user_id / department_idをcurrentUserから設定
        S->>R: InsertAsync(workReport)
        R->>DB: INSERT work_reports
        DB-->>R: created work_report_id
        R-->>S: WorkReportId
        S-->>C: CreateWorkReportResponse
        C-->>C: 201 Created
    end
```

## 作業実績検索

```mermaid
sequenceDiagram
    autonumber
    participant C as WorkReportsController
    participant V as WorkReportSearchValidator
    participant S as WorkReportApplicationService
    participant Q as WorkReportQueryService
    participant DB as SQL Server

    C->>C: GetCurrentUser()
    C->>V: Validate(SearchWorkReportsQuery)
    alt 入力エラー
        V-->>C: Validation errors
        C-->>C: 400 ValidationProblem
    else 入力OK
        C->>S: SearchAsync(query, currentUser)
        alt currentUser.role == USER
            S->>S: userId = currentUser.userId に固定
        else currentUser.role == ADMIN
            S->>S: 全体検索を許可
        end
        S->>Q: SearchAsync(normalizedCondition)
        Q->>DB: SELECT work_reports JOIN users JOIN departments
        DB-->>Q: rows
        Q-->>S: WorkReportSearchResult[]
        S-->>C: SearchWorkReportsResponse
        C-->>C: 200 OK
    end
```

## 月次報告書出力

```mermaid
sequenceDiagram
    autonumber
    participant C as MonthlyReportsController
    participant V as MonthlyReportRequestValidator
    participant S as MonthlyReportApplicationService
    participant U as UserRepository
    participant H as ReportHistoryRepository
    participant Q as MonthlyReportQueryService
    participant X as ExcelReportService
    participant F as ReportFileStorage
    participant DB as SQL Server

    C->>C: GetCurrentUser()
    C->>V: Validate(ExportMonthlyReportRequest)
    alt 入力エラー
        V-->>C: Validation errors
        C-->>C: 400 ValidationProblem
    else 入力OK
        C->>S: ExportAsync(request, currentUser)
        S->>S: 対象年月をYYYYMMへ正規化
        alt currentUser.role == USER
            S->>S: targetUser = currentUser
        else currentUser.role == ADMIN
            S->>U: FindReportTargetUserAsync(request.userId)
            U->>DB: SELECT users WHERE user_id = @userId AND role_code = 'USER'
            DB-->>U: targetUser
            U-->>S: targetUser
        end
        S->>H: InsertProcessingAsync(history)
        H->>DB: INSERT report_output_histories status=PROCESSING
        DB-->>H: historyId
        H-->>S: historyId
        S->>Q: FindReportDataAsync(targetUser, targetYearMonth)
        Q->>DB: SELECT summary/category/daily details
        DB-->>Q: report rows
        Q-->>S: MonthlyReportData
        S->>X: CreateAsync(reportData, template)
        X-->>S: xlsx bytes
        S->>F: SaveAsync(targetYearMonth, fileName, bytes)
        F-->>S: savedPath
        S->>H: UpdateSuccessAsync(historyId, fileName, savedPath)
        H->>DB: UPDATE report_output_histories status=SUCCESS
        S-->>C: MonthlyReportFile
        C-->>C: 200 File(bytes, xlsx)
    end
```

## 月次報告書出力の補償処理

```mermaid
sequenceDiagram
    autonumber
    participant S as MonthlyReportApplicationService
    participant H as ReportHistoryRepository
    participant X as ExcelReportService
    participant F as ReportFileStorage
    participant DB as SQL Server

    S->>H: InsertProcessingAsync(history)
    H->>DB: INSERT status=PROCESSING
    DB-->>H: historyId
    H-->>S: historyId
    S->>X: CreateAsync(reportData, template)
    alt Excel生成失敗
        X-->>S: exception
        S->>F: DeleteIfExistsAsync(savedPath)
        S->>H: UpdateErrorAsync(historyId, errorMessage)
        H->>DB: UPDATE status=ERROR
        S-->>S: throw
    else ファイル保存後に履歴更新失敗
        X-->>S: xlsx bytes
        S->>F: SaveAsync(...)
        F-->>S: savedPath
        S->>H: UpdateSuccessAsync(historyId, fileName, savedPath)
        H-->>S: exception
        S->>F: DeleteIfExistsAsync(savedPath)
        S->>H: UpdateErrorBestEffortAsync(historyId, errorMessage)
        S-->>S: throw
    end
```

## 帳票履歴検索

```mermaid
sequenceDiagram
    autonumber
    participant C as ReportHistoriesController
    participant V as ReportHistorySearchValidator
    participant S as ReportHistoryApplicationService
    participant Q as ReportHistoryQueryService
    participant DB as SQL Server

    C->>C: GetCurrentUser()
    C->>V: Validate(SearchReportHistoriesQuery)
    alt 入力エラー
        V-->>C: Validation errors
        C-->>C: 400 ValidationProblem
    else 入力OK
        C->>S: SearchAsync(query, currentUser)
        alt currentUser.role == USER
            S->>S: targetUserId = currentUser.userId に固定
        else currentUser.role == ADMIN
            S->>S: 全履歴検索を許可
        end
        S->>Q: SearchAsync(normalizedCondition)
        Q->>DB: SELECT report_output_histories JOIN users
        DB-->>Q: rows
        Q-->>S: ReportHistorySummary[]
        S-->>C: SearchReportHistoriesResponse
        C-->>C: 200 OK
    end
```

## 帳票履歴詳細

```mermaid
sequenceDiagram
    autonumber
    participant C as ReportHistoriesController
    participant S as ReportHistoryApplicationService
    participant R as ReportHistoryRepository
    participant DB as SQL Server

    C->>C: GetCurrentUser()
    C->>S: FindDetailAsync(historyId, currentUser)
    S->>R: FindByIdAsync(historyId)
    R->>DB: SELECT report_output_histories WHERE id = @historyId
    DB-->>R: history
    R-->>S: ReportHistory
    alt 履歴なし
        S-->>C: NotFound
        C-->>C: 404 Not Found
    else 権限なし
        S-->>C: NotFound or Forbidden
        C-->>C: 404 or 403
    else 権限あり
        S->>S: ADMIN以外には内部file_pathを返さない
        S-->>C: ReportHistoryDetailResponse
        C-->>C: 200 OK
    end
```

## 帳票再ダウンロード

```mermaid
sequenceDiagram
    autonumber
    participant C as ReportHistoriesController
    participant S as ReportHistoryApplicationService
    participant R as ReportHistoryRepository
    participant F as ReportFileStorage
    participant DB as SQL Server

    C->>C: GetCurrentUser()
    C->>S: DownloadAsync(historyId, currentUser)
    S->>R: FindByIdAsync(historyId)
    R->>DB: SELECT report_output_histories WHERE id = @historyId
    DB-->>R: history
    R-->>S: ReportHistory
    alt 履歴なし / 権限なし
        S-->>C: NotFound or Forbidden
        C-->>C: 404 or 403
    else status != SUCCESS
        S-->>C: Conflict
        C-->>C: 409 Conflict
    else SUCCESS
        S->>F: ReadAsync(history.filePath)
        F->>F: 保存領域配下かreal pathで検証
        alt ファイルなし / 保存領域外
            F-->>S: NotFound
            S-->>C: NotFound
            C-->>C: 404 Not Found
        else ファイル取得OK
            F-->>S: file bytes
            S-->>C: DownloadFile
            C-->>C: 200 File(bytes, xlsx, fileName)
        end
    end
```

## トランザクション境界

```mermaid
graph TD
    A["Controller"] --> B["Application Service"]
    B --> C{"DB更新のみ?"}
    C -- "Yes" --> D["DB Transaction内でRepositoryを実行"]
    D --> E["Commit"]
    C -- "No: DB + File" --> F["PROCESSING履歴をDB保存"]
    F --> G["Excel生成"]
    G --> H["ファイル保存"]
    H --> I["SUCCESS履歴更新"]
    G -- "失敗" --> J["ERROR履歴更新"]
    H -- "失敗" --> J
    I -- "失敗" --> K["保存済みファイル削除"]
    K --> J
```

## 実装ルール

- ControllerにSQL、ファイルパス検証、帳票生成ロジックを書きません。
- Application Serviceはログインユーザーを受け取り、`ADMIN` / `USER` の制約を必ず再評価します。
- Repository / Query ServiceはSQL Server向けSQLとパラメータバインドを担当します。
- Reporting ServiceはExcelテンプレートと出力データだけを受け取り、DBやHTTPに依存しません。
- File Storageは保存領域外アクセス防止を担当し、Application Serviceは直接ファイルシステムを触りません。
