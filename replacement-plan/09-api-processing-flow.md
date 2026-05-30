# 09. API処理フロー

## 目的

React、ASP.NET Core Web API、SQL Serverへ移行した後の主要API処理フローをMermaidで整理します。実装時はこの流れをベースに、Controller、Application Service、Repository、帳票サービス、ファイル保存の責務を分離します。

バックエンド内部のレイヤ別シーケンスは [10-backend-layer-sequence.md](10-backend-layer-sequence.md) に整理します。

## 共通APIパイプライン

```mermaid
graph TD
    A["React画面"] --> B["API Client"]
    B --> C["ASP.NET Core Middleware"]
    C --> D{"認証済み?"}
    D -- "No" --> E["401 Unauthorized"]
    D -- "Yes" --> F{"CSRF検証が必要?"}
    F -- "必要 / NG" --> G["400 または 403"]
    F -- "不要 または OK" --> H["Controller"]
    H --> I["Request DTO バインド"]
    I --> J{"入力検証OK?"}
    J -- "No" --> K["400 Validation Error"]
    J -- "Yes" --> L["Application Service"]
    L --> M{"権限OK?"}
    M -- "No" --> N["403 Forbidden または 404 Not Found"]
    M -- "Yes" --> O["Repository / Query Service"]
    O --> P["SQL Server"]
    P --> O
    O --> L
    L --> Q["Response DTO"]
    Q --> H
    H --> R["JSON / Blob Response"]
    R --> A
```

## ログイン

```mermaid
sequenceDiagram
    autonumber
    participant U as 利用者
    participant R as React
    participant A as AuthController
    participant S as AuthService
    participant DB as SQL Server

    U->>R: ログインID・パスワード入力
    R->>A: POST /api/auth/login
    A->>S: Authenticate(loginId, password)
    S->>DB: usersをlogin_idで検索
    DB-->>S: User + BCrypt hash
    S->>S: BCrypt検証 / ロール確認
    alt 認証成功
        S-->>A: 認証ユーザー
        A->>A: HttpOnly Cookie発行
        A-->>R: 200 OK + user
        R->>A: GET /api/auth/csrf
        A-->>R: CSRF token
        R-->>U: ダッシュボードへ遷移
    else 認証失敗
        S-->>A: 認証失敗
        A-->>R: 401 Unauthorized
        R-->>U: エラーメッセージ表示
    end
```

## ログインユーザー取得

```mermaid
sequenceDiagram
    autonumber
    participant R as React
    participant A as AuthController
    participant S as AuthService
    participant DB as SQL Server

    R->>A: GET /api/auth/me
    A->>S: GetCurrentUser(userId)
    S->>DB: users + departmentsを取得
    DB-->>S: ユーザー情報
    S-->>A: CurrentUserResponse
    A-->>R: 200 OK
```

## 作業日報登録

```mermaid
sequenceDiagram
    autonumber
    participant R as React
    participant C as WorkReportsController
    participant S as WorkReportService
    participant DB as SQL Server

    R->>C: POST /api/work-reports
    C->>C: CSRF / 認証 / Request DTO検証
    C->>S: Register(request, currentUser)
    S->>S: 業務入力チェック
    S->>S: user_id / department_idをログインユーザーから補完
    S->>DB: INSERT work_reports
    DB-->>S: 登録ID
    S-->>C: 登録結果
    C-->>R: 201 Created
```

## 作業実績検索

```mermaid
sequenceDiagram
    autonumber
    participant R as React
    participant C as WorkReportsController
    participant S as WorkReportService
    participant Q as WorkReportQueryService
    participant DB as SQL Server

    R->>C: GET /api/work-reports?dateFrom=&dateTo=&...
    C->>C: 認証 / Query DTO検証
    C->>S: Search(query, currentUser)
    alt currentUser.role == USER
        S->>S: user_idを自分に強制絞り込み
    else currentUser.role == ADMIN
        S->>S: 全ユーザー検索を許可
    end
    S->>Q: Search(normalizedCondition)
    Q->>DB: SELECT work_reports join users/departments
    DB-->>Q: 検索結果
    Q-->>S: WorkReportSearchResponse
    S-->>C: 検索結果
    C-->>R: 200 OK
```

## 月次報告書出力

```mermaid
sequenceDiagram
    autonumber
    participant R as React
    participant C as MonthlyReportsController
    participant S as MonthlyReportService
    participant H as ReportHistoryService
    participant Q as MonthlyReportQueryService
    participant X as ExcelReportService
    participant F as ReportFileStorage
    participant DB as SQL Server

    R->>C: POST /api/monthly-reports/export
    C->>C: CSRF / 認証 / Request DTO検証
    C->>S: CreateReport(request, currentUser)
    S->>S: 対象年月と対象ユーザーを検証
    alt currentUser.role == USER
        S->>S: 対象ユーザーを自分に固定
    else currentUser.role == ADMIN
        S->>DB: 対象ユーザーが一般ユーザーか確認
        DB-->>S: 対象ユーザー
    end
    S->>H: SaveProcessingHistory()
    H->>DB: INSERT report_output_histories PROCESSING
    DB-->>H: historyId
    S->>Q: FindMonthlyReportData(condition)
    Q->>DB: summary / category / daily detail SELECT
    DB-->>Q: 帳票データ
    Q-->>S: MonthlyReportData
    S->>X: CreateMonthlyReport(data, template)
    X-->>S: xlsx bytes
    S->>F: Save(generated-reports/YYYYMM/file.xlsx)
    F-->>S: savedPath
    S->>H: UpdateSuccessHistory(historyId, fileName, savedPath)
    H->>DB: UPDATE SUCCESS
    S-->>C: MonthlyReportFile
    C-->>R: 200 OK Blob xlsx
```

