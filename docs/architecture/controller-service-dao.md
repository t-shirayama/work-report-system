# Controller / Service / DAO 構成

このドキュメントでは、実装済み機能を例にして、Controller / Service / DAO の役割分担を説明します。

本プロジェクトでは、Spring Boot、JPA、MyBatis、Hibernateは使用しません。認証はSpring Security、DBアクセスはSpring JDBCを使い、SQLはDAO層に明示的に記述します。

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

## 主な対象ファイル

| ファイル | 役割 |
|---|---|
| `LoginController.java` | ログイン画面表示、ログイン実行、ダッシュボード表示、ログアウト |
| `DashboardController.java` | DB集計結果を使ったダッシュボード表示 |
| `WorkReportController.java` | 作業日報登録、作業実績検索 |
| `MonthlyReportController.java` | 月次報告書Excel出力 |
| `ReportHistoryController.java` | 帳票作成履歴検索、詳細、再ダウンロード |
| `WorkReportUserDetailsService.java` | Spring Securityから呼び出され、ログインIDでユーザー情報を取得 |
| `LoginSuccessHandler.java` | ログイン成功時のセッション情報設定とダッシュボード遷移 |
| `LoginForm.java` | ログインIDとパスワードの入力値を保持 |
| `WorkReportForm.java` | 作業日報登録の入力値を保持 |
| `WorkReportSearchForm.java` | 作業実績検索条件を保持 |
| `MonthlyReportForm.java` | 月次報告書出力条件を保持 |
| `ReportHistorySearchForm.java` | 帳票作成履歴検索条件を保持 |
| `UserService.java` | ユーザー関連処理の補助 |
| `DashboardService.java` | ダッシュボード集計結果の組み立て |
| `WorkReportService.java` | 作業日報登録、検索条件チェック |
| `MonthlyReportService.java` | 月次帳票データ作成と出力処理の制御 |
| `ReportHistoryService.java` | 帳票作成履歴登録、検索、ファイル読込確認 |
| `UserDao.java` | `users` テーブルからログインIDでユーザーを検索 |
| `DashboardDao.java` | ダッシュボード用の集計SQLを実行 |
| `WorkReportDao.java` | `work_reports` へのINSERTと検索SQLを実行 |
| `MonthlyReportDao.java` | 月次報告書用の集計SQLを実行 |
| `ReportHistoryDao.java` | `report_output_histories` のINSERT、検索、詳細取得 |
| `User.java` | DBから取得したユーザー情報 |
| `login.jsp` | ログイン画面 |
| `dashboard.jsp` | ログイン後のダッシュボード |

## Controllerの役割

`LoginController` はログイン画面の表示を担当します。ログインPOST、パスワード照合、ログアウトはSpring Securityが担当します。

主なURLは以下です。

| URL | メソッド | 処理 |
|---|---|---|
| `/login` | GET | ログイン画面を表示 |
| `/login` | POST | Spring SecurityがログインIDとパスワードで認証 |
| `/dashboard` | GET | ログイン済みユーザー向けダッシュボードを表示 |
| `/logout` | POST | Spring Securityがセッションを破棄してログアウト |

ControllerにはSQLを書きません。認証処理は `WorkReportUserDetailsService` と `UserDao` をSpring Securityから呼び出す形にしています。

Controllerが担当するのはHTTPに近い処理です。具体的には、URL、GET/POST、入力Form、Modelへの表示データ設定、セッション確認、リダイレクトなどです。

## Serviceの役割

認証では `WorkReportUserDetailsService` がログインIDをもとに `UserDao` を呼び出し、`users.password` に保存されたBCryptハッシュをSpring Securityが照合します。認証成功後は `LoginSuccessHandler` が画面表示用ユーザー情報とログイン時刻をHTTPセッションに保存します。

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
2. `springSecurityFilterChain` がログインリクエストを処理する
3. `WorkReportUserDetailsService` が `UserDao#findByLoginId` を呼び出す
4. DAOが `NamedParameterJdbcTemplate` で `users` と `departments` を検索する
5. Spring Securityが入力パスワードとDB上のBCryptハッシュを照合する
6. `LoginSuccessHandler` が認証済みユーザーをHTTPセッションに保存する
7. `/dashboard` へリダイレクトする
8. `DashboardController` がDB集計結果を取得し、`dashboard.jsp` にログインユーザー情報とダッシュボード情報を表示する

ログイン失敗時は、`login.jsp` に戻り、エラーメッセージを表示します。

## セッション管理

ログイン成功時は、Spring Securityの認証情報に加え、画面表示用として以下のキーで `User` をセッションに保存します。

```text
loginUser
```

`/dashboard` ではセッションに `loginUser` が存在するか確認し、存在しない場合は `/login` へリダイレクトします。

