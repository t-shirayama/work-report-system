# データベース設計

このドキュメントは、`work-report-system` のDB設計を整理したものです。

想定DBは **Oracle Database 11g / 12c / 19c** です。DDLとサンプルデータはOracle向けSQLとして作成しています。JPA / Hibernate / MyBatisなどのORMやSQLマッパーは前提にせず、今後Spring JDBCから明示的なSQLでアクセスする想定です。

本番・案件想定のDBはOracle Databaseとし、JDBCドライバはOracle JDBC Driver 19.17.0.0、Maven依存関係は `ojdbc8:19.17.0.0` を使用します。

開発用DBは、ローカル環境構築を簡単にするためDocker ComposeでOracle Database FreeまたはOracle Database XEを起動する構成にしてよいものとします。ただし、アプリケーション本体はDocker化せず、STS + Tomcat 8.5 + Maven WARで起動します。

SQL、DDL、DAO実装はOracle Database前提で作成し、H2 / PostgreSQL / MySQL向けに寄せません。

このDB設計は、Spring JDBCで明示的にSQLを書く方針に合わせています。テーブル同士の関係、主キー、外部キー、検索条件に使う列を確認しながらDAO実装を読むと、SQLと画面機能のつながりを把握できます。

開発用DBの起動手順は `docs/database/oracle-docker-setup.md` にまとめています。Docker Composeで起動するOracle Database Freeの接続先は `jdbc:oracle:thin:@//localhost:1521/FREEPDB1` です。

## テーブル一覧

| テーブル名 | 概要 |
|---|---|
| `departments` | 部署マスタ |
| `users` | ログインユーザー情報 |
| `work_reports` | 日々の作業実績 |
| `report_output_histories` | 月次報告書の出力履歴 |

## 開発用DB環境

開発用DBは `docker-compose.yml` でOracle Database Freeを起動します。

| 項目 | 値 |
|---|---|
| Docker service | `oracle-db` |
| Image | `gvenzl/oracle-free:23-slim` |
| Host | `localhost` |
| Port | `1521` |
| Service | `FREEPDB1` |
| User | `work_report` |
| Password | `work_report` |

初回起動時に `docker/oracle/init/01-init-work-report.sql` が実行され、`src/main/resources/sql/schema.sql` と `src/main/resources/sql/sample-data.sql` が `work_report` スキーマへ投入されます。

## ER図のテキスト表現

```text
departments 1 ---- * users
departments 1 ---- * work_reports
users       1 ---- * work_reports
users       1 ---- * report_output_histories
```

関係は以下です。

- 1つの部署には複数のユーザーが所属します。
- 1つの部署には複数の作業日報が紐づきます。
- 1人のユーザーは複数の作業日報を登録できます。
- 1人のユーザーは複数の月次報告書出力履歴を作成できます。

作業実績検索や月次報告書出力では、`work_reports` を中心に `users` と `departments` をJOINします。日報データにユーザーIDと部署IDを持たせることで、社員名や部署名を検索結果や帳票に表示できます。

## 各テーブルのカラム定義

### departments

部署マスタです。ユーザー所属部署や作業日報の部署情報として参照します。

| カラム名 | 型 | NULL | 説明 |
|---|---|---|---|
| `department_id` | `NUMBER(10)` | 不可 | 部署ID |
| `department_code` | `VARCHAR2(20)` | 不可 | 部署コード |
| `department_name` | `VARCHAR2(100)` | 不可 | 部署名 |
| `display_order` | `NUMBER(5)` | 不可 | 表示順 |
| `created_at` | `DATE` | 不可 | 作成日時 |
| `updated_at` | `DATE` | 不可 | 更新日時 |

### users

ログインユーザー情報です。社員名、ログインID、パスワード、部署、権限を保持します。

| カラム名 | 型 | NULL | 説明 |
|---|---|---|---|
| `user_id` | `NUMBER(10)` | 不可 | ユーザーID |
| `department_id` | `NUMBER(10)` | 不可 | 所属部署ID |
| `login_id` | `VARCHAR2(50)` | 不可 | ログインID |
| `password` | `VARCHAR2(255)` | 不可 | パスワード |
| `employee_name` | `VARCHAR2(100)` | 不可 | 社員名 |
| `role_code` | `VARCHAR2(20)` | 不可 | 権限コード |
| `created_at` | `DATE` | 不可 | 作成日時 |
| `updated_at` | `DATE` | 不可 | 更新日時 |

