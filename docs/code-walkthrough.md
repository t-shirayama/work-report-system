# コード walkthrough

このドキュメントでは、実装済み機能のコードを画面操作の流れに沿って説明します。

面談前の復習では、最初にREADMEで全体像を確認し、最後にこのドキュメントで実装の流れを通し読みすると理解しやすくなります。

## 読み方

各機能は、おおむね以下の順番で処理されます。

```text
JSPのフォーム
  ↓
Controller
  ↓
Service
  ↓
DAO
  ↓
SQL
  ↓
DTO / Entity
  ↓
JSP表示 または Excel出力
```

コードを読むときは、URLを受けるControllerから入り、Serviceで業務判断を確認し、DAOで実際のSQLを見る流れがおすすめです。

## 画面からログインする

利用者は以下のURLにアクセスします。

```text
http://localhost:8080/work-report-system/login
```

`GET /login` は `LoginController#showLoginForm` が受け取り、空の `LoginForm` を `Model` に設定して `login.jsp` を表示します。

## login.jsp

`login.jsp` はJSTLを使用してCSSパスやリンクURLを生成します。

```jsp
<link rel="stylesheet" href="<c:url value='/resources/css/common.css' />">
```

ログインフォームは `POST /login` に送信されます。

```jsp
<form class="form" method="post" action="<c:url value='/login' />">
```

エラーがある場合は `errorMessage` を表示します。

## LoginController

`POST /login` は `LoginController#login` が受け取ります。

Controllerは入力値を `LoginForm` として受け取り、認証処理を `UserService` に依頼します。

```java
User user = userService.authenticate(loginForm.getLoginId(), loginForm.getPassword());
```

認証に失敗した場合は、エラーメッセージを設定して `login.jsp` を再表示します。

認証に成功した場合は、ログインユーザーをセッションに保存します。

```java
session.setAttribute("loginUser", user);
```

その後、`/dashboard` へリダイレクトします。

## UserService

`UserService#authenticate` は、ログインIDとパスワードの簡易認証を行います。

処理は以下です。

1. ログインIDとパスワードが入力されているか確認する
2. `UserDao#findByLoginId` でユーザーを検索する
3. 入力パスワードとDB上のパスワードを比較する
4. 成功時は `User` を返し、失敗時は `null` を返す

現在はポートフォリオ簡易版のため平文比較です。本番ではパスワードハッシュ化が必須です。

## UserDao

`UserDao` はSpring JDBCの `NamedParameterJdbcTemplate` を使用します。

SQLはDAO内に明示的に記述し、ログインIDは `:loginId` としてバインドします。

```java
return namedParameterJdbcTemplate.queryForObject(SELECT_BY_LOGIN_ID, params, new UserRowMapper());
```

検索結果が0件の場合は `EmptyResultDataAccessException` を捕捉し、`null` を返します。

## dashboard.jsp

`GET /dashboard` では、セッションに `loginUser` があるか確認します。

ログイン済みの場合は `DashboardController` が `DashboardService` を呼び出し、DB集計結果を `dashboard.jsp` に表示します。

未ログインの場合は `/login` にリダイレクトします。

ダッシュボードでは、`work_reports` と `report_output_histories` から以下を取得します。

- 本日の作業日報登録件数
- 今月の総作業時間
- 未出力の月次報告件数
- 最近の活動一覧

最近の活動は、作業日報登録と帳票出力履歴を時系列でまとめた表示です。

## logout

`GET /logout` または `POST /logout` でログアウトできます。

ログアウト時はHTTPセッションを破棄します。

```java
session.invalidate();
```

その後、`/login` にリダイレクトします。

## 作業日報登録画面を表示する

ログイン後、以下のURLへアクセスします。

```text
http://localhost:8080/work-report-system/work-reports/new
```

`GET /work-reports/new` は `WorkReportController#showForm` が受け取ります。

Controllerはセッションから `loginUser` を取得し、未ログインの場合は `/login` へリダイレクトします。ログイン済みの場合は、空の `WorkReportForm` を `Model` に設定して `work-report-form.jsp` を表示します。

## work-report-form.jsp

`work-report-form.jsp` は、作業日報の入力画面です。

入力項目は以下です。

- 作業日
- プロジェクト名
- 作業分類
- 作業時間
- 作業内容

社員名と部署は、ログイン中ユーザーの情報を表示します。入力画面ではSpringのフォームタグを使用し、入力値を `WorkReportForm` にバインドします。

