# Controller / Service / DAO 構成

このドキュメントでは、簡易ログイン機能を例にして、Controller / Service / DAO の役割分担を説明します。

本プロジェクトでは、Spring Boot、Spring Security、JPA、MyBatis、Hibernateは使用しません。Spring MVCとSpring JDBCを使い、SQLはDAO層に明示的に記述します。

## 今回の対象ファイル

| ファイル | 役割 |
|---|---|
| `LoginController.java` | ログイン画面表示、ログイン実行、ダッシュボード表示、ログアウト |
| `LoginForm.java` | ログインIDとパスワードの入力値を保持 |
| `UserService.java` | 認証処理の業務判断 |
| `UserDao.java` | `users` テーブルからログインIDでユーザーを検索 |
| `User.java` | DBから取得したユーザー情報 |
| `login.jsp` | ログイン画面 |
| `dashboard.jsp` | ログイン後のダッシュボード |

## Controllerの役割

`LoginController` はHTTPリクエストを受け取り、画面遷移とセッション管理を担当します。

主なURLは以下です。

| URL | メソッド | 処理 |
|---|---|---|
| `/login` | GET | ログイン画面を表示 |
| `/login` | POST | ログインIDとパスワードで認証 |
| `/dashboard` | GET | ログイン済みユーザー向けダッシュボードを表示 |
| `/logout` | GET / POST | セッションを破棄してログアウト |

ControllerにはSQLを書きません。認証処理は `UserService` に委譲します。

## Serviceの役割

`UserService` はログイン認証の業務判断を担当します。

今回の簡易版では以下を行います。

1. ログインIDとパスワードが空でないか確認する
2. `UserDao` でログインIDに一致するユーザーを検索する
3. 入力されたパスワードとDB上のパスワードを比較する
4. 認証成功時は画面表示用にパスワードを `null` にして返す
5. 認証失敗時は `null` を返す

ポートフォリオ簡易版のため、現在は平文パスワード比較です。本番では必ずハッシュ化したパスワードを保存し、ハッシュ照合を行います。

## DAOの役割

`UserDao` はDBアクセスを担当します。

`NamedParameterJdbcTemplate` を使用し、ログインIDをバインド変数として渡します。

```java
MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("loginId", loginId);
```

SQLでは `:loginId` を使用しているため、ユーザー入力をSQL文字列へ直接連結しません。

## 処理の流れ

ログイン成功時の流れは以下です。

1. ブラウザから `POST /login` を送信する
2. `LoginController#login` が `LoginForm` として入力値を受け取る
3. Controllerが `UserService#authenticate` を呼び出す
4. Serviceが `UserDao#findByLoginId` を呼び出す
5. DAOが `NamedParameterJdbcTemplate` で `users` と `departments` を検索する
6. Serviceがパスワードを比較する
7. Controllerが認証済みユーザーをHTTPセッションに保存する
8. `/dashboard` へリダイレクトする
9. `dashboard.jsp` にログインユーザー情報を表示する

ログイン失敗時は、`login.jsp` に戻り、エラーメッセージを表示します。

## セッション管理

ログイン成功時は、以下のキーで `User` をセッションに保存します。

```text
loginUser
```

`/dashboard` ではセッションに `loginUser` が存在するか確認し、存在しない場合は `/login` へリダイレクトします。

ログアウト時は `session.invalidate()` によりセッションを破棄します。