`role_code` は当面 `ADMIN` または `USER` を想定します。

サンプルデータでは動作確認用のダミーパスワード文字列を登録しています。運用環境ではハッシュ化したパスワードを保存する方針に変更します。

### work_reports

日々の作業実績です。作業日、ユーザー、部署、プロジェクト名、作業分類、作業時間、作業内容を保持します。

| カラム名 | 型 | NULL | 説明 |
|---|---|---|---|
| `work_report_id` | `NUMBER(10)` | 不可 | 作業日報ID |
| `user_id` | `NUMBER(10)` | 不可 | 登録ユーザーID |
| `department_id` | `NUMBER(10)` | 不可 | 部署ID |
| `work_date` | `DATE` | 不可 | 作業日 |
| `project_name` | `VARCHAR2(100)` | 不可 | プロジェクト名 |
| `work_category` | `VARCHAR2(30)` | 不可 | 作業分類 |
| `work_hours` | `NUMBER(4,2)` | 不可 | 作業時間 |
| `work_content` | `VARCHAR2(1000)` | 不可 | 作業内容 |
| `created_at` | `DATE` | 不可 | 作成日時 |
| `updated_at` | `DATE` | 不可 | 更新日時 |

`work_category` は当面以下を想定します。

| 値 | 意味 |
|---|---|
| `DESIGN` | 設計 |
| `DEVELOPMENT` | 開発 |
| `TEST` | テスト |
| `MEETING` | 会議 |
| `DOCUMENT` | 資料作成 |
| `OTHER` | その他 |

### report_output_histories

月次報告書の出力履歴です。対象年月、作成者、ファイル名、ファイルパス、ステータス、エラーメッセージを保持します。

| カラム名 | 型 | NULL | 説明 |
|---|---|---|---|
| `report_output_history_id` | `NUMBER(10)` | 不可 | 帳票出力履歴ID |
| `target_year_month` | `VARCHAR2(6)` | 不可 | 対象年月。`YYYYMM`形式 |
| `created_by` | `NUMBER(10)` | 不可 | 作成者ユーザーID |
| `report_type` | `VARCHAR2(50)` | 不可 | 帳票種別 |
| `file_name` | `VARCHAR2(255)` | 不可 | 作成ファイル名 |
| `file_path` | `VARCHAR2(500)` | 不可 | 作成ファイルパス |
| `status` | `VARCHAR2(20)` | 不可 | 出力ステータス |
| `error_message` | `VARCHAR2(1000)` | 可 | エラーメッセージ |
| `created_at` | `DATE` | 不可 | 作成日時 |
| `updated_at` | `DATE` | 不可 | 更新日時 |

`status` は当面以下を想定します。

| 値 | 意味 |
|---|---|
| `SUCCESS` | 作成成功。画面表示は「完了」 |
| `ERROR` | 作成失敗 |
| `PROCESSING` | 作成中。画面表示は「処理中」 |

## 主キー・外部キー

### 主キー

| テーブル名 | 主キー |
|---|---|
| `departments` | `department_id` |
| `users` | `user_id` |
| `work_reports` | `work_report_id` |
| `report_output_histories` | `report_output_history_id` |

### 外部キー

| 制約名 | 参照元 | 参照先 |
|---|---|---|
| `fk_users_department` | `users.department_id` | `departments.department_id` |
| `fk_work_reports_user` | `work_reports.user_id` | `users.user_id` |
| `fk_work_reports_department` | `work_reports.department_id` | `departments.department_id` |
| `fk_report_histories_user` | `report_output_histories.created_by` | `users.user_id` |

## インデックス方針

検索や一覧表示で使用頻度が高い項目にインデックスを付与します。

| インデックス名 | 対象 | 目的 |
|---|---|---|
| `uk_departments_code` | `departments.department_code` | 部署コードの一意性確保 |
| `uk_users_login_id` | `users.login_id` | ログインID検索と一意性確保 |
| `idx_users_department_id` | `users.department_id` | 部署別ユーザー検索 |
| `idx_work_reports_user_date` | `work_reports.user_id, work_reports.work_date` | ユーザー別・日付範囲検索 |
| `idx_work_reports_department_date` | `work_reports.department_id, work_reports.work_date` | 部署別・日付範囲検索 |
| `idx_work_reports_project_name` | `work_reports.project_name` | プロジェクト名検索 |
| `idx_report_histories_ym` | `report_output_histories.target_year_month` | 対象年月別の履歴検索 |
| `idx_report_histories_created_by` | `report_output_histories.created_by` | 作成者別の履歴検索 |

