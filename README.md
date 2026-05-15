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

現在は、業務機能を実装する前のMaven WARプロジェクトの土台として、以下の構成を作成しています。

```text
work-report-system/
  README.md
  AGENTS.md
  .gitignore
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
    spring-mvc-basic.md
    controller-service-dao.md
    spring-jdbc-basic.md
    apache-poi-basic.md
    excel-report-generation.md
    database-design.md
    code-walkthrough.md
```

`docs/` 配下の各Markdownファイルは、今後の機能実装や学習メモ作成に合わせて追加します。

現時点では、Javaコード、JSP画面、DB接続処理、業務機能はまだ作成していません。

## 11. DB設計概要

想定する主なテーブルは以下です。

| テーブル名 | 概要 |
|---|---|
| USERS | ログイン利用者を管理する |
| WORK_RECORDS | 日々の作業実績を管理する |
| REPORT_FILES | 作成済み帳票ファイルの履歴を管理する |
| CODE_VALUES | 区分値や表示用コードを管理する |

主な設計方針は以下です。

- Oracle Databaseでの利用を前提にする
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

## 13. STSでのセットアップ手順

1. STSを起動する
2. `File > Import > Maven > Existing Maven Projects` を選択する
3. 本プロジェクトのルートディレクトリを選択する
4. Maven Dependencies が解決されることを確認する
5. `pom.xml` の packaging が `war` になっていることを確認する
6. Serversビューで Tomcat 8.5 Server を追加する
7. Project Facets で Java 1.8 / Dynamic Web Module を確認する
8. プロジェクトをTomcatへ追加する
9. Tomcatを起動する
10. ブラウザで `http://localhost:8080/work-report-system/` にアクセスする

この土台作成時点ではControllerとJSPをまだ作成していないため、Tomcatへの配置確認が主目的です。画面表示は、次工程で最小ControllerとJSPを追加してから確認します。

## 14. Tomcat 8.5での実行方法

実装後は、STS上でTomcat 8.5 Server Runtimeを設定し、Maven WARプロジェクトとしてTomcatへ追加して起動します。

想定URLは以下です。

```text
http://localhost:8080/work-report-system/
```

アプリケーション本体はDocker前提にしません。Dockerを使用する場合は、Oracle互換DBや検証用DBの起動補助など、必要な範囲に限定します。

現在のSpring MVC設定は以下です。

- `src/main/webapp/WEB-INF/web.xml` で `DispatcherServlet` と `ContextLoaderListener` を定義
- `src/main/webapp/WEB-INF/spring/dispatcher-servlet.xml` でControllerスキャン、`mvc:annotation-driven`、JSP ViewResolver、静的リソースマッピングを定義
- `src/main/webapp/WEB-INF/spring/applicationContext.xml` は、将来のDB接続、トランザクション、Service/DAO共通設定の追加場所として用意

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
- 月次報告書の複数フォーマット対応
- 帳票テンプレート差し替え機能
- 作業実績のCSV出力
- 作業時間の集計グラフ表示
- Oracle以外の検証用DBプロファイル追加
- 単体テストと結合テストの拡充

## 17. 未決事項

現時点で未決の事項は以下です。実装時に要件を整理しながら決定します。

- ログイン認証方式とパスワード管理方式
- Oracle接続情報の管理方法
- 帳票テンプレートの具体的なレイアウト
- 作業実績として管理する入力項目の詳細
- 休日、休暇、欠勤などの扱い
- 帳票ファイル保存先の正式なパス
- サンプルデータの内容
- テスト用DBをどのように用意するか

## docs配下の学習用ドキュメント作成予定

実装とあわせて、以下の学習用ドキュメントを `docs/` 配下に作成していく予定です。

- `docs/spring-mvc-basic.md`
- `docs/controller-service-dao.md`
- `docs/spring-jdbc-basic.md`
- `docs/apache-poi-basic.md`
- `docs/excel-report-generation.md`
- `docs/database-design.md`
- `docs/code-walkthrough.md`
