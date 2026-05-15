# work-report-system

## 1. プロジェクト名

**work-report-system** は、日々の作業実績を登録し、月次報告書をExcel形式で自動作成するWebアプリケーションです。

既存業務で手作業作成されているExcel帳票を、Spring MVCベースの業務システムとしてシステム化することを想定したプロジェクトです。

## 2. システム概要

本システムでは、作業者が日々の作業内容、作業時間、案件情報などを登録し、登録済みデータをもとに月次報告書をExcel形式で出力します。

帳票は既存のExcelテンプレートに対してApache POIで値を差し込む方式とし、業務現場で使われている帳票作成フローをWebアプリケーションとして再現します。

## 3. 開発背景

業務システムでは、現在でもSpring Bootではなく、Spring Framework、Spring MVC、JSP、Servlet、Tomcat、Spring JDBCで構成された既存システムが運用されているケースがあります。

本プロジェクトは、既存Java業務システムの保守・改修を想定し、あえてSpring BootではなくSpring Framework 4.x / Spring MVC / JSP / WAR構成で実装しています。新規開発での推奨構成を示すものではなく、既存業務システムに近い構成で設計・実装・保守の流れを確認するための構成です。

このプロジェクトでは、既存のJava業務システムで採用される構成を前提に、以下を実現することを目的とします。

- Spring MVCによるWebアプリケーション構成
- Controller、Service、DAOに分けた業務アプリケーション設計
- Spring JDBCによる明示的なSQL実装
- JSP / JSTLによるサーバーサイド画面描画
- Apache POIによるExcel帳票出力
- STS / EclipseとTomcatを使った開発・実行手順

## 4. 想定業務フロー

1. 利用者がログインする
2. ダッシュボードで当月の作業登録状況を確認する
3. 日々の作業実績を登録する
4. 登録済みの作業実績を条件検索する
5. 対象年月を指定して月次報告書を作成する
6. システムがExcelテンプレートへ作業実績を反映する
7. 作成されたExcelファイルをダウンロードする
8. 帳票作成履歴から過去に作成した帳票を再ダウンロードする

## 5. 主な機能一覧

| 機能 | 概要 |
|---|---|
| ログイン | 利用者IDとパスワードによる認証を行う |
| ダッシュボード | DB上の作業日報・帳票履歴を集計し、本日の登録件数、今月の総作業時間、未出力件数、最近の活動を表示する |
| 作業日報登録 | 作業日、プロジェクト名、作業分類、作業時間、作業内容を登録する |
| 作業実績検索 | 対象期間、社員、部署、作業分類、プロジェクト名で実績を検索する |
| 月次報告書Excel出力 | 登録済み作業実績をもとにExcel帳票を作成し、ブラウザからダウンロードする |
| 帳票作成履歴 | 対象年月、帳票種別、作成者、ステータスで作成済み帳票の履歴を検索・一覧表示する |
| 帳票作成履歴詳細 | 帳票出力結果、保存先、エラーメッセージを確認する |
| 作成済み帳票の再ダウンロード | `generated-reports/` 配下に保存したExcelファイルを再取得する |

## 5.1 できること / 運用環境で調整すること

現在のアプリケーションで利用できる主な機能は以下です。

- Spring SecurityとBCryptによるログイン認証
- ログインユーザーの権限に応じた作業実績・帳票履歴の参照制御
- 作業日報登録、作業実績検索、月次報告書Excel出力
- 帳票出力履歴の `PROCESSING`、`SUCCESS`、`ERROR` 管理と再ダウンロード
- Docker Composeによる開発用Oracle Database Freeの起動補助

運用環境へ適用する際は、以下を環境に合わせて調整します。

- DB接続情報の外部化方式、JNDI DataSource、接続プール
- 帳票ファイルの正式な保存先と保管期間
- 長時間 `PROCESSING` のまま残った帳票履歴の確認・異常終了扱い
- アカウントロック、パスワード変更、有効期限などの運用ルール
- `users.enabled`、`users.account_locked`、`users.failed_login_count`、`users.password_changed_at`、`users.last_login_at` などのアカウント状態管理
- 本番データ量に合わせたインデックス、監視、バックアップ

## 5.2 画面イメージ

画面設計の参考画像は `docs/designs/` 配下に配置しています。

| 画面 | 画像 |
|---|---|
| ログイン | [ログイン画面.png](docs/designs/ログイン画面.png) |
| ダッシュボード | [ダッシュボード.png](docs/designs/ダッシュボード.png) |
| 作業日報登録 | [作業日報登録.png](docs/designs/作業日報登録.png) |
| 帳票出力・履歴 | [帳票出力・履歴.png](docs/designs/帳票出力・履歴.png) |

