# コード walkthrough

このドキュメントでは、簡易ログイン機能と作業日報登録機能のコードを画面操作の流れに沿って説明します。

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

ログイン済みの場合は `dashboard.jsp` にユーザー情報を表示します。

未ログインの場合は `/login` にリダイレクトします。

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

## 現時点で実装していないこと

- Spring Securityによる認証・認可
- パスワードハッシュ化
- 入力チェックの詳細化
- DB接続エラー時の専用エラー画面
- 権限別メニュー制御
- 作業日報の一覧、編集、削除
- 作業実績検索のページング
- 月次報告書出力
