# AGENTS.md

このファイルは、Codexが本リポジトリで作業するときの開発ルールです。実装時は必ずこの内容を前提にしてください。

## 1. プロジェクトの目的

`work-report-system` は、日々の作業実績を登録し、月次報告書をExcel形式で自動作成するWebアプリケーションです。

ポートフォリオとして、古めのSpring MVC業務システム構成を理解し、実装できることを示す目的があります。

## 2. 使用技術と固定バージョン

以下の技術とバージョンを前提にします。Codexは、ユーザーから明示的な指示がない限り勝手に変更しないでください。

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

## 3. 実装方針

- Java 8で動作するコードを前提にする
- Spring Framework 4.3.17.RELEASE のSpring MVC構成にする
- Spring Bootは使用しない
- Maven WARプロジェクトにする
- `web.xml` を使用する
- Tomcat 8.5で動作するServlet/JSP APIを前提にする
- Controller、Service、DAOを分離する
- DBアクセスは原則として Spring JDBC の `NamedParameterJdbcTemplate` を使用する
- SQLはDAO層に明示的に記述する
- SQLの可読性を重視する
- `PreparedStatement` / バインド変数によるSQLインジェクション対策を意識する
- Excel帳票はApache POI 3.17で、テンプレートExcelに値を差し込む方式を前提にする
- JSPは `src/main/webapp/WEB-INF/views/` 配下に配置する
- IDE固有ファイルは原則コミットしない

## 4. パッケージ構成ルール

ルートパッケージは `com.example.workreport` とします。

基本構成は以下です。

```text
com.example.workreport
  common
  config
  controller
  dao
  dto
  entity
  form
  exception
  service
  util
```

ルールは以下です。

- Controllerは `controller` 配下に配置する
- Serviceは `service` 配下に配置する
- DAOは `dao` 配下に配置する
- Entity相当のDB行データは `entity` 配下に配置する
- 画面入力用のFormは `form` 配下に配置する
- レイヤ間で使うデータは `dto` 配下に配置する
- 共通処理は `common` 配下に配置する
- Javaベース設定を追加する場合は `config` 配下に配置する
- 汎用ユーティリティは `util` 配下に配置する
- 共通例外は `exception` 配下に配置する
- Excel帳票作成処理を追加する場合は、実装時に `report` パッケージを追加するか、既存パッケージ構成との整合を確認する

## 4.1 Maven / Spring MVC構成ルール

- `pom.xml` の packaging は `war` とする
- `maven-compiler-plugin` の source / target は `1.8` とする
- Servlet APIとJSP APIはTomcatが提供するため `provided` スコープにする
- Spring MVC設定は `src/main/webapp/WEB-INF/spring/dispatcher-servlet.xml` に置く
- アプリケーション共通設定は `src/main/webapp/WEB-INF/spring/applicationContext.xml` に置く
- DispatcherServletは `src/main/webapp/WEB-INF/web.xml` で定義する
- 静的ファイルは `src/main/webapp/resources/` 配下に配置する
- CSSは `src/main/webapp/resources/css/`、JavaScriptは `src/main/webapp/resources/js/` に配置する
- 空ディレクトリをGitで管理する必要がある場合は `.gitkeep` を使用する

## 5. Controller / Service / DAO の責務

### Controller

- HTTPリクエストを受け取る
- 入力値をFormとして受け取る
- 入力チェック結果を画面へ返す
- Serviceを呼び出す
- 画面遷移先のJSP名を返す
- 業務ロジックやSQLを直接書かない

### Service

- 業務ロジックを実装する
- トランザクション境界を管理する
- 複数DAOの呼び出しを組み合わせる
- 帳票作成処理の流れを制御する
- Controller固有の処理を持ち込まない

### DAO

- DBアクセスを担当する
- 原則として `NamedParameterJdbcTemplate` を使用する
- SQLを明示的に記述する
- SQLとパラメータの対応を読みやすく保つ
- 業務判断をDAOに寄せすぎない

## 6. JSPの作成ルール

- JSPは `src/main/webapp/WEB-INF/views/` 配下に配置する
- 直接URLでJSPへアクセスさせず、Controller経由で表示する
- JSTLを使用し、Javaコードのスクリプトレットは原則使用しない
- 共通ヘッダー、フッター、メニューは部品化を検討する
- 画面表示用の文言は必要に応じて `messages.properties` などに分離する
- 入力エラーは画面上で利用者が理解しやすい日本語で表示する

## 7. SQL作成ルール

- SQLはDAO層に明示的に記述する
- 可読性の高い改行とインデントを使用する
- テーブル別名は短くしすぎず、意味が分かるものにする
- 検索条件が増える場合でもSQLインジェクションが起きない実装にする
- 文字列連結でユーザー入力をSQLへ直接埋め込まない
- Oracleで動作するSQLを前提にする
- 日付、年月、ページングの扱いはOracleの仕様を意識する

