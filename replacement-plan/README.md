# リプレイス計画書

このフォルダは、現行の `work-report-system` を React、C# ASP.NET Core Web API、SQL Server 構成へリプレイスするための計画書です。

## リプレイス方針

| 分類 | 現行 | リプレイス後 |
|---|---|---|
| フロントエンド | JSP / JSTL / Spring MVC画面 | React |
| バックエンド | Java 8 / Spring MVC / Spring JDBC | C# / ASP.NET Core Web API |
| データベース | Oracle Database | SQL Server |
| 帳票出力 | Apache POI 3.17 / Excelテンプレート | C#側Excelライブラリ + 既存テンプレート互換を優先 |
| 認証 | Spring Security / Form Login / Session / CSRF | ASP.NET Core認証。初期案はCookie認証 + Antiforgery |
| 実行形態 | Tomcat 8.5 / Maven WAR | React SPA + ASP.NET Core API |

## 目的

- 現行機能、画面、業務ルール、DB、帳票、認証認可を棚卸しする。
- React、ASP.NET Core Web API、SQL Serverへの移行単位を明確にする。
- 現行仕様を再現しながら、API境界、データ移行、帳票互換、テスト、リリース、ロールバックを計画する。
- 未決事項とリスクを見える形で管理し、実装前に判断が必要な論点を整理する。

## 対象範囲

| 区分 | 対象 |
|---|---|
| 画面 | ホーム、ログイン、ダッシュボード、作業日報登録、登録完了、作業実績検索、月次報告書出力、帳票作成履歴、帳票履歴詳細 |
| API | 認証、ユーザー情報、ダッシュボード、作業日報登録、作業実績検索、月次帳票出力、帳票履歴検索、帳票詳細、再ダウンロード |
| DB | `departments`, `users`, `work_reports`, `report_output_histories` |
| 帳票 | `MONTHLY_WORK_REPORT` 月次作業報告書 `.xlsx` |
| 運用 | 帳票ファイル保存、履歴ステータス、ログ、バックアップ、リリース、ロールバック |

## 計画書一覧

| ファイル | 内容 |
|---|---|
| [01-current-state.md](01-current-state.md) | 現行機能、画面、DB、帳票、認証認可の棚卸し |
| [02-target-architecture.md](02-target-architecture.md) | React、ASP.NET Core Web API、SQL Serverの目標アーキテクチャ |
| [03-data-migration.md](03-data-migration.md) | OracleからSQL ServerへのDDL、SQL、データ移行計画 |
| [04-api-design.md](04-api-design.md) | Web API候補、認証認可、エラー形式、移行順序 |
| [05-frontend-migration.md](05-frontend-migration.md) | JSPからReactへの画面移行計画 |
| [06-reporting-migration.md](06-reporting-migration.md) | Excel帳票出力、履歴保存、再ダウンロードの移行計画 |
| [07-test-release-rollback.md](07-test-release-rollback.md) | テスト、リリース、並行稼働、ロールバック計画 |
| [08-decisions-risks.md](08-decisions-risks.md) | 未決事項、リスク、移行チェックリスト |

## 推奨移行順序

1. 現行仕様の凍結範囲を決める。
2. SQL Server版DDLとデータ移行リハーサルを作る。
3. ASP.NET Core Web APIの認証、ユーザー情報、共通エラー形式を作る。
4. Reactの認証画面、共通レイアウト、ルーティングを作る。
5. ダッシュボード、作業日報登録、作業実績検索を移行する。
6. 月次帳票出力、帳票履歴、再ダウンロードを移行する。
7. 現行Excel、DB集計、権限制御を突き合わせる。
8. 検証環境で移行リハーサル、切替、ロールバックを確認する。

## 前提

- この計画書は現行Spring MVC実装をただちに変更するものではありません。
- 現行仕様の参照元は `README.md`、`docs/README.md`、`src/main/java`、`src/main/webapp/WEB-INF/views`、`src/main/resources/sql` です。
- 認証方式、ORM採用有無、帳票ライブラリ、ホスティング方式などは未決事項として扱い、実装開始前に確定します。
