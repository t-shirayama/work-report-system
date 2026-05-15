# コード walkthrough

このドキュメントでは、実装済み機能のコードを画面操作の流れに沿って整理します。

機能ごとの処理経路を確認するときは、最初にREADMEで全体像を確認し、次にこのドキュメントでController、Service、DAO、JSPの流れを追ってください。

## 読み方

各機能は、おおむね以下の順番で処理されます。

```text
JSPのフォーム
  ↓
Spring Security / Controller
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

ログインフォームは `POST /login` に送信されます。CSRF対策として、Spring Securityが生成したトークンをhidden項目として送信します。

```jsp
<form class="form" method="post" action="<c:url value='/login' />">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
```

エラーがある場合は `errorMessage` を表示します。

## LoginController

`LoginController` は `GET /login` の画面表示を担当します。

`POST /login` は `web.xml` に登録された `springSecurityFilterChain` が処理します。認証に失敗した場合は `/login?error=true` に戻り、`LoginController` がエラーメッセージをModelへ設定します。

## Spring Security

`security-context.xml` では、ログイン画面、静的リソース、認証が必要なURL、ログアウト、パスワード照合方式を定義しています。
CSRF対策は有効にし、ログアウトや登録系POSTフォームにはCSRFトークンを含めます。セッション固定攻撃対策として、ログイン成功時にセッションを移行する設定も明示しています。remember-meは現時点では使用しません。
同時ログイン数の制御は現時点では強制していません。運用環境で端末制限や二重ログイン制限が必要になった場合は、Spring Securityのセッション管理設定と `HttpSessionEventPublisher` の追加を検討します。

認証時は `WorkReportUserDetailsService#loadUserByUsername` が呼び出され、`UserDao#findByLoginId` で `users` と `departments` を検索します。取得した `users.password` はBCryptハッシュであり、Spring Securityの `BCryptPasswordEncoder` が入力パスワードと照合します。

認証に成功すると `LoginSuccessHandler` が呼び出されます。ここで画面表示補助用のログインユーザーとログイン時刻をセッションに保存し、`/dashboard` へリダイレクトします。認証状態の正はSpring SecurityのSecurityContextであり、セッション上のUserはJSP表示や既存Controllerとの受け渡しを補助するための情報です。

処理は以下です。

1. `POST /login` をSpring Securityが受け取る
2. `WorkReportUserDetailsService` がログインIDでユーザーを検索する
3. `BCryptPasswordEncoder` が入力パスワードを検証する
4. `LoginSuccessHandler` がセッションへログインユーザー情報を保存する
5. `/dashboard` へリダイレクトする

## UserDao

`UserDao` はSpring JDBCの `NamedParameterJdbcTemplate` を使用します。

SQLはDAO内に明示的に記述し、ログインIDは `:loginId` としてバインドします。

```java
return namedParameterJdbcTemplate.queryForObject(SELECT_BY_LOGIN_ID, params, new UserRowMapper());
```

検索結果が0件の場合は `EmptyResultDataAccessException` を捕捉し、`null` を返します。

## dashboard.jsp

`GET /dashboard` では、Spring Securityにより未認証アクセスがログイン画面へ誘導されます。Controller側では `SessionUtils` からログインユーザーを取得します。

ログイン済みの場合は `DashboardController` が `DashboardService` を呼び出し、DB集計結果を `dashboard.jsp` に表示します。

未ログインの場合は `/login` にリダイレクトします。

ダッシュボードでは、`work_reports` と `report_output_histories` から以下を取得します。

- 本日の作業日報登録件数
- 今月の総作業時間
- 未出力の月次報告件数
- 最近の活動一覧

最近の活動は、作業日報登録と帳票出力履歴を時系列でまとめた表示です。

処理の流れは以下です。

1. `DashboardController#dashboard` がセッションからログインユーザーを取得する
2. 未ログインの場合は `/login` へリダイレクトする
3. ログイン済みの場合は `DashboardService#getDashboard` を呼び出す
4. `DashboardService` が `DashboardDao` の集計メソッドを呼び出す
5. `DashboardDto` と最近の活動一覧をModelへ設定する
6. `dashboard.jsp` が集計カードと活動一覧を表示する

## logout

`POST /logout` でログアウトできます。ログアウトフォームにはCSRFトークンを含めます。

