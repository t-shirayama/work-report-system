# ITテストケース一覧

このページは、React + ASP.NET Core Web API + SQL Server 版で自動化した結合テストのケース一覧です。

## API + SQL Server

`backend/WorkReport.IntegrationTests` は Testcontainers で SQL Server 2022 を起動し、`database/sqlserver/schema.sql` と `seed.sql` を適用してから API を検証します。

| 領域 | ケース | 自動テスト |
|---|---|---|
| DB初期化 | DDLとseedを空DBへ適用できる | `DatabaseInitializationTests.SchemaAndSeed_AreApplied` |
| DB制約 | 不正な日報データをDB制約で拒否する | `DatabaseInitializationTests.Constraints_RejectInvalidWorkReportData` |
| 認証 | 正しいID/パスワードでログインし、`/api/auth/me` でユーザー情報を取得し、ログアウト後は未認証になる | `AuthIntegrationTests.LoginMeLogout_CookieLifecycle_Works` |
| 認証 | 誤ったパスワードは401を返す | `AuthIntegrationTests.Login_ReturnsUnauthorized_WhenPasswordIsInvalid` |
| 認証 | 未認証で保護APIへアクセスすると401を返す | `AuthIntegrationTests.ProtectedApi_ReturnsUnauthorized_WhenNotSignedIn` |
| CSRF | CSRFトークンなしのPOSTは400を返す | `AuthIntegrationTests.UnsafeApi_RejectsRequestWithoutCsrf` |
| CORS | 許可OriginはCORSヘッダーを返し、未許可Originは返さない | `AuthIntegrationTests.Cors_ReturnsHeaderOnlyForAllowedOrigin` |
| 認可 | ADMINは対象ユーザー一覧を取得できる | `AuthorizationIntegrationTests.Admin_CanReadReportTargetUsers` |
| 認可 | USERは対象ユーザー一覧で403になる | `AuthorizationIntegrationTests.User_CannotReadReportTargetUsers` |
| 日報登録 | 一般ユーザーが自分の日報を登録するとDBへ反映される | `WorkReportsIntegrationTests.RegisterWorkReport_WithCsrf_PersistsToDatabase` |
| 日報登録 | 必須/形式/範囲エラーは400を返す | `WorkReportsIntegrationTests.RegisterWorkReport_ReturnsBadRequest_WhenInputIsInvalid` |
| 日報検索 | ADMINはユーザー、年月、日付、分類、キーワードで検索できる | `WorkReportsIntegrationTests.SearchWorkReports_AdminCanUseAllFilters` |
| 日報検索 | USERは自分の日報だけ検索できる | `WorkReportsIntegrationTests.SearchWorkReports_UserCanSeeOnlyOwnReports` |
| 日報検索 | 開始日が終了日より後の検索条件は400を返す | `WorkReportsIntegrationTests.SearchWorkReports_ReturnsBadRequest_WhenDateRangeIsInvalid` |
| ダッシュボード | 月次集計、分類集計、最近の活動を取得できる | `DashboardIntegrationTests.Dashboard_ReturnsAggregatesAndRecentActivities` |
| ダッシュボード | 最近の活動は日付降順で返る | `DashboardIntegrationTests.Dashboard_ReturnsAggregatesAndRecentActivities` |
| 帳票出力 | USERが自分の月次報告書を出力できる | `MonthlyReportsIntegrationTests.User_CanExportOwnReport_FileIsSaved_HistoryIsRegistered_AndCanDownload` |
| 帳票出力 | 出力時にExcelファイルが保存され、履歴が登録され、ダウンロードできる | `MonthlyReportsIntegrationTests.User_CanExportOwnReport_FileIsSaved_HistoryIsRegistered_AndCanDownload` |
| 帳票出力 | Excelに対象年月、対象ユーザー、明細、分類集計が出力される | `MonthlyReportsIntegrationTests.User_CanExportOwnReport_FileIsSaved_HistoryIsRegistered_AndCanDownload` |
| 帳票出力 | ADMINは任意ユーザーの月次報告書を出力できる | `MonthlyReportsIntegrationTests.Admin_CanExportAnyUserReport` |
| 帳票出力 | USERは他ユーザーの月次報告書を出力できない | `MonthlyReportsIntegrationTests.User_CannotExportOtherUsersReport` |
| 帳票出力 | 不正な年月は400を返す | `MonthlyReportsIntegrationTests.Export_ReturnsBadRequest_WhenYearMonthIsInvalid` |
| 帳票出力 | 存在しない対象ユーザーは404を返す | `MonthlyReportsIntegrationTests.Export_ReturnsNotFound_WhenTargetUserDoesNotExist` |
| 帳票出力 | 明細0件の月でも帳票を出力できる | `MonthlyReportsIntegrationTests.Export_Works_WhenMonthHasNoDetails` |
| 帳票DL | 履歴はあるがファイルが存在しない場合は404を返す | `MonthlyReportsIntegrationTests.Download_ReturnsNotFound_WhenHistoryFileDoesNotExist` |
| 履歴検索 | 年月、ステータス、ユーザー条件で履歴検索できる | `ReportHistoriesIntegrationTests.SearchHistories_CanFilterByYearMonthStatusAndTargetUser` |
| 履歴詳細 | 履歴詳細で対象ユーザー、エラー、ファイル情報を取得できる | `ReportHistoriesIntegrationTests.HistoryDetail_ReturnsJoinedUsersAndFilePath` |
| 履歴詳細 | 存在しない履歴詳細は404を返す | `ReportHistoriesIntegrationTests.HistoryDetail_ReturnsNotFound_WhenHistoryDoesNotExist` |

## フロントエンドE2E

`frontend/e2e/work-report-flow.spec.ts` は Playwright でブラウザ操作を検証します。APIが `http://localhost:5000/api` で起動済みで、SQL Serverにseedデータが投入されている前提です。

| 領域 | ケース | 自動テスト |
|---|---|---|
| 一般ユーザー導線 | ログイン、ダッシュボード表示、日報登録、検索結果反映まで通る | `login, open dashboard, register a report, and find it` |
| 管理者導線 | 管理者ログイン、帳票出力、Excelダウンロード、履歴検索まで通る | `admin can export report and open report history` |
| エラー表示 | ログイン失敗を画面表示する | `login error and API validation error are shown` |
| エラー表示 | 日報登録APIの入力エラーを画面表示する | `login error and API validation error are shown` |

## 手動確認に残すケース

以下は環境依存または破壊的になりやすいため、自動ITではなく手動確認対象です。

| 領域 | ケース | 理由 |
|---|---|---|
| SQL Server停止 | API起動中にSQL Serverを停止した場合のログ、500応答、復旧手順 | Testcontainersの通常ITではDB停止を都度再現しないため |
| 帳票保存先異常 | 保存先ディレクトリの権限不足、ディスク容量不足 | ローカル/CI環境への副作用が大きいため |
| 実ブラウザCORS | ブラウザから未許可Originでアクセスしたときの遮断 | APIレベルのCORSヘッダー検証は自動化済みだが、ブラウザ実装差は環境依存のため |
| 性能 | 大量日報、複数ユーザー、長期間検索、帳票大量出力 | 機能ITとは別に性能テストとして閾値を定める必要があるため |
