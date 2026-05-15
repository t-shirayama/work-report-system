# ドキュメント索引

このディレクトリは、`work-report-system` を学習・説明・実装するためのドキュメント置き場です。

まずは「おすすめの読む順番」に沿って読み、実装時は該当カテゴリのドキュメントを更新してください。

## おすすめの読む順番

1. [Spring MVC 基本構成](architecture/spring-mvc-basic.md)
2. [Controller / Service / DAO 構成](architecture/controller-service-dao.md)
3. [データベース設計](database/database-design.md)
4. [Oracle Docker Compose 開発DB](database/oracle-docker-setup.md)
5. [Spring JDBC 基本](database/spring-jdbc-basic.md)
6. [Apache POI 基本](reporting/apache-poi-basic.md)
7. [Excel帳票出力](reporting/excel-report-generation.md)
8. [コード walkthrough](walkthrough/code-walkthrough.md)

## カテゴリ別一覧

### architecture

Spring MVCアプリケーション全体の流れと、レイヤ構成を理解するための資料です。

| ドキュメント | 内容 |
|---|---|
| [Spring MVC 基本構成](architecture/spring-mvc-basic.md) | DispatcherServlet、Controller、ViewResolver、JSP表示の流れ |
| [Controller / Service / DAO 構成](architecture/controller-service-dao.md) | 各レイヤの責務、DTOとEntity、例外処理とログ出力 |

### database

Oracle Database、DDL、Spring JDBC、ローカル開発DBに関する資料です。

| ドキュメント | 内容 |
|---|---|
| [データベース設計](database/database-design.md) | テーブル定義、ER図、インデックス、DAO実装との対応 |
| [Oracle Docker Compose 開発DB](database/oracle-docker-setup.md) | Docker ComposeによるOracle Database Freeの起動、初期化、接続確認 |
| [Spring JDBC 基本](database/spring-jdbc-basic.md) | NamedParameterJdbcTemplate、バインド変数、SQLとDTOの対応 |

### reporting

Apache POIとExcel帳票出力に関する資料です。

| ドキュメント | 内容 |
|---|---|
| [Apache POI 基本](reporting/apache-poi-basic.md) | Workbook、Sheet、Row、Cell、CellStyle、テンプレート方式 |
| [Excel帳票出力](reporting/excel-report-generation.md) | 月次報告書Excel出力、履歴保存、再ダウンロード、例外処理 |

### walkthrough

実装済みコードを画面操作の流れに沿って読むための資料です。

| ドキュメント | 内容 |
|---|---|
| [コード walkthrough](walkthrough/code-walkthrough.md) | ログイン、日報登録、検索、帳票出力、履歴、ダッシュボードの処理フロー |

### designs

画面実装・調整時に参照するデザイン画像です。

| ファイル | 用途 |
|---|---|
| `designs/ログイン画面.png` | ログイン画面 |
| `designs/ダッシュボード.png` | ダッシュボード |
| `designs/作業日報登録.png` | 作業日報登録 |
| `designs/作業実績登録.png` | 作業実績検索 |
| `designs/帳票出力・履歴.png` | 帳票出力・履歴 |
| `designs/帳票1.png` から `designs/帳票5.png` | 帳票レイアウト案 |

## 更新ルール

- Controller / Service / DAOを追加・変更した場合は、`architecture/` または `walkthrough/` を更新します。
- SQL、DDL、DAOの検索条件を変更した場合は、`database/` を更新します。
- Excelテンプレートや帳票出力処理を変更した場合は、`reporting/` を更新します。
- 画面デザインを変更する場合は、`designs/` の該当画像との差分を確認します。
- READMEには概要と導線を置き、詳細説明はこの `docs/README.md` から辿れるようにします。