ログアウト時はSpring SecurityがHTTPセッションを破棄します。

```jsp
<form method="post" action="<c:url value='/logout' />">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
</form>
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
4. エラーがない場合は `WorkReportService#search` に検索条件とログインユーザーを渡す
5. Serviceが `WorkReportDao#search` を呼び出す
6. 一般ユーザーの場合はServiceがログインユーザーIDをDAOへ渡し、DAOが `wr.user_id = :userId` を必ず追加する
7. DAOが指定された条件だけWHERE句に追加し、検索結果をDTOへ変換する
8. JSPが検索結果件数と一覧テーブルを表示する

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
AND wr.user_id = :userId
AND u.employee_name LIKE :employeeName
AND wr.work_category = :workCategory
```

検索条件はすべて `NamedParameterJdbcTemplate` のバインド変数として渡します。ユーザー入力をSQL文字列へ直接連結しません。`ADMIN` は全ユーザー分を検索できますが、`USER` は画面上の条件を改変しても自分の作業実績だけが検索対象になります。

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
8. ファイル名に使う社員名を安全な文字へ変換する
9. `report_output_histories` に `_PROCESSING.xlsx` の仮ファイル名で `PROCESSING` の帳票作成履歴を登録する
10. 採番された履歴IDを含む正式ファイル名を作成する
11. 生成したExcelを `generated-reports/` 配下へ保存する
12. 成功時は同じ履歴を `SUCCESS` と正式ファイル名へ更新する
13. ControllerがExcel用のレスポンスヘッダーを設定し、ブラウザへ返す

一般ユーザーは、自分の月次報告書のみ出力できます。管理者は一般ユーザーを選択して月次報告書を出力できます。集計条件には部署名・社員名ではなく `user_id` を使用します。

Excel生成やファイル保存で失敗した場合は、同じ履歴を `ERROR` へ更新し、エラーメッセージを保存します。ファイル保存後に失敗した場合は生成済みファイルの削除を試み、削除に失敗した場合はログに警告を残します。

Excelテンプレートは以下です。

```text
src/main/resources/templates/monthly-report-template.xlsx
```

ブラウザからは、以下のようなファイル名でダウンロードされます。

```text
月次報告書_202605_山田太郎_履歴ID100.xlsx
```

## 帳票作成履歴

帳票作成履歴一覧は以下のURLで表示します。

```text
http://localhost:8080/work-report-system/report-histories
```

`GET /report-histories` は `ReportHistoryController#list` が受け取り、検索条件を `ReportHistorySearchForm` として受け取ります。

`ReportHistoryDao` は `report_output_histories` と `users` をJOINし、対象年月、帳票種別、作成者、ステータスが指定された場合だけWHERE句に追加します。

`ADMIN` は全ユーザー分を検索できます。`USER` は自分が作成した履歴だけを検索できます。

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
4. `file_path` が `generated-reports/` 配下であることを確認する
5. Excelファイルを読み込む
6. Excel用の `Content-Type` と `Content-Disposition` を設定する
7. ブラウザへExcelファイルを返す

ファイルが見つからない場合は、一覧画面にエラーメッセージを表示します。

## 今後の拡張候補

主要機能は実装済みです。以下は、継続運用に向けた拡張候補です。

- DB接続エラー時の専用エラー画面
- 管理者向けメニューと権限別表示制御
- 作業日報の一覧、詳細、編集、削除
- 作業実績検索と帳票作成履歴のページング
- 古い帳票ファイルの削除運用
- アカウントロック、パスワード変更、パスワード有効期限管理

## 設計上の説明ポイント

このプロジェクトの設計を確認するときは、以下の観点で整理すると全体像を把握しやすくなります。

1. Spring Bootではなく、Spring MVC、JSP、Tomcat、Maven WARで構成している
2. Controller / Service / DAOを分け、SQLはDAOに明示的に書いている
3. DBアクセスには `NamedParameterJdbcTemplate` を使い、バインド変数でSQLインジェクション対策をしている
4. JSPはModelに入ったデータを表示し、業務ロジックは持たせていない
5. 月次報告書はApache POI 3.17でExcelテンプレートに値を差し込む方式にしている
6. 帳票出力履歴を保存し、作成済みファイルを再ダウンロードできるようにしている
7. ログイン認証はSpring Securityで行い、パスワードはBCryptハッシュとして保存している

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
