# Spring JDBC 基本

このドキュメントでは、ログイン機能を例にして、Spring JDBCの使い方を説明します。

本プロジェクトでは、DBアクセスにSpring JDBCの `NamedParameterJdbcTemplate` を使用します。JPA / Hibernate / MyBatisは使用しません。

Spring JDBCは、Java標準のJDBCを使いやすくするためのSpringの機能です。SQLを自分で書く方針はそのままに、接続、パラメータ設定、結果取得、例外変換などの定型処理を簡潔に書けます。

## DB接続設定

DB接続情報は `src/main/resources/application.properties` に定義します。

```properties
jdbc.driverClassName=${JDBC_DRIVER_CLASS_NAME:oracle.jdbc.OracleDriver}
jdbc.url=${JDBC_URL:jdbc:oracle:thin:@//localhost:1521/FREEPDB1}
jdbc.username=${JDBC_USERNAME:work_report}
jdbc.password=${JDBC_PASSWORD:work_report}
```

この設定は開発用のデフォルトです。環境変数で上書きできるようにしています。STS/Tomcatで接続先を変更する場合は、起動構成に `JDBC_URL`、`JDBC_USERNAME`、`JDBC_PASSWORD` などを設定します。Docker Compose側のDBユーザーや管理者パスワードを変更する場合は、`.env.example` をコピーして `.env` を作成します。`.env` はコミットしません。

## Bean定義

`applicationContext.xml` では、`DataSource` と `NamedParameterJdbcTemplate` を定義しています。

```xml
<bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
    <property name="driverClassName" value="${jdbc.driverClassName}"/>
    <property name="url" value="${jdbc.url}"/>
    <property name="username" value="${jdbc.username}"/>
    <property name="password" value="${jdbc.password}"/>
</bean>

<bean id="namedParameterJdbcTemplate" class="org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate">
    <constructor-arg ref="dataSource"/>
</bean>
```

この構成では開発用として `DriverManagerDataSource` を使用しています。継続運用する環境では、TomcatのJNDI DataSourceや接続プールを使ってコネクション管理を行います。

TomcatでJNDI DataSourceを使う場合の構成例は以下です。

```xml
<beans xmlns:jee="http://www.springframework.org/schema/jee"
       xsi:schemaLocation="
           http://www.springframework.org/schema/jee http://www.springframework.org/schema/jee/spring-jee.xsd">

<jee:jndi-lookup id="dataSource" jndi-name="java:comp/env/jdbc/workReportDataSource"/>
</beans>
```

Tomcat側では、たとえば `context.xml` に以下のようなResourceを定義します。

```xml
<Resource name="jdbc/workReportDataSource"
          auth="Container"
          type="javax.sql.DataSource"
          driverClassName="oracle.jdbc.OracleDriver"
          url="${JDBC_URL}"
          username="${JDBC_USERNAME}"
          password="${JDBC_PASSWORD}"
          maxTotal="20"
          maxIdle="5"
          maxWaitMillis="10000"/>
```

この場合、Tomcat側の `context.xml` などで `jdbc/workReportDataSource` を定義し、アプリケーション側には接続先やパスワードを直接持たせない方針にします。

切り替える場合は、`applicationContext.xml` の `DriverManagerDataSource` BeanをJNDI参照に置き換え、`namedParameterJdbcTemplate` と `transactionManager` は同じ `dataSource` Beanを参照し続けます。開発環境では現在の設定を使い、運用環境ではTomcat側で接続プールを管理する構成にします。

このリポジトリには、Tomcat JNDI DataSourceへ切り替えるための参考設定として `src/main/webapp/WEB-INF/spring/datasource-jndi-example.xml` を配置しています。実際に運用環境へ適用する場合は、Tomcat側のJNDIリソース定義、接続プールサイズ、接続検証SQL、タイムアウト値を環境に合わせて決定します。

## DAOでの利用

`UserDao` は `NamedParameterJdbcTemplate` をコンストラクタで受け取ります。

```java
@Repository
public class UserDao {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
}
```

SQLはDAO層に定義し、ControllerやJSPには書きません。

DAOにSQLを集約することで、画面や業務ロジックからDBアクセスの詳細を切り離します。SQLを確認したいときはDAOを見る、という読み方ができるようになります。

## バインド変数

ログインID検索では、以下のように名前付きパラメータを使用します。

```sql
WHERE u.login_id = :loginId
```

Java側では `MapSqlParameterSource` に値を設定します。

```java
MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("loginId", loginId);
```

これにより、ユーザー入力をSQL文字列に直接連結せず、SQLインジェクション対策になります。

`NamedParameterJdbcTemplate` は、`?` の順番ではなく `:loginId` のような名前でパラメータを指定できます。条件が増えても対応関係が読みやすく、業務SQLの保守に向いています。

## RowMapper

SELECT結果は `RowMapper` で `User` に変換します。

```java
user.setUserId(rs.getLong("user_id"));
user.setLoginId(rs.getString("login_id"));
user.setEmployeeName(rs.getString("employee_name"));
```

ResultSetの扱いをDAO内に閉じ込めることで、ServiceやControllerはDBの詳細を意識せずに済みます。

## SQLとDTOの対応

SELECT文の結果は、画面や処理で使いやすいDTOまたはEntityに変換します。