## WorkReportController

`POST /work-reports` は `WorkReportController#register` が受け取ります。

処理は以下です。

1. セッションからログイン中ユーザーを取得する
2. `WorkReportService#validate` で入力チェックを行う
3. エラーがある場合は `work-report-form.jsp` を再表示する
4. エラーがない場合は `WorkReportService#register` を呼び出す
5. 登録完了後、`/work-reports/complete` へリダイレクトする

## WorkReportService

`WorkReportService` は入力チェックと登録データの組み立てを担当します。

作業日や作業時間は文字列としてFormで受け取り、Serviceで日付や数値に変換します。

ログイン中ユーザーの `userId` と `departmentId` を使って、`work_reports` に登録する `WorkReport` を作成します。

## WorkReportDao

`WorkReportDao` は `NamedParameterJdbcTemplate` を使用してINSERTを実行します。

SQLではOracleのシーケンス `seq_work_reports.NEXTVAL` を使用して主キーを採番します。

```sql
INSERT INTO work_reports (
    work_report_id,
    user_id,
    department_id,
    work_date,
    project_name,
    work_category,
    work_hours,
    work_content,
    created_at,
    updated_at
) VALUES (
    seq_work_reports.NEXTVAL,
    :userId,
    :departmentId,
    :workDate,
    :projectName,
    :workCategory,
    :workHours,
    :workContent,
    SYSDATE,
    SYSDATE
)
```

入力値はバインド変数として渡すため、SQL文字列へ直接連結しません。

## 登録完了画面

登録後は `GET /work-reports/complete` にリダイレクトし、`work-report-complete.jsp` を表示します。

完了画面では登録した作業日、プロジェクト名、作業分類、作業時間を表示します。

## 作業実績検索画面を表示する

ログイン後、以下のURLへアクセスします。

```text
http://localhost:8080/work-report-system/work-reports/search
```

`GET /work-reports/search` は `WorkReportController#showSearchForm` が受け取ります。

Controllerはセッションから `loginUser` を取得し、未ログインの場合は `/login` へリダイレクトします。ログイン済みの場合は、空の `WorkReportSearchForm` と検索結果を `Model` に設定して `work-report-search.jsp` を表示します。

## work-report-search.jsp

`work-report-search.jsp` は検索条件エリアと検索結果テーブルを表示します。

検索条件は以下です。

- 対象期間 From
- 対象期間 To
- 社員
- 部署
- 作業分類
- プロジェクト名

検索結果は以下を表示します。

- 日付
- 社員名
- 部署
- プロジェクト名
- 作業分類
- 作業時間
- 作業内容

## 検索処理

`POST /work-reports/search` は `WorkReportController#search` が受け取ります。

処理は以下です。

1. セッションからログイン中ユーザーを取得する
2. `WorkReportService#validateSearch` で日付範囲と作業分類を確認する
3. エラーがある場合は検索画面を再表示する
4. エラーがない場合は `WorkReportService#search` を呼び出す
5. Serviceが `WorkReportDao#search` を呼び出す
6. DAOが指定された条件だけWHERE句に追加し、検索結果をDTOへ変換する
7. JSPが検索結果件数と一覧テーブルを表示する

## 検索SQL

検索SQLは `work_reports`、`users`、`departments` をJOINします。

```sql
FROM work_reports wr
INNER JOIN users u
    ON wr.user_id = u.user_id
INNER JOIN departments d
    ON wr.department_id = d.department_id
WHERE 1 = 1
```

条件が指定された場合のみ、以下のようなWHERE句を追加します。

```sql
AND wr.work_date >= :dateFrom
AND u.employee_name LIKE :employeeName
AND wr.work_category = :workCategory
```

検索条件はすべて `NamedParameterJdbcTemplate` のバインド変数として渡します。ユーザー入力をSQL文字列へ直接連結しません。

## 月次報告書Excel出力

ログイン後、以下のURLへアクセスします。

```text
http://localhost:8080/work-report-system/monthly-reports/new
```

`GET /monthly-reports/new` は `MonthlyReportController#showForm` が受け取り、月次報告書出力画面を表示します。

入力条件は以下です。

- 対象年
- 対象月
- 部署
- 社員

`POST /monthly-reports/export` では、Controllerが `MonthlyReportService#createReport` を呼び出します。

