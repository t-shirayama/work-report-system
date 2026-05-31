# DB設計

DBは SQL Server を前提にします。DDLは [schema.sql](../database/sqlserver/schema.sql)、サンプルデータは [seed.sql](../database/sqlserver/seed.sql) です。

## テーブル

| テーブル | 用途 |
|---|---|
| `departments` | 部署マスタ |
| `users` | 利用者、認証情報、権限 |
| `work_reports` | 作業日報 |
| `report_output_histories` | 帳票出力履歴 |

## 主な制約

- `users.login_id` は一意。
- `users.role_code` は `ADMIN` または `USER`。
- `work_reports.work_category` は `DESIGN`, `DEVELOPMENT`, `TEST`, `MEETING`, `DOCUMENT`, `OTHER`。
- `work_reports.work_hours` は 0 より大きく 24 以下。
- `report_output_histories.status` は `SUCCESS`, `ERROR`, `PROCESSING`。
- `report_output_histories.target_year_month` は6桁の年月。

## SQL実装方針

- SQLはRepositoryに集約する。
- ユーザー入力はDapperパラメータでバインドする。
- 日付範囲、部分一致、権限制御条件はRepositoryで明示する。
- 集計SQLはSQL Server関数を使用する。

## ローカルDB

```powershell
docker compose up -d
```

既定のSAパスワードは `docker-compose.yml` の `MSSQL_SA_PASSWORD` を参照してください。
