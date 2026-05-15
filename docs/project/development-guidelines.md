# 開発ガイドライン

このドキュメントは、`AGENTS.md` から切り出した詳細な開発ルールです。

Codex公式ドキュメントの [AGENTS.md guide](https://developers.openai.com/codex/guides/agents-md) では、`AGENTS.md` はリポジトリ固有の追加指示や文脈をCodexへ渡すためのファイルとして扱われます。一方で、指示ファイルが大きくなりすぎると重要な指示が埋もれやすくなるため、本リポジトリでは `AGENTS.md` を最小限にし、詳細は `docs/` 配下に整理します。

## このプロジェクトで守ること

`work-report-system` は、日々の作業実績を登録し、月次報告書をExcel形式で自動作成するWebアプリケーションです。

業務システムとして、以下の方針を満たす状態を目指します。

- Spring Bootを使わないSpring MVC構成
- Spring Securityによるログイン認証とCSRF対策
- JSP / JSTLによるサーバーサイド画面
- Controller / Service / DAO の責務分離
- Spring JDBCによる明示的なSQL実装
- Oracle Database向けDDLとSQL
- Apache POI 3.17によるExcelテンプレート差し込み
- STS / Eclipse + Tomcat 8.5での開発・実行

## 固定技術スタック

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

## ディレクトリとパッケージ

ルートパッケージは `com.example.workreport` です。

| パッケージ | 役割 |
|---|---|
| `common` | セッションキー、共通定数など |
| `config` | Javaベース設定を追加する場合の配置先 |
| `controller` | Spring MVC Controller |
| `dao` | Spring JDBCによるDBアクセス |
| `dto` | 画面表示、検索結果、帳票用のデータ |
| `entity` | DBテーブルの1行に近いデータ |
| `exception` | 共通例外 |
| `form` | JSPフォームの入力値 |
| `security` | Spring Security連携、UserDetails、ログイン成功処理 |
| `service` | 業務ロジック、トランザクション境界 |
| `util` | ダウンロード処理などの汎用処理 |

主なWebリソースは以下に配置します。

| パス | 役割 |
|---|---|
| `src/main/webapp/WEB-INF/web.xml` | DispatcherServlet、ContextLoaderListener |
| `src/main/webapp/WEB-INF/spring/dispatcher-servlet.xml` | Spring MVC設定 |
| `src/main/webapp/WEB-INF/spring/applicationContext.xml` | 共通Bean、DB、トランザクション設定 |
| `src/main/webapp/WEB-INF/spring/security-context.xml` | Spring Security設定 |
| `src/main/webapp/WEB-INF/views/` | JSP |
| `src/main/webapp/resources/css/` | CSS |
| `src/main/webapp/resources/js/` | JavaScript |
| `src/main/resources/sql/` | Oracle向けDDL、サンプルデータ |
| `src/main/resources/templates/` | Excelテンプレート |

## Controller / Service / DAO

### Controller

ControllerはHTTPに近い処理を担当します。

- URL、HTTPメソッド、Form、Model、Sessionを扱う
- 入力チェック結果や画面メッセージをModelへ設定する
- Serviceを呼び出す
- JSP名またはリダイレクト先を返す
- SQLや帳票作成の詳細処理は書かない

### Service

Serviceは業務判断と処理の流れを担当します。

- 入力チェックを行う
- Form、Entity、DTOの変換を制御する
- 複数DAOや帳票サービスの呼び出し順を組み立てる
- トランザクション境界を意識する
- Controller固有のModel操作を持ち込まない

### DAO

DAOはDBアクセスを担当します。

- SQLを明示的に記述する
- `NamedParameterJdbcTemplate` を使う
- バインド変数でSQLインジェクションを防ぐ
- `RowMapper` で検索結果をEntityまたはDTOへ変換する
- 業務判断をDAOへ寄せすぎない

詳細は [Controller / Service / DAO 構成](../architecture/controller-service-dao.md) を参照してください。

## JSP作成ルール

- JSPは `WEB-INF/views/` 配下に配置し、直接URLアクセスさせない。
- JSTLを使用し、Javaスクリプトレットは原則使わない。
- 入力エラーは利用者が理解しやすい日本語で表示する。
- 画面データはControllerまたはServiceで準備し、JSPに業務ロジックを書かない。
- 画面変更時は `docs/designs/` の画像を確認し、業務利用に適した落ち着いたUIに合わせる。

## SQL / Spring JDBC

- SQLはDAO層に置く。
- 複雑なSELECTは `src/main/resources/sql/dao/` 配下のSQLファイルへ外出しし、`SqlFileLoader` で読み込む。
- 短いINSERT / UPDATE、シーケンス取得SQLは、処理との対応が分かりやすい場合にDAO内へ残してよい。
- Oracle Databaseで動作するSQLを前提にする。
- H2 / PostgreSQL / MySQL互換のためにOracle向けDDLやSQLを薄めない。
- 動的検索条件は、指定された条件だけWHERE句へ追加する。
- ユーザー入力をSQL文字列へ直接連結しない。
- `MapSqlParameterSource` や `BeanPropertySqlParameterSource` でパラメータを渡す。
- 検索結果0件、DB接続エラー、制約違反を分けて考える。

詳細は [Spring JDBC 基本](../database/spring-jdbc-basic.md) を参照してください。

## Excel帳票

- Apache POI 3.17で動作するAPIだけを使う。
- Excelはゼロから作るより、テンプレートへ値を差し込む方式を優先する。
- テンプレートは `src/main/resources/templates/` 配下に配置する。
- セル位置は定数化し、意味の分からないマジックナンバーを増やさない。
- 明細行はテンプレート行のスタイルをコピーして追加する。
- 作成ファイルは開発用として `generated-reports/` 配下に保存する。
- 出力結果は `report_output_histories` に保存し、再ダウンロードできるようにする。
- ファイル名にユーザー入力を使う場合は、パス区切り文字や制御文字を除去・置換する。
- 再ダウンロードでは、正規化した保存先が `generated-reports/` 配下であることを確認する。
- ストリームはtry-with-resourcesで確実にクローズする。

詳細は [Excel帳票出力](../reporting/excel-report-generation.md) を参照してください。

## 例外処理とログ

- 入力エラーは画面へ戻して、利用者向けメッセージを表示する。
- DB接続エラーやファイル出力エラーはシステムエラーとして扱う。
- 帳票出力開始時は履歴を `PROCESSING` で作成し、成功時は `SUCCESS`、失敗時は `ERROR` へ更新する。
- 帳票履歴のDB登録・更新はトランザクション管理し、ファイル保存後に失敗した場合は生成済みファイルの削除を試みる。
- 例外は握りつぶさない。
- パスワードなどの機密情報はログに出力しない。
- ログには処理名、対象年月、ユーザーID、履歴IDなど調査に役立つ情報を残す。

## テストと確認

- JUnit 4.xを使用する。
- Service層の業務ロジックを中心に単体テストを追加する。
- 入力チェックは当面Service層の手書きバリデーションを維持し、Form制約や業務制約のテストを追加する。
- DAOはOracle DB接続を伴うため、必要に応じて接続確認手順をdocsに残す。
- 帳票処理は、ファイル生成有無と主要セル値を確認する。
- 変更後は可能な範囲で `mvn test` と `mvn package` を実行する。
- MavenがPATHにないなど環境理由で実行できない場合は、最終報告に明記する。

## STS / Eclipse互換性

- `Existing Maven Projects` としてインポートできる構成を維持する。
- `pom.xml` の packaging は `war` とする。
- `maven-compiler-plugin` の source / target は `1.8` とする。
- Servlet APIとJSP APIはTomcatが提供するため `provided` スコープにする。
- `.project`、`.classpath`、`.settings/`、`.springBeans`、`.sts4-cache/` はコミットしない。
- 空ディレクトリをGit管理したい場合は `.gitkeep` を置く。

## 禁止事項

- Spring Bootを使用しない。
- Gradleを使用しない。
- JPA / Hibernate / MyBatisを使用しない。
- React / Vue / Angularを使用しない。
- アプリケーション本体をDocker化しない。
- 開発用DB以外の目的でDocker Compose構成を広げない。
- Java 9以降のAPIを使用しない。
- Spring Framework、Apache POI、Oracle JDBC Driverを勝手に更新しない。
- SQLをControllerやJSPに書かない。
- IDE固有ファイルや生成ファイルをコミットしない。

## 権限制御の基本方針

- `ADMIN` は全ユーザーの帳票作成履歴を検索・詳細表示・再ダウンロードできる。
- `USER` は自分の月次報告書のみ出力できる。
- `USER` は自分が帳票対象者になっている帳票作成履歴のみ検索・詳細表示・再ダウンロードできる。
- ControllerだけでなくService層でも権限判断を行い、URL直接指定による参照を防ぐ。
- ログインユーザーを前提にするServiceメソッドは、`loginUser` 未指定時に例外を投げて安全側に倒す。

## ドキュメント更新ルール

機能実装後は、コードだけで終わらせず、関連ドキュメントも確認します。

| 変更内容 | 更新候補 |
|---|---|
| Controller / Service / DAO | `docs/architecture/`, `docs/walkthrough/` |
| SQL、DDL、DAO検索条件 | `docs/database/` |
| Excelテンプレート、帳票、履歴保存 | `docs/reporting/` |
| 画面デザイン | `docs/designs/` の画像確認、必要に応じてREADME |
| docs構成変更 | `docs/README.md`, ルート `README.md` |

## Codex作業時の進め方

1. 既存ファイルと関連docsを読む。
2. 変更範囲を小さく決める。
3. 既存の命名、パッケージ、JSP/CSSの雰囲気に合わせて実装する。
4. 可能な範囲でビルド、テスト、SQL、画面確認を行う。
5. 変更ファイル、確認結果、残ったリスクを簡潔に報告する。

重大な設計判断が必要な場合は、実装前にユーザーへ確認してください。
