# Oracle Docker Compose 開発DB

このドキュメントでは、開発用Oracle Database FreeをDocker Composeで起動し、`work-report-system` のDDLとサンプルデータを投入する手順を説明します。

アプリケーション本体はDocker化しません。アプリケーションはこれまでどおり、STS + Tomcat 8.5 + Maven WARで起動します。Docker Composeはローカル開発用DBを用意する補助用途です。

## 使用するイメージ

```text
gvenzl/oracle-free:23-slim
```

Oracle Database FreeのPDBサービス名は `FREEPDB1` を使用します。

## 接続情報

| 項目 | 値 |
|---|---|
| Host | `localhost` |
| Port | `1521` |
| Service | `FREEPDB1` |
| App User | `work_report` |
| App Password | `work_report` |
| JDBC URL | `jdbc:oracle:thin:@//localhost:1521/FREEPDB1` |

管理ユーザー用のパスワードは、開発用として `oracle_admin` を設定しています。共有環境や外部公開環境では、リポジトリに固定パスワードを置かない運用にしてください。

## 起動手順

リポジトリのルートディレクトリで以下を実行します。

```powershell
docker compose up -d oracle-db
```

初回起動時はOracleイメージの取得とDB作成に時間がかかります。起動状況は以下で確認します。

```powershell
docker compose ps
```

`oracle-db` が `healthy` になれば、DBの起動と初期化が完了しています。

## 初期化される内容

`docker-compose.yml` では、以下のSQLをコンテナにマウントしています。

```text
src/main/resources/sql/schema.sql
src/main/resources/sql/sample-data.sql
```

初回DB作成時に `docker/oracle/init/01-init-work-report.sql` が実行され、`work_report` スキーマにDDLとサンプルデータを投入します。

作成される主なテーブルは以下です。

- `departments`
- `users`
- `work_reports`
- `report_output_histories`

## 接続確認

SQL*Plusで接続確認する場合は、以下を実行します。

```powershell
docker compose exec oracle-db sqlplus -L work_report/work_report@//localhost:1521/FREEPDB1
```

接続後、以下のSQLでサンプルデータを確認できます。

```sql
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM work_reports;
EXIT;
```

PowerShellから一度に確認する場合は、以下のように実行できます。

```powershell
"SELECT COUNT(*) AS USER_COUNT FROM users;`nSELECT COUNT(*) AS REPORT_COUNT FROM work_reports;`nEXIT;" | docker compose exec -T oracle-db sqlplus -L work_report/work_report@//localhost:1521/FREEPDB1
```

## アプリケーションからの接続

`src/main/resources/application.properties` は、Docker Composeで起動したOracle Database Freeに接続する設定です。

```properties
jdbc.driverClassName=oracle.jdbc.OracleDriver
jdbc.url=jdbc:oracle:thin:@//localhost:1521/FREEPDB1
jdbc.username=work_report
jdbc.password=work_report
```

STS上のTomcat 8.5でアプリケーションを起動すると、この接続先を使用してSpring JDBCからOracleへアクセスします。

## DBを作り直す場合

開発用DBを初期状態から作り直す場合は、以下を実行します。

```powershell
docker compose down -v
docker compose up -d oracle-db
```

`down -v` はDocker volumeを削除します。登録済みデータも消えるため、必要なデータがないことを確認してから実行してください。

## 注意点

- Docker Composeは開発用DBの補助用途に限定します。
- アプリケーション本体はDocker化しません。
- SQL、DDL、DAO実装はOracle Database前提のままにします。
- H2、PostgreSQL、MySQL向けの互換SQLには寄せません。