処理の流れは以下です。

1. `MonthlyReportService` が入力チェックを行う
2. 対象年・対象月から月初日と月末日を計算する
3. `MonthlyReportDao` が月次サマリー、作業分類別集計、日別作業実績を検索する
4. Serviceが総作業時間、稼働日数、平均時間、割合を整理する
5. `ExcelReportService` がテンプレートExcelを読み込む
6. `Workbook`、`Sheet`、`Row`、`Cell` を使って値を差し込む
7. 明細行はテンプレート行の `CellStyle` をコピーしながら動的に追加する
8. ControllerがExcel用のレスポンスヘッダーを設定し、ブラウザへ返す
9. 生成したExcelを `generated-reports/` 配下へ保存する
10. `report_output_histories` に帳票作成履歴を登録する

Excelテンプレートは以下です。

```text
src/main/resources/templates/monthly-report-template.xlsx
```

ブラウザからは、以下のようなファイル名でダウンロードされます。

```text
月次報告書_202605_山田太郎.xlsx
```

## 帳票作成履歴

帳票作成履歴一覧は以下のURLで表示します。

```text
http://localhost:8080/work-report-system/report-histories
```

`GET /report-histories` は `ReportHistoryController#list` が受け取り、検索条件を `ReportHistorySearchForm` として受け取ります。

`ReportHistoryDao` は `report_output_histories` と `users` をJOINし、対象年月、帳票種別、作成者、ステータスが指定された場合だけWHERE句に追加します。

一覧画面では、ステータスが `SUCCESS` の履歴だけにダウンロードボタンを表示します。

履歴詳細は以下のURLで表示します。

```text
GET /report-histories/{id}
```

詳細画面では、出力日時、対象年月、帳票種別、作成者、ステータス、ファイル名、ファイルパス、エラーメッセージを確認できます。`SUCCESS` の場合は詳細画面にもダウンロードボタンを表示します。

## 帳票の再ダウンロード

再ダウンロードは以下のURLで行います。

```text
GET /report-histories/{id}/download
```

処理の流れは以下です。

1. `ReportHistoryController#download` が履歴IDを受け取る
2. `ReportHistoryService#findById` で履歴を取得する
3. ステータスが `SUCCESS` であることを確認する
4. `file_path` のExcelファイルを `generated-reports/` から読み込む
5. Excel用の `Content-Type` と `Content-Disposition` を設定する
6. ブラウザへExcelファイルを返す

ファイルが見つからない場合は、一覧画面にエラーメッセージを表示します。

## 現時点で実装していないこと

- Spring Securityによる認証・認可
- パスワードハッシュ化
- 入力チェックの詳細化
- DB接続エラー時の専用エラー画面
- 権限別メニュー制御
- 作業日報の一覧、編集、削除
- 作業実績検索のページング
- 帳票作成履歴の検索条件
- 帳票ファイルの削除運用

## 面談前の説明ポイント

このプロジェクトを説明するときは、以下の順番で話すと伝わりやすくなります。

1. Spring Bootではなく、Spring MVC、JSP、Tomcat、Maven WARで構成している
2. Controller / Service / DAOを分け、SQLはDAOに明示的に書いている
3. DBアクセスには `NamedParameterJdbcTemplate` を使い、バインド変数でSQLインジェクション対策をしている
4. JSPはModelに入ったデータを表示し、業務ロジックは持たせていない
5. 月次報告書はApache POI 3.17でExcelテンプレートに値を差し込む方式にしている
6. 帳票出力履歴を保存し、作成済みファイルを再ダウンロードできるようにしている
7. 現在のログインはポートフォリオ簡易版であり、本番ではパスワードハッシュ化や権限制御が必要である

## 用語の補足

| 用語 | 簡単な説明 |
|---|---|
| `DispatcherServlet` | Spring MVCの入口。URLに対応するControllerへ処理を振り分ける |
| `Model` | ControllerからJSPへ値を渡す入れ物 |
| `DTO` | 画面表示や帳票出力に必要な形へ整えたデータ |
| `DAO` | DBアクセスを担当するクラス。SQLを持つ |
| バインド変数 | SQLに値を安全に渡す仕組み。`:loginId` のように名前で指定する |
| `Workbook` | Apache POIで扱うExcelファイル全体 |
| `Sheet` | Excelのシート |
| `CellStyle` | セルの罫線、色、表示形式などの書式 |