| DAO | SQLの主な取得元 | 変換先 |
|---|---|---|
| `UserDao` | `users`, `departments` | `User` |
| `DashboardDao` | `work_reports`, `report_output_histories`, `users` | `DashboardActivityDto`、集計値 |
| `WorkReportDao#search` | `work_reports`, `users`, `departments` | `WorkReportSearchResultDto` |
| `MonthlyReportDao` | `work_reports`, `users`, `departments` | `MonthlyReportSummaryDto`, `MonthlyReportCategorySummaryDto`, `MonthlyReportDailyDetailDto` |
| `ReportHistoryDao` | `report_output_histories`, `users` | `ReportHistoryDto`, `ReportOutputHistory` |

たとえば `WorkReportSearchResultDto` は、作業日報テーブルだけでなく社員名、部署名、作業分類表示名も持ちます。これは検索結果画面に必要な形に合わせたDTOです。

## 認証用SQL

今回のログインでは、`users` と `departments` を結合してログインユーザー情報を取得します。

```sql
SELECT
    u.user_id,
    u.department_id,
    d.department_name,
    u.login_id,
    u.password,
    u.employee_name,
    u.role_code,
    u.created_at,
    u.updated_at
FROM users u
INNER JOIN departments d
    ON u.department_id = d.department_id
WHERE u.login_id = :loginId
```

`users.password` にはBCryptハッシュを保存します。`WorkReportUserDetailsService` がログインIDでユーザーを取得し、Spring Securityの `BCryptPasswordEncoder` が入力パスワードとDB上のハッシュを照合します。DAOはハッシュ文字列を取得するだけで、パスワード比較の判定は行いません。

## 動的検索条件

作業実績検索では、対象期間、社員名、部署名、作業分類、プロジェクト名を任意条件として指定できます。

検索SQLは `WorkReportDao` に記述し、`users`、`departments`、`work_reports` をJOINします。

```sql
FROM work_reports wr
INNER JOIN users u
    ON wr.user_id = u.user_id
INNER JOIN departments d
    ON wr.department_id = d.department_id
WHERE 1 = 1
```

条件が指定された場合だけ、DAOでWHERE句を追加します。

一般ユーザーの場合は、画面の検索条件に関係なくService層からログインユーザーIDをDAOへ渡し、DAOで以下の条件を必ず追加します。これにより、他ユーザーの作業実績を参照できないようにしています。

```sql
AND wr.user_id = :userId
```

```java
if (dateFrom != null) {
    sql.append("  AND wr.work_date >= :dateFrom ");
    params.addValue("dateFrom", dateFrom);
}
```

社員名、部署名、プロジェクト名は部分一致検索にしています。

```java
if (StringUtils.hasText(employeeName)) {
    sql.append("  AND u.employee_name LIKE :employeeName ");
    params.addValue("employeeName", "%" + employeeName + "%");
}
```

SQL文字列にユーザー入力を直接連結せず、検索条件はすべて `MapSqlParameterSource` に設定します。これにより、動的な検索条件でもバインド変数を使用できます。

作業分類はコード値で完全一致検索します。

```java
if (StringUtils.hasText(workCategory)) {
    sql.append("  AND wr.work_category = :workCategory ");
    params.addValue("workCategory", workCategory);
}
```

検索結果は `RowMapper` で `WorkReportSearchResultDto` に変換します。画面表示用に、作業日は `TO_CHAR(wr.work_date, 'YYYY/MM/DD')`、作業分類名は `CASE` 式で変換しています。

帳票作成履歴検索でも同じ考え方を使います。`ReportHistoryDao#search` では `report_output_histories` と `users` をJOINし、対象年月、帳票種別、作成者、ステータスが指定された場合だけWHERE句を追加します。

```java
if (StringUtils.hasText(form.getStatus())) {
    sql.append("  AND roh.status = :status ");
    params.addValue("status", form.getStatus());
}
```

作成者名は部分一致検索、対象年月・帳票種別・ステータスは完全一致検索です。ステータス名や帳票種別名は、画面表示しやすいようにSQLの `CASE` 式で変換して `ReportHistoryDto` に詰めています。

## ダッシュボード集計SQL

`DashboardDao` は、画面に表示する集計値を個別のSQLで取得します。

| 表示項目 | 主なSQLの考え方 |
|---|---|
| 本日の作業日報登録件数 | `work_reports.created_at` を `TRUNC(SYSDATE)` で当日判定する |
| 今月の総作業時間 | `work_reports.work_date` が当月範囲内の `work_hours` を合計する |
| 未出力の月次報告件数 | `users` に対して当月の `SUCCESS` 履歴が存在しない件数を `NOT EXISTS` で数える |
| 最近の活動 | 作業日報登録と帳票出力履歴を `UNION ALL` でまとめ、日付降順で取得する |

集計SQLもDAOに置くことで、ControllerやJSPはSQLの詳細を知らずに画面表示へ集中できます。

## 更新系SQL

登録処理では、`NamedParameterJdbcTemplate#update` を使用します。

作業日報登録の `WorkReportDao` では、Oracleのシーケンス `seq_work_reports.NEXTVAL` で主キーを採番し、Formから変換した `WorkReport` の値をバインド変数としてINSERTします。

```sql
VALUES (
    seq_work_reports.NEXTVAL,
    :userId,
    :departmentId,
    :workDate,
    :projectName,
    :workCategory,
    :workHours,
    :workContent,
    SYSDATE,
    SYSDATE
)
```

INSERT、UPDATE、DELETEでも、検索と同じくユーザー入力をSQL文字列へ直接連結しないことが基本です。

## 今後の改善

- コネクションプールの導入
- DB接続情報の環境別管理
- DAO単体テスト方針の整理
- 検索結果のページング対応