## 8. Spring JDBCの使用ルール

- 原則として `NamedParameterJdbcTemplate` を使用する
- パラメータは `MapSqlParameterSource` などで明示的に渡す
- SELECT結果は `RowMapper` を使用してDTOへ変換する
- INSERT / UPDATE / DELETE の戻り件数を必要に応じて確認する
- 例外はSpringのDataAccessException系を前提に扱う
- DBアクセス処理をControllerへ直接書かない

## 9. Apache POIによるExcel出力ルール

- Apache POI 3.17を使用する
- 既存テンプレートExcelに値を差し込む方式を前提にする
- テンプレートファイルは `src/main/resources/report-template/` 配下に配置する想定とする
- セル位置は定数化し、マジックナンバーを増やしすぎない
- 帳票レイアウト変更に備えて、帳票作成処理をService本体から分離する
- 作成したExcelファイルは `generated-reports/` 配下へ保存する想定とする
- 作成履歴はDBに登録し、再ダウンロードできる設計にする
- ファイルストリームは必ず適切にクローズする

## 10. 例外処理ルール

- 想定内の入力エラーは画面に戻して利用者へ説明する
- DBエラーやファイル出力エラーはログに記録する
- 業務例外とシステム例外を分けて扱うことを検討する
- 例外を握りつぶさない
- Controllerで過度にtry-catchを書かず、共通例外ハンドリングを検討する

## 11. ログ出力ルール

- ログは障害調査に必要な粒度で出力する
- パスワードなどの機密情報をログに出力しない
- 例外発生時はスタックトレースを確認できる形にする
- 帳票作成開始、完了、失敗などの主要イベントを記録する
- ログファイルはコミットしない

## 12. テスト方針

- JUnit 4.xを使用する
- Service層の業務ロジックを中心に単体テストを作成する
- DAOはDB接続を伴うため、テスト方針を実装時に整理する
- 帳票作成処理は、作成結果のファイル有無や主要セルの値を確認する
- Java 8で実行可能なテストコードにする

## 13. コードコメント方針

- コメントは処理の意図が分かりにくい箇所に限定して書く
- 自明な代入やgetter/setterに説明コメントを付けない
- 業務ルール、帳票セル位置、SQLの意図は必要に応じて補足する
- 日本語コメントで問題ないが、簡潔で読みやすく書く

## 14. ドキュメント作成方針

実装とあわせて、以下の学習用ドキュメントを `docs/` 配下に作成・更新します。

- `docs/spring-mvc-basic.md`
- `docs/controller-service-dao.md`
- `docs/spring-jdbc-basic.md`
- `docs/apache-poi-basic.md`
- `docs/excel-report-generation.md`
- `docs/database-design.md`
- `docs/code-walkthrough.md`

機能を実装した後は、関連する解説ドキュメントも更新してください。たとえばDAOを追加した場合は `docs/spring-jdbc-basic.md` や `docs/controller-service-dao.md`、帳票出力を追加した場合は `docs/apache-poi-basic.md` や `docs/excel-report-generation.md` を更新対象として検討します。

## 15. STS / Eclipse 互換性ルール

- STS / Eclipse に `Existing Maven Projects` としてインポートできる構成にする
- Maven WARプロジェクトとして扱う
- Javaコンパイラレベルは1.8を前提にする
- Tomcat 8.5 Server Runtimeで動作する構成にする
- `.project`、`.classpath`、`.settings/` などのIDE固有ファイルは原則コミットしない
- Eclipse WTPで認識しやすい標準的な `src/main/webapp` 構成にする

## 16. 禁止事項

以下は禁止です。

- Spring Bootを使用しない
- Gradleを使用しない
- JPA / Hibernateを使用しない
- MyBatisを使用しない
- React / Vue / Angularを使用しない
- Docker前提のアプリ構成にしない
- Java 9以降のAPIを使用しない
- 最新Springへ勝手に更新しない
- Apache POIを3.17以外へ勝手に更新しない
- DBアクセスをORM化しない
- SQLをControllerやJSPに書かない
- JSPにJavaスクリプトレットを多用しない
- IDE固有ファイルや生成ファイルをコミットしない

## 17. Codexへの作業指示ルール

Codexが作業するときは、以下を守ってください。

- 作業前に既存ファイルを確認する
- ユーザーの指示範囲を超える実装を行わない
- 技術スタックや固定バージョンを勝手に変更しない
- Javaコード、JSP、XML、pom.xmlを作成する場合は、ユーザーから明示的に依頼された範囲に限定する
- 実装後は関連するREADMEまたはdocsの更新を検討する
- 変更後は可能な範囲でビルドやテストを実行する
- 不明点は作業を止めるのではなく、影響が小さい場合は未決事項として整理する
- 重大な設計判断が必要な場合は、実装前にユーザーへ確認する