## 6. 画面一覧

| 画面ID | 画面名 | 概要 |
|---|---|---|
| LOGIN | ログイン画面 | 利用者認証を行う |
| DASHBOARD | ダッシュボード画面 | 本日の登録件数、今月の総作業時間、未出力件数、最近の活動を表示する |
| DAILY_INPUT | 作業日報登録画面 | 日々の作業実績を登録する |
| WORK_SEARCH | 作業実績検索画面 | 登録済みの作業実績を検索する |
| REPORT_CREATE | 月次報告書作成画面 | 対象年月を指定して帳票を作成する |
| REPORT_HISTORY | 帳票作成履歴画面 | 作成済み帳票を検索・一覧表示する |
| REPORT_HISTORY_DETAIL | 帳票作成履歴詳細画面 | ファイル情報やエラー内容を確認する |

## 7. 帳票一覧

| 帳票ID | 帳票名 | 出力形式 | 概要 |
|---|---|---|---|
| MONTHLY_WORK_REPORT | 月次作業報告書 | Excel | 対象月の作業実績を日別に出力する |

## 8. 技術スタック

| 分類 | 技術 | バージョン |
|---|---|---|
| Java | Java | 1.8 |
| Framework | Spring Framework | 4.3.17.RELEASE |
| Web | Spring MVC | 4.3.17.RELEASE |
| Security | Spring Security | 4.2.20.RELEASE |
| View | JSP / JSTL | JSP 2.3 / JSTL 1.2 |
| Servlet Container | Apache Tomcat | 8.5.x |
| Excel | Apache POI | 3.17 |
| Database | Oracle Database | 11g / 12c / 19c 想定 |
| JDBC Driver | ojdbc8 | 19.17.0.0 |
| DB Access | Spring JDBC | Spring Framework同梱 |
| Build | Maven | 3.x |
| Packaging | WAR | war |
| Test | JUnit | 4.x |
| IDE | STS / Eclipse | STS想定 |

## 9. アーキテクチャ概要

本プロジェクトは、Spring Bootを使用しない従来型のSpring MVCアプリケーションとして構成します。

想定するレイヤ構成は以下です。

| レイヤ | 役割 |
|---|---|
| Controller | リクエスト受付、入力値の受け取り、画面遷移制御 |
| Service | 業務ロジック、トランザクション境界、帳票作成処理の制御 |
| DAO | Spring JDBCによるDBアクセス、SQLの管理 |
| Form | JSPフォームの入力値保持、画面とのデータ受け渡し |
| DTO | レイヤ間で扱うデータ構造 |
| View | JSP / JSTLによる画面表示 |
| Report | Apache POIによるExcel帳票作成 |

## 10. ディレクトリ構成

現在は、Spring MVCの基本構成、ログイン、DB連動ダッシュボード、作業日報登録、作業実績検索、月次報告書Excel出力、帳票作成履歴検索・詳細、開発用Oracle DB環境を含む構成になっています。

```text
work-report-system/
  README.md
  AGENTS.md
  .gitignore
  docker-compose.yml
  docker/
    oracle/
      init/
        01-init-work-report.sql
  pom.xml
  src/
    main/
      java/
        com/example/workreport/
          common/
          config/
          controller/
          dao/
          dto/
          entity/
          form/
          exception/
          security/
          service/
          util/
      resources/
        application.properties
        sql/
          schema.sql
          sample-data.sql
        templates/
          monthly-report-template.xlsx
      webapp/
        resources/
          css/
          js/
        WEB-INF/
          web.xml
          spring/
            applicationContext.xml
            dispatcher-servlet.xml
            security-context.xml
          views/
    test/
      java/
  docs/
    README.md
    project/
      development-guidelines.md
    architecture/
      spring-mvc-basic.md
      controller-service-dao.md
    database/
      database-design.md
      oracle-docker-setup.md
      spring-jdbc-basic.md
    reporting/
      apache-poi-basic.md
      excel-report-generation.md
    walkthrough/
      code-walkthrough.md
    designs/
```

`docs/` 配下の各Markdownファイルは、設計、実装方針、運用手順を機能別に整理しています。索引は `docs/README.md` です。

## 11. DB設計概要

想定する主なテーブルは以下です。