作業実績検索では、対象年月、ユーザー、部署、プロジェクト名を条件にする想定です。実装後の検索条件や実行計画に応じて、複合インデックスは見直します。

インデックスは、DBが目的の行を探しやすくするための仕組みです。すべての列に付ければよいものではなく、検索条件、JOIN条件、並び替えでよく使う列を中心に検討します。

## サンプルデータの説明

サンプルデータは `src/main/resources/sql/sample-data.sql` に定義しています。

内容は以下です。

| テーブル名 | 件数 | 内容 |
|---|---:|---|
| `departments` | 3件 | 開発部、品質管理部、プロジェクト管理部 |
| `users` | 5件 | 管理者1件、一般ユーザー4件 |
| `work_reports` | 32件 | 2026年5月の作業日報 |
| `report_output_histories` | 5件 | 2026年4月、2026年5月の帳票出力履歴 |

サンプルデータの用途は以下です。

- ログインユーザー一覧の確認
- 部署別の検索確認
- 対象年月別の作業実績検索確認
- 月次報告書の集計処理確認
- 帳票出力履歴一覧と再ダウンロード処理の確認

## DDLファイル

DDLは `src/main/resources/sql/schema.sql` に定義しています。

主な方針は以下です。

- Oracle向けSQLとして作成する
- H2 / PostgreSQL / MySQL向けではなく、Oracle Database前提で作成する
- IDは `NUMBER(10)` を基本にする
- 日付は `DATE` を基本にする
- 作業時間は `NUMBER(4,2)` とする
- 文字列は `VARCHAR2` を使用する
- 作成日時、更新日時は各テーブルに持たせる
- Oracle 11gも想定し、Identity列ではなくシーケンスを用意する
- サンプルデータ投入後のID衝突を避けるため、シーケンスは `1001` から開始する
- 論理削除カラムは今回は持たせない

## DAO実装との対応

| テーブル | 主に参照・更新するDAO | 主な用途 |
|---|---|---|
| `departments` | `UserDao`, `WorkReportDao`, `MonthlyReportDao` | 部署名表示、検索、帳票出力 |
| `users` | `UserDao`, `WorkReportDao`, `MonthlyReportDao`, `ReportHistoryDao` | ログイン認証、社員名表示、作成者表示 |
| `work_reports` | `DashboardDao`, `WorkReportDao`, `MonthlyReportDao` | ダッシュボード集計、日報登録、作業実績検索、月次集計 |
| `report_output_histories` | `DashboardDao`, `ReportHistoryDao` | ダッシュボード集計、帳票作成履歴登録、検索、詳細表示、再ダウンロード |

DAOでは、これらのテーブルを機能に合わせてJOINし、EntityまたはDTOへ変換します。たとえば `ReportHistoryDao` は履歴テーブルとユーザーテーブルをJOINし、一覧表示に必要な作成者名を含む `ReportHistoryDto` を作成します。

## 例外とデータ整合性

外部キー制約により、存在しないユーザーIDや部署IDを持つ作業日報は登録できません。アプリケーション側でもセッション中の `loginUser` から `userId` と `departmentId` を取得して登録しますが、DB側の制約も最後の防御線として機能します。

DB制約違反や接続エラーが発生した場合は、利用者には分かりやすいエラーメッセージを表示し、ログには調査に必要な情報を残す方針です。

## 今後の拡張案

- パスワードハッシュ化方式に合わせたカラム名や認証項目の見直し
- ユーザーの有効・無効フラグ追加
- 作業分類をマスタテーブル化
- 権限をロールマスタとして分離
- 月次締め状態を管理するテーブル追加
- 添付ファイルや帳票テンプレート管理テーブルの追加
- 更新者ID、作成者IDの監査カラム追加
- 論理削除フラグの追加
- 作業時間の丸めルールや休暇区分の追加
- 実データ量に応じたインデックス再設計
