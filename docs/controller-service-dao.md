# Controller / Service / DAO 構成

このドキュメントでは、簡易ログイン機能を例にして、Controller / Service / DAO の役割分担を説明します。

本プロジェクトでは、Spring Boot、Spring Security、JPA、MyBatis、Hibernateは使用しません。Spring MVCとSpring JDBCを使い、SQLはDAO層に明示的に記述します。

## 全体像

Controller / Service / DAO は、業務システムでよく使われる責務分担です。

```text
ブラウザ
  ↓
Controller: URLを受け取り、Form、Model、Session、画面遷移を扱う
  ↓
Service: 入力チェック、業務判断、登録・検索・帳票作成の流れを組み立てる
  ↓
DAO: SQLを実行し、Oracle Databaseとデータをやり取りする
  ↓
Oracle Database
```

この分け方により、画面遷移の変更、業務ルールの変更、SQLの変更をそれぞれ追いやすくしています。

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

Controllerが担当するのはHTTPに近い処理です。具体的には、URL、GET/POST、入力Form、Modelへの表示データ設定、セッション確認、リダイレクトなどです。

## Serviceの役割

`UserService` はログイン認証の業務判断を担当します。

今回の簡易版では以下を行います。

1. ログインIDとパスワードが空でないか確認する
2. `UserDao` でログインIDに一致するユーザーを検索する
3. 入力されたパスワードとDB上のパスワードを比較する
4. 認証成功時は画面表示用にパスワードを `null` にして返す
5. 認証失敗時は `null` を返す

ポートフォリオ簡易版のため、現在は平文パスワード比較です。本番では必ずハッシュ化したパスワードを保存し、ハッシュ照合を行います。

Serviceが担当するのは業務ルールです。作業日報登録では「作業時間は0より大きいこと」、月次報告書では「対象年・対象月から月初日と月末日を計算すること」などがServiceの責務です。

## DAOの役割

`UserDao` はDBアクセスを担当します。

`NamedParameterJdbcTemplate` を使用し、ログインIDをバインド変数として渡します。

```java
MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("loginId", loginId);
```

SQLでは `:loginId` を使用しているため、ユーザー入力をSQL文字列へ直接連結しません。

DAOが担当するのはDBに近い処理です。SQL文、バインド変数、`RowMapper` によるDTO/Entity変換、検索結果0件時の扱いなどをDAO内に閉じ込めます。

## DTOとEntityの使い分け

このプロジェクトでは、DB上の1行に近いデータはEntity、画面表示や帳票出力のために整えたデータはDTOとして扱います。

| 種類 | 例 | 使いどころ |
|---|---|---|
| Entity | `User`, `WorkReport`, `ReportOutputHistory` | DBテーブルのデータを表す |
| DTO | `WorkReportSearchResultDto`, `MonthlyReportSummaryDto`, `ReportHistoryDto` | 検索結果、集計結果、画面表示、帳票出力用データを表す |

たとえば作業実績検索では、`work_reports`、`users`、`departments` をJOINします。検索結果には社員名や部署名も表示するため、テーブル単体のEntityではなく `WorkReportSearchResultDto` に詰め替えています。

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

## 作業日報登録機能の責務分担

作業日報登録では、以下のファイルを追加しています。

| ファイル | 役割 |
|---|---|
| `WorkReportController.java` | 登録画面表示、登録実行、完了画面表示 |
| `WorkReportForm.java` | 作業日、プロジェクト名、作業分類、作業時間、作業内容の入力値を保持 |
| `WorkReportService.java` | 入力チェック、FormからEntityへの変換、登録処理の制御 |
| `WorkReportDao.java` | `work_reports` テーブルへのINSERT |
| `WorkReport.java` | 登録する作業日報データ |
| `work-report-form.jsp` | 作業日報登録画面 |
| `work-report-complete.jsp` | 登録完了画面 |

### Controller

`WorkReportController` は、ログイン中ユーザーをセッションから取得します。

未ログインの場合は `/login` へリダイレクトします。ログイン済みの場合のみ、登録画面表示、登録処理、完了画面表示を行います。

| URL | メソッド | 処理 |
|---|---|---|
| `/work-reports/new` | GET | 作業日報登録画面を表示 |
| `/work-reports` | POST | 入力チェック後、作業日報を登録 |
| `/work-reports/complete` | GET | 登録完了画面を表示 |

Controllerは入力値を受け取り、入力チェックと登録処理を `WorkReportService` に委譲します。

### Service

`WorkReportService` は、入力チェックと登録データの組み立てを担当します。

主な入力チェックは以下です。

- 作業日は必須
- プロジェクト名は必須
- 作業分類は必須
- 作業時間は必須
- 作業時間は0より大きい数値
- 作業内容は必須

登録時は、ログイン中ユーザーの `userId` と `departmentId` を使用し、`WorkReport` を作成してDAOへ渡します。

### DAO

`WorkReportDao` は、Spring JDBCの `NamedParameterJdbcTemplate` を使用して `work_reports` にINSERTします。

主キーはOracle向けDDLで定義した `seq_work_reports.NEXTVAL` を使用します。

SQLはDAOに明示的に記述し、入力値は `BeanPropertySqlParameterSource` によりバインド変数として渡します。

## 例外処理の考え方

例外処理は、利用者に見せるエラーと、開発者・運用者が調査するエラーを分けて考えます。

入力チェックエラーは、Serviceで検出してControllerへ返し、JSPに分かりやすいメッセージを表示します。たとえば作業日報登録では、作業日未入力や作業時間が0以下の場合に登録画面を再表示します。

DB検索で0件になるケースは、必ずしもシステムエラーではありません。`UserDao#findByLoginId` では、ログインIDが見つからない場合に `null` を返し、Serviceが認証失敗として扱います。

一方、DB接続エラー、Excel作成エラー、ファイル保存エラーはシステム側の問題として扱います。月次報告書出力では、失敗時に `report_output_histories` へ `ERROR` ステータスとエラーメッセージを保存する方針です。

## ログ出力の考え方

現在の実装は学習用の最小構成ですが、実案件ではController、Service、DAOの主要な処理開始・終了、例外発生時にログを出力します。

ログに残すとよい情報は、処理名、ログインユーザーID、対象年月、履歴ID、例外内容などです。ただし、パスワードや個人情報を過度に出力しないように注意します。
