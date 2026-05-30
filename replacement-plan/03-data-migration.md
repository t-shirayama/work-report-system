# 03. データ移行計画

## 対象テーブル

| テーブル | 役割 | 移行優先度 |
|---|---|---|
| `departments` | 部署マスタ | 高 |
| `users` | ログインユーザー、権限、パスワード | 高 |
| `work_reports` | 作業日報 | 高 |
| `report_output_histories` | 帳票履歴 | 高 |

## 型変換方針

| Oracle | SQL Server案 | 備考 |
|---|---|---|
| `NUMBER(10)` | `int` | ID列 |
| `NUMBER(5)` | `smallint` または `int` | 表示順 |
| `NUMBER(4,2)` | `decimal(4,2)` | 作業時間 |
| `VARCHAR2(n)` | `nvarchar(n)` | 日本語保持を前提 |
| `DATE` | `date` | `work_date` |
| `DATE` | `datetime2(0)` または `datetime2(3)` | `created_at`, `updated_at` |

## 採番方針

現行はOracle `SEQUENCE` と `NEXTVAL` を使っています。SQL Serverでは以下のどちらかを採用します。

| 案 | 内容 | 注意 |
|---|---|---|
| `IDENTITY` | 各主キーを `IDENTITY` にする | 既存ID移行後にシード値を最大IDより大きく調整 |
| `SEQUENCE` | SQL Server `SEQUENCE` を使う | 現行の「先にIDを取得」する処理に近いが実装が増える |

初期案は `IDENTITY` です。ただし、帳票ファイル名に履歴IDを含めるため、移行後もIDの一意性と連続採番開始値を必ず検証します。

## SQL Server版DDL方針

| 現行制約 | SQL Server方針 |
|---|---|
| `role_code IN ('ADMIN', 'USER')` | `CHECK` 制約で維持 |
| `work_category IN (...)` | `CHECK` 制約で維持。将来マスタ化は別判断 |
| `work_hours > 0 AND work_hours <= 24` | `CHECK` 制約で維持 |
| `status IN ('SUCCESS', 'ERROR', 'PROCESSING')` | `CHECK` 制約で維持 |
| `report_type IN ('MONTHLY_WORK_REPORT')` | `CHECK` 制約で維持 |
| `REGEXP_LIKE(target_year_month, '^[0-9]{6}$')` | `LIKE '[0-9][0-9][0-9][0-9][0-9][0-9]'` などで代替 |

## Oracle固有SQLの変換

| 分類 | Oracle | SQL Server |
|---|---|---|
| 現在日時 | `SYSDATE` | `SYSDATETIME()` |
| 月初 | `TRUNC(SYSDATE, 'MM')` | `DATEFROMPARTS(YEAR(SYSDATETIME()), MONTH(SYSDATETIME()), 1)` |
| 月加算 | `ADD_MONTHS` | `DATEADD(month, n, date)` |
| NULL代替 | `NVL` | `COALESCE` または `ISNULL` |
| 日付文字列 | `TO_CHAR` | API/Reactで整形を優先 |
| 日本語曜日 | `TO_CHAR(..., 'DY', 'NLS_DATE_LANGUAGE=JAPANESE')` | C#またはReactで曜日名を生成 |
| 文字列連結 | `||` | `CONCAT()` |
| 件数制限 | `ROWNUM <= n` | `TOP (n)` または `OFFSET/FETCH` |
| シーケンス | `seq_xxx.NEXTVAL FROM dual` | `IDENTITY` または `NEXT VALUE FOR` |

## 移行手順案

1. 現行Oracleからテーブル定義、制約、インデックス、件数を取得する。
2. SQL Server版DDLを作成する。
3. 初期データ投入スクリプトを作成する。
4. OracleからCSVまたは移行ツールでデータを抽出する。
5. SQL Serverへステージング投入する。
6. 型、文字コード、日付、小数、NULLを検証する。
7. 本テーブルへ投入する。
8. PK/FK、CHECK、UNIQUE、INDEXを有効化する。
9. 採番シード値を最大IDより後ろへ調整する。
10. アプリから登録、検索、帳票出力を確認する。

## 検証観点

| 観点 | 確認内容 |
|---|---|
| 件数 | 4テーブルの移行前後件数一致 |
| 主キー | ID重複なし、NULLなし |
| 外部キー | 部署、ユーザー、作成者、対象者の参照整合性 |
| 日本語 | 部署名、社員名、作業内容、エラーメッセージの文字化けなし |
| 日付 | `work_date` は日付のみ、作成更新日時は時刻を保持 |
| 小数 | `3.50`, `7.50` などの作業時間が丸められない |
| 制約 | ロール、分類、ステータス、対象年月、不正作業時間を拒否 |
| 集計 | 月次合計、分類別合計、日別明細、未出力件数が現行と一致 |
| 権限 | 一般ユーザーが自分以外のデータを取得できない |
| 帳票 | SQL Server移行後のExcel出力が現行と一致 |

## リスク

- SQLの表示整形をSQL Serverへ機械変換すると、APIとReactの責務が曖昧になります。
- `IDENTITY` のシード調整漏れはID衝突につながります。
- `varchar` を使うと日本語文字化けのリスクがあります。原則 `nvarchar` を使います。
- Oracle `DATE` は時刻も保持できるため、SQL Serverの `date` / `datetime2` への分離判断が必要です。
- 帳票履歴の `file_path` は環境依存になりやすいため、移行時に保存先設計を見直します。
