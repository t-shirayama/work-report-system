# コード walkthrough

このドキュメントでは、簡易ログイン機能のコードを画面操作の流れに沿って説明します。

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

## 現時点で実装していないこと

- Spring Securityによる認証・認可
- パスワードハッシュ化
- 入力チェックの詳細化
- DB接続エラー時の専用エラー画面
- 権限別メニュー制御
- 作業日報登録などの業務機能