## 月次報告書出力エラー時

```mermaid
sequenceDiagram
    autonumber
    participant S as MonthlyReportService
    participant H as ReportHistoryService
    participant X as ExcelReportService
    participant F as ReportFileStorage
    participant DB as SQL Server

    S->>H: SaveProcessingHistory()
    H->>DB: INSERT PROCESSING
    DB-->>H: historyId
    S->>X: CreateMonthlyReport()
        X-->>S: 例外
    S->>F: 作成途中ファイルがあれば削除
    S->>H: UpdateErrorHistory(historyId, errorMessage)
    H->>DB: UPDATE ERROR
        S-->>S: 例外をAPI層へ伝播
```

## 帳票履歴検索・詳細

```mermaid
sequenceDiagram
    autonumber
    participant R as React
    participant C as ReportHistoriesController
    participant S as ReportHistoryService
    participant Q as ReportHistoryQueryService
    participant DB as SQL Server

    R->>C: GET /api/report-histories?targetYearMonth=&status=&...
    C->>C: 認証 / Query DTO検証
    C->>S: Search(query, currentUser)
    alt currentUser.role == USER
        S->>S: target_user_idを自分に強制絞り込み
    else currentUser.role == ADMIN
        S->>S: 全履歴検索を許可
    end
    S->>Q: Search(normalizedCondition)
    Q->>DB: SELECT report_output_histories join users
    DB-->>Q: 履歴一覧
    Q-->>S: ReportHistoryListResponse
    S-->>C: 履歴一覧
    C-->>R: 200 OK
```

```mermaid
sequenceDiagram
    autonumber
    participant R as React
    participant C as ReportHistoriesController
    participant S as ReportHistoryService
    participant DB as SQL Server

    R->>C: GET /api/report-histories/{id}
    C->>S: FindById(id, currentUser)
    S->>DB: 履歴詳細を取得
    DB-->>S: ReportHistory
    alt 参照権限なし
        S-->>C: NotFound または Forbidden
        C-->>R: 404 または 403
    else 参照権限あり
        S-->>C: ReportHistoryDetailResponse
        C-->>R: 200 OK
    end
```

## 帳票再ダウンロード

```mermaid
sequenceDiagram
    autonumber
    participant R as React
    participant C as ReportHistoriesController
    participant S as ReportHistoryService
    participant F as ReportFileStorage
    participant DB as SQL Server

    R->>C: GET /api/report-histories/{id}/download
    C->>S: Download(id, currentUser)
    S->>DB: 履歴を取得
    DB-->>S: ReportHistory
    alt 履歴なし / 権限なし
        S-->>C: NotFound または Forbidden
        C-->>R: 404 または 403
    else status != SUCCESS
        S-->>C: Conflict
        C-->>R: 409 Conflict
    else SUCCESS
        S->>F: 保存領域配下の実ファイルか検証
        alt ファイルなし / 保存領域外
            F-->>S: NotFound
            S-->>C: NotFound
            C-->>R: 404 Not Found
        else 取得OK
            F-->>S: xlsx bytes
            S-->>C: file bytes + fileName
            C-->>R: 200 OK Blob xlsx
        end
    end
```

## 実装時の責務境界

| 層 | 責務 |
|---|---|
| React | 画面表示、フォーム状態、API呼び出し、Blob保存、入力補助 |
| Controller | HTTPメソッド、DTOバインド、認証ユーザー取得、ステータスコード |
| Application Service | 業務入力チェック、権限補正、トランザクション、補償処理 |
| Repository / Query Service | SQL Serverアクセス、バインド変数、検索条件、Row mapping |
| ExcelReportService | テンプレート読み込み、セル設定、行追加、書式維持 |
| ReportFileStorage | 保存先解決、ファイル名検証、保存、読み込み、保存領域外アクセス防止 |

## 注意点

- React側の表示制御だけで権限を守らず、API側で必ず `ADMIN` / `USER` の制約を再評価します。
- 月次帳票出力はDB更新とファイル保存をまたぐため、失敗時の `ERROR` 更新と作成途中ファイル削除を明示します。
- 他人の帳票履歴へのアクセスは、情報秘匿を優先する場合 `403` ではなく `404` を返す方針も検討します。
- 同期ダウンロードでタイムアウトが見える場合は、非同期ジョブ化し、`PROCESSING` 履歴をポーリングする方式へ拡張します。