| テーブル名 | 概要 |
|---|---|
| departments | 部署マスタを管理する |
| users | ログイン利用者を管理する |
| work_reports | 日々の作業実績を管理する |
| report_output_histories | 作成済み帳票ファイルの履歴を管理する |

主な設計方針は以下です。

- 本番・案件想定のDBはOracle Databaseとする
- JDBCドライバはOracle JDBC Driver 19.17.0.0を使用し、Maven依存関係は `ojdbc8:19.17.0.0` とする
- SQL、DDL、DAO実装はOracle Database前提で作成し、H2 / PostgreSQL / MySQL向けに寄せない
- 開発用DBは、ローカル環境構築を簡単にするためDocker ComposeでOracle Database FreeまたはOracle Database XEを起動する構成にしてよい
- 本リポジトリでは、開発用DBとして `docker-compose.yml` でOracle Database Freeを起動する
- 開発用DBの接続先は `jdbc:oracle:thin:@//localhost:1521/FREEPDB1` とする
- 主キーは `NUMBER(10)` の数値IDを基本とし、Oracleシーケンスで採番する
- 日付、年月、作業時間を検索しやすい形で保持する
- 帳票ファイルはファイルシステム保存を基本案とし、DBには保存先パスや作成日時を保持する
- 物理削除ではなく、必要に応じて削除フラグによる論理削除を検討する

## 12. Excel帳票出力方針

月次報告書は、Apache POI 3.17を使用してExcelテンプレートに値を差し込む方式で作成します。

方針は以下です。

- テンプレートExcelを `src/main/resources/templates/` 配下に配置する
- Service層で帳票作成処理の全体制御を行う
- `ExcelReportService` でApache POIによるセル操作を行う
- 出力ファイルは `generated-reports/` 配下に保存する想定とする
- 作成履歴をDBに登録し、後から再ダウンロードできるようにする
- ファイル名は保存前に安全な文字へ変換し、履歴IDを含めて同一年月・同一社員の再出力でも物理ファイルが上書きされないようにする
- ファイル保存時は既存ファイルを上書きせず、再ダウンロード時も `generated-reports/` 配下のファイルだけを読み込む
- 月次報告書の集計条件は内部的に `user_id` を使用し、部署名・社員名は表示用として扱う
- 月次報告書の出力対象は一般ユーザーとし、管理者は対象社員を選択して出力する
- 対象期間に作業実績が0件の場合も、0件の月次報告書としてExcelを出力する
- 帳票履歴は作成操作を行ったユーザーと帳票対象ユーザーを分けて管理する
- 一般ユーザーは自分の月次報告書と帳票作成履歴のみ扱い、管理者は帳票作成履歴を全件確認できる
- セル位置や帳票レイアウトの変更に備え、定数化や設定化を検討する

## 13. 導入手順

初めてこのリポジトリを動かす場合は、以下の順番で確認してください。

最短の流れは、開発用DBを起動し、Mavenでビルド確認を行い、STSへインポートしてTomcat 8.5で起動する、という順番です。

```text
1. 前提ソフトウェア確認
2. docker compose up -d oracle-db
3. DB接続・サンプルデータ確認
4. mvn test / mvn package
5. STSへExisting Maven Projectsとしてインポート
6. Tomcat 8.5へ追加して起動
7. /home と /login をブラウザで確認
```

### 13.1 前提ソフトウェア

| ソフトウェア | 用途 | 確認コマンド |
|---|---|---|
| JDK 8 | Javaコンパイル、Tomcat実行 | `java -version` |
| Maven 3.x | 依存関係解決、WAR作成 | `mvn -version` |
| Docker Desktop | 開発用Oracle DB起動 | `docker version` |
| STS / Eclipse | 開発IDE | 画面上で確認 |
| Apache Tomcat 8.5.x | ローカルアプリ実行 | STSのServersビューで確認 |

`java -version` ではJava 1.8系が使われていることを確認してください。Java 9以降を前提にした実装にはしません。

### 13.2 リポジトリ取得

任意の作業ディレクトリでリポジトリを取得し、プロジェクトルートへ移動します。

```powershell
git clone <repository-url>
cd work-report-system
```

すでに取得済みの場合は、`work-report-system` のルートディレクトリで以降のコマンドを実行してください。

### 13.3 開発用Oracle DBの起動

アプリケーション本体はDocker化しません。Docker Composeは開発用Oracle Database Freeを起動するためだけに使用します。

```powershell
docker compose up -d oracle-db
```

起動状態を確認します。

```powershell
docker compose ps
```

