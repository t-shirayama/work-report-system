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

```sh
docker compose up -d --build
```

`docker-compose.yml` の `db-init` サービスが `WorkReport` データベースを作成し、`schema.sql` と `seed.sql` を投入します。

`db-init` は `departments` テーブルが存在する場合、既に初期化済みと判断してDDL/seed投入をスキップします。

初期データは `DB_SEED_MODE` で切り替えます。

| 値 | 内容 |
|---|---|
| `sample` | 部署、ユーザー、日報、帳票履歴のサンプルを投入 |
| `empty` | 管理者ログインに必要な最小マスタだけ投入 |
| `none` | DDLのみ投入 |

DBを作り直して再投入する場合:

```sh
docker compose --profile reset run --rm db-reset
docker compose up -d --build
```

初期データなし相当で作り直す場合:

```sh
DB_SEED_MODE=empty docker compose --profile reset run --rm db-reset
docker compose up -d --build
```

既定のSAパスワードは `docker-compose.yml` の `MSSQL_SA_PASSWORD` を参照してください。
