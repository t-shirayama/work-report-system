# work-report-system

## 1. プロジェクト名

**work-report-system** は、日々の作業実績を登録し、月次報告書をExcel形式で自動作成するWebアプリケーションです。

既存業務で手作業作成されているExcel帳票を、古めのSpring MVC業務システム構成でシステム化することを想定したポートフォリオ用プロジェクトです。

## 2. システム概要

本システムでは、作業者が日々の作業内容、作業時間、案件情報などを登録し、登録済みデータをもとに月次報告書をExcel形式で出力します。

帳票は既存のExcelテンプレートに対してApache POIで値を差し込む方式とし、業務現場で使われている帳票作成フローをWebアプリケーションとして再現します。

## 3. 開発背景

業務システムでは、現在でもSpring Bootではなく、Spring Framework、Spring MVC、JSP、Servlet、Tomcat、Spring JDBCで構成された既存システムが運用されているケースがあります。

このプロジェクトでは、参画予定案件の技術構成に近い形で、以下を学習・提示できることを目的とします。

- Spring MVCによる古典的なWebアプリケーション構成
- Controller、Service、DAOに分けた業務アプリケーション設計
- Spring JDBCによる明示的なSQL実装
- JSP / JSTLによるサーバーサイド画面描画
- Apache POIによるExcel帳票出力
- STS / EclipseとTomcatを使った開発・実行手順

## 4. 想定業務フロー

1. 利用者がログインする
2. ダッシュボードで当月の作業登録状況を確認する
3. 日々の作業実績を登録・更新する
4. 登録済みの作業実績を条件検索する
5. 対象年月を指定して月次報告書を作成する
6. システムがExcelテンプレートへ作業実績を反映する
7. 作成されたExcelファイルをダウンロードする
8. 帳票作成履歴から過去に作成した帳票を再ダウンロードする

## 5. 主な機能一覧

| 機能 | 概要 |
|---|---|
| ログイン | 利用者IDとパスワードによる認証を行う |
| ダッシュボード | 当月の作業日数、登録状況、帳票作成状況を表示する |
| 作業日報登録 | 日付、作業内容、作業時間、備考などを登録する |
| 作業実績検索 | 対象年月、作業日、キーワードなどで実績を検索する |
| 月次報告書Excel出力 | 登録済み作業実績をもとにExcel帳票を作成する |
| 帳票作成履歴 | 作成済み帳票の履歴を一覧表示する |
| 作成済み帳票の再ダウンロード | 過去に作成したExcelファイルを再取得する |

## 6. 画面一覧

| 画面ID | 画面名 | 概要 |
|---|---|---|
| LOGIN | ログイン画面 | 利用者認証を行う |
| DASHBOARD | ダッシュボード画面 | 当月の作業状況、帳票作成状況を表示する |
| DAILY_INPUT | 作業日報登録画面 | 日々の作業実績を登録・更新する |
| WORK_SEARCH | 作業実績検索画面 | 登録済みの作業実績を検索する |
| WORK_DETAIL | 作業実績詳細画面 | 作業実績の詳細確認と編集を行う |
| REPORT_CREATE | 月次報告書作成画面 | 対象年月を指定して帳票を作成する |
| REPORT_HISTORY | 帳票作成履歴画面 | 作成済み帳票の一覧を表示する |

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

現在は、Spring MVCの基本構成、簡易ログイン、作業日報登録、作業実績検索、月次報告書Excel出力、帳票作成履歴、開発用Oracle DB環境を含む構成になっています。

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
          service/
          util/
      resources/
      webapp/
        resources/
          css/
          js/
        WEB-INF/
          web.xml
          spring/
            applicationContext.xml
            dispatcher-servlet.xml
          views/
    test/
      java/
  docs/
    README.md
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