`oracle-db` の `STATUS` に `healthy` が表示されれば、DB起動と初期化が完了しています。初回はOracleイメージの取得とDB作成のため、数分かかる場合があります。

### 13.4 DB接続確認

SQL*Plusで `work_report` ユーザーに接続します。

```powershell
docker compose exec oracle-db sqlplus -L work_report/work_report@//localhost:1521/FREEPDB1
```

接続できたら、以下のSQLでサンプルデータが投入されていることを確認します。

```sql
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM work_reports;
SELECT COUNT(*) FROM report_output_histories;
EXIT;
```

目安として、初期データは以下の件数です。

| テーブル | 件数 |
|---|---:|
| `departments` | 3 |
| `users` | 5 |
| `work_reports` | 32 |
| `report_output_histories` | 5 |

PowerShellから一度に確認する場合は、以下でも確認できます。

```powershell
"SELECT COUNT(*) AS USER_COUNT FROM users;`nSELECT COUNT(*) AS WORK_REPORT_COUNT FROM work_reports;`nSELECT COUNT(*) AS HISTORY_COUNT FROM report_output_histories;`nEXIT;" | docker compose exec -T oracle-db sqlplus -L work_report/work_report@//localhost:1521/FREEPDB1
```

### 13.5 Mavenでビルド確認

STSへインポートする前に、Mavenで依存関係解決とビルドを確認します。

```powershell
mvn test
mvn package
```

`mvn package` が成功すると、以下のWARファイルが作成されます。

```text
target/work-report-system.war
```

現在の単体テストは、Service層の入力チェックと権限制御、帳票履歴の `PROCESSING -> SUCCESS/ERROR` 遷移、ファイル名サニタイズ、帳票ファイル読込時のパス制限を中心に配置しています。

`mvn test` では JaCoCo によるカバレッジ確認も実行されます。対象はユニットテストで検証する業務ロジック層（Service / Security / Util）とし、画面遷移、DAOのSQL、DTO/Form/Entity、Excelテンプレートへの実書き込みは結合テストまたは画面確認の対象として分けています。ユニットテスト対象のラインカバレッジとブランチカバレッジは 100% を下回るとビルド失敗になります。

この時点でMavenが見つからない場合は、MavenのインストールまたはSTS同梱Mavenの利用設定を確認してください。

### 13.6 STSへのインポート

1. STSを起動する
2. `File > Import > Maven > Existing Maven Projects` を選択する
3. 本プロジェクトのルートディレクトリを選択する
4. `pom.xml` が認識されていることを確認する
5. `Finish` を押してインポートする
6. Maven Dependencies が解決されることを確認する
7. `pom.xml` の packaging が `war` になっていることを確認する
8. Project Facets で Java 1.8 / Dynamic Web Module を確認する

Eclipse / STS固有ファイルである `.project`、`.classpath`、`.settings/` は原則コミットしません。

### 13.7 Tomcat 8.5の設定

1. STSのServersビューを開く
2. `No servers are available. Click this link to create a new server...` を選択する
3. `Apache > Tomcat v8.5 Server` を選択する
4. Tomcat 8.5のインストールディレクトリを指定する
5. 作成したServerに `work-report-system` を追加する
6. Serverを起動する

コンテキストパスは通常 `work-report-system` になります。異なる場合は、STSのServer設定でModulesタブを確認してください。

## 14. 起動確認方法

Tomcat起動後、ブラウザで以下にアクセスします。

```text
http://localhost:8080/work-report-system/home
```

トップ画面が表示されれば、DispatcherServlet、Controller、ViewResolver、JSP、静的CSSの最小疎通は成功です。

ログイン機能は以下で確認します。

```text
http://localhost:8080/work-report-system/login
```

サンプルユーザー例は以下です。

| ログインID | パスワード |
|---|---|
| `admin` | `password` |
| `sato` | `password` |

`sample-data.sql` では、上記の初期パスワードをBCryptハッシュとして `users.password` に保存しています。

ログイン後、以下の画面を確認できます。

| URL | 確認内容 |
|---|---|
| `/dashboard` | DB集計結果を使ったダッシュボード表示 |
| `/work-reports/new` | 作業日報登録 |
| `/work-reports/search` | 作業実績検索 |
| `/monthly-reports/new` | 月次報告書Excel出力 |
| `/report-histories` | 帳票作成履歴検索、詳細、再ダウンロード |

月次報告書を出力すると、開発用として `generated-reports/` 配下にExcelファイルが保存されます。このディレクトリは `.gitignore` 対象です。

### 14.1 接続設定

アプリケーションのDB接続設定は以下です。

```text
src/main/resources/application.properties
```

Docker Composeで起動したOracle Database Freeへ接続する開発用設定になっています。

```properties
jdbc.driverClassName=${JDBC_DRIVER_CLASS_NAME:oracle.jdbc.OracleDriver}
jdbc.url=${JDBC_URL:jdbc:oracle:thin:@//localhost:1521/FREEPDB1}
jdbc.username=${JDBC_USERNAME:work_report}
jdbc.password=${JDBC_PASSWORD:work_report}
```

Docker Compose側のDBユーザーや管理者パスワードを変更する場合は、`.env.example` を参考に `.env` を作成します。`.env` はコミット対象外です。アプリケーション側の `JDBC_URL`、`JDBC_USERNAME`、`JDBC_PASSWORD` を変更する場合は、STS/Tomcatの起動構成で環境変数として設定します。上記のデフォルト値はローカル開発専用であり、運用環境では環境変数、JNDI、外部設定ファイルなどへ外部化します。

### 14.2 よくある確認ポイント

| 症状 | 確認ポイント |
|---|---|
| `docker compose ps` で `healthy` にならない | 初回起動中の可能性があります。数分待ってから再確認します |
| DB接続に失敗する | `docker compose ps`、ポート `1521`、`.env`、`application.properties` のURLを確認します |
| `mvn` が認識されない | MavenがPATHに設定されているか、STS同梱Mavenを使う設定か確認します |
| Tomcat起動時にServlet API関連で失敗する | `javax.servlet-api` と `javax.servlet.jsp-api` が `provided` スコープになっているか確認します |
| 404になる | URLのコンテキストパスが `work-report-system` になっているか、STSのModules設定を確認します |
| ログインできない | サンプルデータ投入済みか、`users` テーブルに `admin` / `sato` が存在するか確認します |
| POST送信で403になる | Spring SecurityのCSRFトークンがフォームに出力されているか確認します |

### 14.3 DBを初期化し直す場合

開発用DBを作り直す場合は、以下を実行します。

```powershell
docker compose down -v
docker compose up -d oracle-db
```

`down -v` はDocker volumeを削除するため、登録済みデータも消えます。必要なデータがないことを確認してから実行してください。

詳細手順は `docs/database/oracle-docker-setup.md` も参照してください。

### 14.4 現在のSpring MVC設定

現在のSpring MVC設定は以下です。

- `src/main/webapp/WEB-INF/web.xml` で `DispatcherServlet` と `ContextLoaderListener` を定義
- `web.xml` で `springSecurityFilterChain` を定義し、認証必須URLをSpring Securityで保護
- `src/main/webapp/WEB-INF/spring/dispatcher-servlet.xml` でControllerスキャン、`mvc:annotation-driven`、JSP ViewResolver、静的リソースマッピングを定義
- `src/main/webapp/WEB-INF/spring/applicationContext.xml` でDB接続、トランザクション、Service/DAO共通設定を定義
- `src/main/webapp/WEB-INF/spring/security-context.xml` でログイン、ログアウト、CSRF、認証必須URL、BCrypt照合を定義
- `GET /home` は `HomeController` が受け取り、`/WEB-INF/views/home.jsp` を表示
- CSSは `/resources/css/common.css` として配信
- `GET /login` は `LoginController` がログイン画面を表示し、`POST /login` の認証処理はSpring Securityが実行
- ログイン成功時はHTTPセッションにログインユーザーとログイン時刻を保存し、ログアウト時にセッションを破棄
- remember-meは使用せず、セッション固定攻撃対策はSpring Security設定で明示
- `GET /dashboard` では `DashboardController` がDB集計結果を取得し、ログイン時刻はセッション値を表示
- 作業実績検索では、一般ユーザーは自分の作業実績だけを参照し、管理者は全ユーザー分を検索できる
- 帳票作成履歴は `GET /report-histories` で検索、`GET /report-histories/{id}` で詳細確認、`GET /report-histories/{id}/download` で再ダウンロード
- 月次報告書出力では、開始時に帳票履歴を `PROCESSING` で登録し、成功時は `SUCCESS`、失敗時は `ERROR` へ更新する

DB接続情報は環境変数で上書きできる開発用デフォルト値を持ちます。運用環境では環境変数、JNDI、外部設定ファイルなどに外部化し、アプリケーションの配布物に接続先・認証情報を固定しない方針とします。ローカル起動用に `applicationContext-local-datasource.xml`、Tomcat運用向けに `applicationContext-jndi-datasource.xml` を分けています。

## 15. 設計・実装ポイント

このプロジェクトで重視する設計・実装上のポイントは以下です。

- Spring Bootを使わないSpring MVCの基本構成
- `web.xml` を使用したDispatcherServlet設定
- JSP / JSTLによる画面作成
- Controller、Service、DAOの責務分離
- Spring JDBCとNamedParameterJdbcTemplateによるDBアクセス
- Spring SecurityとBCryptによるログイン認証
- Oracle向けSQLの作成
- Apache POIによるExcel帳票作成
- Maven WARプロジェクトの構成
- STS / Eclipse / Tomcatによる業務システム開発の流れ

設計上は、Spring Bootへ寄せずに `web.xml` とXML設定を使うこと、DAOにSQLを明示すること、Oracle Database前提のDDL/SQLを維持すること、既存ExcelテンプレートにApache POIで差し込むことを重視しています。

## 16. 今後の拡張案

- 入力チェックとエラーメッセージ表示の充実
- アカウントロック、パスワード変更、パスワード有効期限管理
- ロール別権限制御
- 承認一覧
- 作業日報の申請・承認・差戻し
- 部署マスタ、社員マスタ、作業分類マスタ
- 管理者向けメニュー
- 作業日報の一覧、詳細、編集、削除
- 作業実績検索のページング
- 月次報告書の複数フォーマット対応
- 帳票テンプレート差し替え機能
- 作業実績のCSV出力
- 作業時間の集計グラフ表示
- 単体テストと結合テストの拡充

## 17. 未決事項

運用環境へ適用する際に決定が必要な事項は以下です。

- 運用環境で採用するJNDI DataSourceまたは接続プールの具体設定
- 帳票テンプレートの正式レイアウト
- 作業実績として管理する入力項目の追加要否
- 休日、休暇、欠勤などの扱い
- 帳票ファイル保存先の正式なパス
- テスト用DBと自動テストデータの管理方式

## docs配下のドキュメント

`docs/` 配下には、このプロジェクトの設計、実装方針、運用手順を確認するためのドキュメントを用意しています。索引として `docs/README.md` を用意しているため、基本的にはここから参照してください。

| ドキュメント | 内容 |
|---|---|
| `docs/README.md` | ドキュメント索引、読む順番、カテゴリ別一覧 |
| `docs/project/development-guidelines.md` | 固定技術、実装ルール、禁止事項、Codex作業時の詳細ガイドライン |
| `docs/architecture/spring-mvc-basic.md` | Spring MVCのリクエスト処理、DispatcherServlet、Controller、ViewResolver、JSP表示の流れ |
| `docs/architecture/controller-service-dao.md` | Controller / Service / DAO の責務分担、DTOとEntity、例外処理とログ出力の考え方 |
| `docs/database/spring-jdbc-basic.md` | Spring JDBC、NamedParameterJdbcTemplate、バインド変数、SQLとDTOの対応、動的検索条件 |
| `docs/database/database-design.md` | Oracle前提のDB設計、テーブル定義、ER図、インデックス、DAO実装との対応 |
| `docs/database/oracle-docker-setup.md` | Docker ComposeによるOracle Database Freeの起動、初期化、接続確認 |
| `docs/reporting/apache-poi-basic.md` | Apache POI 3.17の基本、Workbook / Sheet / Row / Cell、テンプレート方式 |
| `docs/reporting/excel-report-generation.md` | 月次報告書Excel出力、履歴保存、再ダウンロード、例外処理の流れ |
| `docs/walkthrough/code-walkthrough.md` | ログイン、日報登録、検索、帳票出力、履歴機能をコードの流れで説明 |

おすすめの読む順番は以下です。

1. `docs/project/development-guidelines.md`
2. `docs/architecture/spring-mvc-basic.md`
3. `docs/architecture/controller-service-dao.md`
4. `docs/database/database-design.md`
5. `docs/database/oracle-docker-setup.md`
6. `docs/database/spring-jdbc-basic.md`
7. `docs/reporting/apache-poi-basic.md`
8. `docs/reporting/excel-report-generation.md`
9. `docs/walkthrough/code-walkthrough.md`

最初に開発ガイドラインでプロジェクト全体の前提を把握し、その後Spring MVCとレイヤ構成、DBとSpring JDBC、最後にExcel帳票と実装全体の流れを確認すると、設計と実装の関係を追いやすくなります。
