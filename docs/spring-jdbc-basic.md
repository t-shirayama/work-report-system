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

## 今後の改善

- コネクションプールの導入
- DB接続情報の環境別管理
- パスワードハッシュ化
- DAO単体テスト方針の整理
- トランザクション管理の追加