`docs/` 配下の各Markdownファイルは、実装済み機能を教材として読み返せるように整理しています。索引は `docs/README.md` です。

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
- ポートフォリオ開発用DBは、ローカル環境構築を簡単にするためDocker ComposeでOracle Database FreeまたはOracle Database XEを起動する構成にしてよい
- 本リポジトリでは、開発用DBとして `docker-compose.yml` でOracle Database Freeを起動する
- 開発用DBの接続先は `jdbc:oracle:thin:@//localhost:1521/FREEPDB1` とする
- 主キーは業務要件に応じて数値IDまたは複合キーを検討する
- 日付、年月、作業時間を検索しやすい形で保持する
- 帳票ファイルはファイルシステム保存を基本案とし、DBには保存先パスや作成日時を保持する
- 物理削除ではなく、必要に応じて削除フラグによる論理削除を検討する

## 12. Excel帳票出力方針

月次報告書は、Apache POI 3.17を使用してExcelテンプレートに値を差し込む方式で作成します。

方針は以下です。

- テンプレートExcelを `src/main/resources/report-template/` 配下に配置する
- Service層で帳票作成処理の全体制御を行う
- Report専用クラスでApache POIによるセル操作を行う
- 出力ファイルは `generated-reports/` 配下に保存する想定とする
- 作成履歴をDBに登録し、後から再ダウンロードできるようにする
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

ログイン後、以下の画面を確認できます。

| URL | 確認内容 |
|---|---|
| `/dashboard` | ダッシュボード表示 |
| `/work-reports/new` | 作業日報登録 |
| `/work-reports/search` | 作業実績検索 |
| `/monthly-reports/new` | 月次報告書Excel出力 |
| `/report-histories` | 帳票作成履歴、再ダウンロード |

月次報告書を出力すると、開発用として `generated-reports/` 配下にExcelファイルが保存されます。このディレクトリは `.gitignore` 対象です。

### 14.1 接続設定

アプリケーションのDB接続設定は以下です。

```text
src/main/resources/application.properties
```

Docker Composeで起動したOracle Database Freeへ接続する設定になっています。

```properties
jdbc.driverClassName=oracle.jdbc.OracleDriver
jdbc.url=jdbc:oracle:thin:@//localhost:1521/FREEPDB1
jdbc.username=work_report
jdbc.password=work_report
```

### 14.2 よくある確認ポイント

| 症状 | 確認ポイント |
|---|---|
| `docker compose ps` で `healthy` にならない | 初回起動中の可能性があります。数分待ってから再確認します |
| DB接続に失敗する | `docker compose ps`、ポート `1521`、`application.properties` のURLを確認します |
| `mvn` が認識されない | MavenがPATHに設定されているか、STS同梱Mavenを使う設定か確認します |
| Tomcat起動時にServlet API関連で失敗する | `javax.servlet-api` と `javax.servlet.jsp-api` が `provided` スコープになっているか確認します |
| 404になる | URLのコンテキストパスが `work-report-system` になっているか、STSのModules設定を確認します |
| ログインできない | サンプルデータ投入済みか、`users` テーブルに `admin` / `sato` が存在するか確認します |

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
- `src/main/webapp/WEB-INF/spring/dispatcher-servlet.xml` でControllerスキャン、`mvc:annotation-driven`、JSP ViewResolver、静的リソースマッピングを定義
- `src/main/webapp/WEB-INF/spring/applicationContext.xml` は、将来のDB接続、トランザクション、Service/DAO共通設定の追加場所として用意
- `GET /home` は `HomeController` が受け取り、`/WEB-INF/views/home.jsp` を表示
- CSSは `/resources/css/common.css` として配信
- `GET /login`、`POST /login`、`GET /dashboard`、`GET/POST /logout` による簡易ログイン機能を実装
- ログイン成功時はHTTPセッションにログインユーザーを保存し、ログアウト時にセッションを破棄

現在のログイン機能はポートフォリオ用の簡易実装です。サンプルデータでは平文パスワードを使用していますが、本番では必ずパスワードをハッシュ化して保存・照合する必要があります。

