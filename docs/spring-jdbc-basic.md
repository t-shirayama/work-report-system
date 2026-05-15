# Spring JDBC 基本

このドキュメントでは、簡易ログイン機能を例にして、Spring JDBCの使い方を説明します。

本プロジェクトでは、DBアクセスにSpring JDBCの `NamedParameterJdbcTemplate` を使用します。JPA / Hibernate / MyBatisは使用しません。

## DB接続設定

DB接続情報は `src/main/resources/application.properties` に定義します。

```properties
jdbc.driverClassName=oracle.jdbc.OracleDriver
jdbc.url=jdbc:oracle:thin:@localhost:1521/FREE
jdbc.username=work_report
jdbc.password=work_report
```

この設定は開発用の例です。実際の接続先、ユーザー名、パスワードはローカルのOracle Database Free / XEに合わせて変更します。

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

この段階では、シンプルさを優先して `DriverManagerDataSource` を使用しています。実案件ではコネクションプールの利用を検討します。

## DAOでの利用

`UserDao` は `NamedParameterJdbcTemplate` をコンストラクタで受け取ります。

```java
@Repository
public class UserDao {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
}
```

SQLはDAO層に定義し、ControllerやJSPには書きません。

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

## RowMapper

SELECT結果は `RowMapper` で `User` に変換します。

```java
user.setUserId(rs.getLong("user_id"));
user.setLoginId(rs.getString("login_id"));
user.setEmployeeName(rs.getString("employee_name"));
```

ResultSetの扱いをDAO内に閉じ込めることで、ServiceやControllerはDBの詳細を意識せずに済みます。

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

パスワード比較はService層で行っています。ポートフォリオ簡易版では平文比較ですが、本番ではハッシュ化したパスワードを照合します。

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

## 今後の改善

- コネクションプールの導入
- DB接続情報の環境別管理
- パスワードハッシュ化
- DAO単体テスト方針の整理
- トランザクション管理の追加
