# データベース設計

このドキュメントは、`work-report-system` のDB設計を整理したものです。

想定DBは **Oracle Database 11g / 12c / 19c** です。DDLとサンプルデータはOracle向けSQLとして作成しています。JPA / Hibernate / MyBatisなどのORMやSQLマッパーは前提にせず、今後Spring JDBCから明示的なSQLでアクセスする想定です。

本番・案件想定のDBはOracle Databaseとし、JDBCドライバはOracle JDBC Driver 19.17.0.0、Maven依存関係は `ojdbc8:19.17.0.0` を使用します。

ポートフォリオの開発用DBは、ローカル環境構築を簡単にするためDocker ComposeでOracle Database FreeまたはOracle Database XEを起動する構成にしてよいものとします。ただし、アプリケーション本体はDocker化せず、STS + Tomcat 8.5 + Maven WARで起動します。

SQL、DDL、DAO実装はOracle Database前提で作成し、H2 / PostgreSQL / MySQL向けに寄せません。

## テーブル一覧

| テーブル名 | 概要 |
|---|---|
| `departments` | 部署マスタ |
| `users` | ログインユーザー情報 |
| `work_reports` | 日々の作業実績 |
| `report_output_histories` | 月次報告書の出力履歴 |

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

サンプルデータでは分かりやすさを優先してダミーパスワード文字列を登録しています。実装時はハッシュ化したパスワードを保存する方針に変更します。

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
| `file_name` | `VARCHAR2(255)` | 不可 | 作成ファイル名 |
| `file_path` | `VARCHAR2(500)` | 不可 | 作成ファイルパス |
| `status` | `VARCHAR2(20)` | 不可 | 出力ステータス |
| `error_message` | `VARCHAR2(1000)` | 可 | エラーメッセージ |
| `created_at` | `DATE` | 不可 | 作成日時 |
| `updated_at` | `DATE` | 不可 | 更新日時 |

`status` は当面以下を想定します。

| 値 | 意味 |
|---|---|
| `SUCCESS` | 作成成功 |
| `FAILED` | 作成失敗 |
| `PROCESSING` | 作成中 |

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