## 15. 学習ポイント

このプロジェクトで学習・説明できる内容は以下です。

- Spring Bootを使わないSpring MVCの基本構成
- `web.xml` を使用したDispatcherServlet設定
- JSP / JSTLによる画面作成
- Controller、Service、DAOの責務分離
- Spring JDBCとNamedParameterJdbcTemplateによるDBアクセス
- Oracle向けSQLの作成
- Apache POIによるExcel帳票作成
- Maven WARプロジェクトの構成
- STS / Eclipse / Tomcatによる業務システム開発の流れ

## 16. 今後の拡張案

- 入力チェックとエラーメッセージ表示の充実
- パスワードハッシュ化
- ロール別権限制御
- 承認一覧
- 作業日報の申請・承認・差戻し
- 部署マスタ、社員マスタ、作業分類マスタ
- 管理者向けメニュー
- 月次報告書の複数フォーマット対応
- 帳票テンプレート差し替え機能
- 作業実績のCSV出力
- 作業時間の集計グラフ表示
- Docker ComposeによるOracle Database Free / XEの開発用起動補助
- 単体テストと結合テストの拡充

## 17. 未決事項

現時点で未決の事項は以下です。実装時に要件を整理しながら決定します。

- ログイン認証方式とパスワード管理方式
- Oracle接続情報の管理方法
- 帳票テンプレートの具体的なレイアウト
- 作業実績として管理する入力項目の詳細
- 休日、休暇、欠勤などの扱い
- 帳票ファイル保存先の正式なパス
- テスト用DBをどのように用意するか

## docs配下の学習用ドキュメント

`docs/` 配下には、このプロジェクトを教材として読み進めるための学習用ドキュメントを用意しています。索引として `docs/README.md` を用意しているため、基本的にはここから読み始めてください。

| ドキュメント | 内容 |
|---|---|
| `docs/README.md` | ドキュメント索引、読む順番、カテゴリ別一覧 |
| `docs/architecture/spring-mvc-basic.md` | Spring MVCのリクエスト処理、DispatcherServlet、Controller、ViewResolver、JSP表示の流れ |
| `docs/architecture/controller-service-dao.md` | Controller / Service / DAO の責務分担、DTOとEntity、例外処理とログ出力の考え方 |
| `docs/database/spring-jdbc-basic.md` | Spring JDBC、NamedParameterJdbcTemplate、バインド変数、SQLとDTOの対応、動的検索条件 |
| `docs/database/database-design.md` | Oracle前提のDB設計、テーブル定義、ER図、インデックス、DAO実装との対応 |
| `docs/database/oracle-docker-setup.md` | Docker ComposeによるOracle Database Freeの起動、初期化、接続確認 |
| `docs/reporting/apache-poi-basic.md` | Apache POI 3.17の基本、Workbook / Sheet / Row / Cell、テンプレート方式 |
| `docs/reporting/excel-report-generation.md` | 月次報告書Excel出力、履歴保存、再ダウンロード、例外処理の流れ |
| `docs/walkthrough/code-walkthrough.md` | ログイン、日報登録、検索、帳票出力、履歴機能をコードの流れで説明 |

おすすめの読む順番は以下です。

1. `docs/architecture/spring-mvc-basic.md`
2. `docs/architecture/controller-service-dao.md`
3. `docs/database/database-design.md`
4. `docs/database/oracle-docker-setup.md`
5. `docs/database/spring-jdbc-basic.md`
6. `docs/reporting/apache-poi-basic.md`
7. `docs/reporting/excel-report-generation.md`
8. `docs/walkthrough/code-walkthrough.md`

最初にSpring MVCとレイヤ構成を理解し、その後DBとSpring JDBC、最後にExcel帳票と実装全体の流れを確認すると、面談前の復習資料として使いやすくなります。