ログアウト時はSpring Securityのログアウト処理によりセッションを破棄します。POSTフォームにはCSRFトークンを含めます。

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

## ダッシュボード機能の責務分担

ダッシュボードは、静的な画面ではなくDB上の実データを集計して表示します。

| レイヤ | 役割 |
|---|---|
| `DashboardController` | セッション確認、ログインユーザーとログイン時刻のModel設定、Service呼び出し |
| `DashboardService` | 本日の登録件数、今月の総作業時間、未出力件数、最近の活動をDTOにまとめる |
| `DashboardDao` | `work_reports` と `report_output_histories` への集計SQL、最近の活動SQLを実行する |
| `DashboardDto` | ダッシュボード画面に表示する集計値を保持する |
| `DashboardActivityDto` | 最近の活動一覧の1行分を保持する |

Controllerは画面表示に必要な値をModelへ詰めるだけにし、集計SQLは `DashboardDao` に閉じ込めています。

## 帳票作成履歴機能の責務分担

帳票作成履歴では、一覧検索、詳細確認、再ダウンロードを扱います。

| レイヤ | 役割 |
|---|---|
| `ReportHistoryController` | 一覧、詳細、ダウンロードURLを受け取り、画面遷移やレスポンス出力を制御する |
| `ReportHistoryService` | 履歴登録、履歴取得、ダウンロード可能なファイルかどうかの確認を行う |
| `ReportHistoryDao` | `report_output_histories` と `users` をJOINし、検索・詳細取得・INSERTを行う |
| `ReportHistorySearchForm` | 対象年月、帳票種別、作成者、ステータスの検索条件を保持する |
| `ReportHistoryDto` | 一覧・詳細画面に表示する履歴情報を保持する |
| `DownloadResponseUtil` | Excelダウンロード用のHTTPヘッダーとレスポンス書き込みを共通化する |

ステータス表示は、DAOのSELECT内で `SUCCESS` を「完了」、`ERROR` を「エラー」、`PROCESSING` を「処理中」に変換しています。現在のExcel出力は同期処理ですが、DB定義済みの `PROCESSING` は画面表示に対応しています。

帳票作成履歴は、Service層でログインユーザーの権限を確認します。`ADMIN` は全ユーザー分を扱えますが、`USER` は自分が作成した履歴だけを検索・詳細表示・再ダウンロードできます。ControllerのURLを直接指定されても、Service側で対象外の履歴を `null` として扱うため、他ユーザーのファイルを取得できないようにしています。

## 月次報告書出力の責務分担

月次報告書出力では、Excel作成だけでなく、ファイル保存と履歴登録も行います。

| レイヤ | 役割 |
|---|---|
| `MonthlyReportController` | 入力画面表示、入力エラー表示、Excelレスポンス出力 |
| `MonthlyReportService` | 入力チェック、一般ユーザーの本人確認、帳票データ作成、ファイル保存と履歴登録の流れを制御 |
| `MonthlyReportDao` | 月次サマリー、分類別集計、日別明細のSQLを実行 |
| `ExcelReportService` | Apache POIでテンプレートExcelへ値を差し込む |
| `ReportHistoryService` | ファイル保存、履歴登録、保存先パス検証、再ダウンロード用ファイル読込 |
| `FileNameUtils` | 帳票ファイル名に使う文字列を安全化する |

一般ユーザーは自分の部署・社員名の月次報告書のみ出力できます。管理者は画面から部署・社員名を指定して出力できます。

履歴登録は `ReportHistoryService` でトランザクション管理します。Excelファイル保存後に履歴登録が失敗した場合は、生成済みファイルを削除して、ファイルだけが残る不整合を減らします。

## 例外処理の考え方

例外処理は、利用者に見せるエラーと、開発者・運用者が調査するエラーを分けて考えます。

入力チェックエラーは、Serviceで検出してControllerへ返し、JSPに分かりやすいメッセージを表示します。たとえば作業日報登録では、作業日未入力や作業時間が0以下の場合に登録画面を再表示します。

DB検索で0件になるケースは、必ずしもシステムエラーではありません。`UserDao#findByLoginId` では、ログインIDが見つからない場合に `null` を返し、Serviceが認証失敗として扱います。

一方、DB接続エラー、Excel作成エラー、ファイル保存エラーはシステム側の問題として扱います。月次報告書出力では、失敗時に `report_output_histories` へ `ERROR` ステータスとエラーメッセージを保存する方針です。

## ログ出力の考え方

運用時は、Controller、Service、DAOの主要な処理開始・終了、例外発生時にログを出力します。

ログに残すとよい情報は、処理名、ログインユーザーID、対象年月、履歴ID、例外内容などです。ただし、パスワードや個人情報を過度に出力しないように注意します。
