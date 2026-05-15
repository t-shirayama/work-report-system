# AGENTS.md

このファイルは、Codexが本リポジトリで作業するときに毎回読む最小限のルールです。
詳細な設計・実装方針は [docs/README.md](docs/README.md) から辿ってください。

## プロジェクト目的

`work-report-system` は、作業日報登録と月次報告書Excel出力を行う、ポートフォリオ用のSpring MVC業務システムです。

古めのJava業務アプリ構成を理解・説明できることを重視します。

## 最重要制約

- Java 8で動作するコードにする。
- Spring Framework / Spring MVC は `4.3.17.RELEASE` を維持する。
- Maven WARプロジェクトとして扱い、Spring BootやGradleへ変更しない。
- Viewは JSP / JSTL を使用し、React / Vue / Angular は使用しない。
- アプリ本体はDocker化しない。実行は STS / Eclipse + Tomcat 8.5 + Maven WAR を前提にする。
- 開発用DBのみ Docker Compose のOracle Database Freeを使用してよい。
- DB、DDL、SQL、DAOはOracle Database前提で作成し、H2 / PostgreSQL / MySQL向けに寄せない。
- DBアクセスは原則 `NamedParameterJdbcTemplate` を使用し、JPA / Hibernate / MyBatis は使用しない。
- Excel出力は Apache POI `3.17` を使用し、テンプレートExcelへの差し込み方式を基本にする。
- Java 9以降のAPI、最新Spring、POI 3.17以外への更新は勝手に行わない。

## 作業前に読む場所

- 全体索引: [docs/README.md](docs/README.md)
- 開発ルール詳細: [docs/project/development-guidelines.md](docs/project/development-guidelines.md)
- Spring MVC: [docs/architecture/spring-mvc-basic.md](docs/architecture/spring-mvc-basic.md)
- レイヤ責務: [docs/architecture/controller-service-dao.md](docs/architecture/controller-service-dao.md)
- DB / SQL: [docs/database/spring-jdbc-basic.md](docs/database/spring-jdbc-basic.md)
- 帳票: [docs/reporting/excel-report-generation.md](docs/reporting/excel-report-generation.md)
- 実装の流れ: [docs/walkthrough/code-walkthrough.md](docs/walkthrough/code-walkthrough.md)

画面を変更する場合は、先に `docs/designs/` 配下の該当画像を確認してください。

## 実装ルール

- 既存コードとドキュメントを確認してから、指示範囲に絞って変更する。
- Controller、Service、DAOの責務を分離する。
- ControllerにSQLや重い業務ロジックを書かない。
- Serviceに業務判断、入力チェック、トランザクション境界を置く。
- DAOにSQL、バインド変数、RowMapper、DB例外に近い処理を置く。
- SQLはDAO層に明示し、ユーザー入力を文字列連結で埋め込まない。
- JSPは `src/main/webapp/WEB-INF/views/` 配下に置き、Controller経由で表示する。
- JSPではJSTLを使い、Javaスクリプトレットを増やさない。
- 共通処理は `common`、`util`、`exception` など既存パッケージの役割に合わせる。
- IDE固有ファイルや生成ファイルはコミットしない。

## ドキュメント更新

実装後は、変更内容に応じて関連ドキュメントを更新してください。

- Controller / Service / DAOを変更したら `docs/architecture/` と `docs/walkthrough/` を確認する。
- SQL、DDL、DAO検索条件を変更したら `docs/database/` を確認する。
- Excel出力、テンプレート、履歴保存を変更したら `docs/reporting/` を確認する。
- ドキュメント構成を変えたら `docs/README.md` と `README.md` の索引も更新する。

## 検証ルール

- 可能であれば `mvn test` と `mvn package` を実行する。
- MavenやDockerなどローカル環境都合で実行できない場合は、できなかった理由を最終報告に書く。
- DB関連の変更では、Oracle Database Free起動後のSQLまたは画面確認手順も示す。

## 判断に迷ったとき

- 技術スタックや大きな設計変更は、勝手に変更せずユーザーへ確認する。
- 影響が小さい不明点は、実装を止めずにREADMEやdocsの未決事項として整理する。
- 詳細ルールはAGENTS.mdへ追記しすぎず、原則として `docs/` 配下へ吸収して索引する。
