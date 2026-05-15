# Spring MVC 基本構成

このドキュメントでは、`work-report-system` の最小起動確認として作成したSpring MVC構成を説明します。

本プロジェクトはSpring Bootを使用せず、`web.xml`、Spring MVC XML設定、Controller、JSPを組み合わせた従来型のMaven WARアプリケーションとして構成します。

## 起動確認URL

STS上のTomcat 8.5にアプリケーションを追加して起動した後、以下へアクセスします。

```text
http://localhost:8080/work-report-system/home
```

コンテキストパスが異なる場合は、`/home` の前の部分をTomcat上の設定に合わせて変更してください。

## 今回作成したファイル

| ファイル | 役割 |
|---|---|
| `src/main/java/com/example/workreport/controller/HomeController.java` | `/home` のリクエストを受け取るController |
| `src/main/webapp/WEB-INF/views/home.jsp` | トップ画面JSP |
| `src/main/webapp/resources/css/common.css` | 共通CSS |
| `src/main/webapp/WEB-INF/web.xml` | DispatcherServletの定義 |
| `src/main/webapp/WEB-INF/spring/dispatcher-servlet.xml` | Spring MVC設定 |

## DispatcherServlet

`web.xml` では、Spring MVCの入口として `DispatcherServlet` を定義しています。

```xml
<servlet>
    <servlet-name>dispatcher</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/spring/dispatcher-servlet.xml</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
</servlet>
```

`/` にマッピングしているため、アプリケーションへのリクエストはDispatcherServletが受け付けます。

## Controller

`HomeController` は `com.example.workreport.controller` パッケージに配置しています。

`GET /home` を受け取り、画面に表示する機能予定一覧を `Model` に設定して、ビュー名 `home` を返します。

```java
@GetMapping("/home")
public String home(Model model) {
    model.addAttribute("plannedFeatures", plannedFeatures);
    return "home";
}
```

ここではDB接続や業務処理は行っていません。Spring MVCのリクエスト受付からJSP表示までの疎通確認だけを目的にしています。

## component-scan

`dispatcher-servlet.xml` では、Controllerを検出するために以下を設定しています。

```xml
<context:component-scan base-package="com.example.workreport.controller"/>
```

この設定により、`@Controller` が付いたクラスがSpring MVCのControllerとして登録されます。

## annotation-driven

以下の設定により、`@GetMapping` などのアノテーションベースのSpring MVC機能を有効化しています。

```xml
<mvc:annotation-driven/>
```

## ViewResolver

Controllerが返したビュー名 `home` は、`InternalResourceViewResolver` によってJSPのパスへ変換されます。

```xml
<bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
    <property name="prefix" value="/WEB-INF/views/"/>
    <property name="suffix" value=".jsp"/>
</bean>
```

この設定により、`home` は以下のJSPとして解決されます。

```text
/WEB-INF/views/home.jsp
```

## JSP表示の流れ

`/home` へアクセスした場合の処理の流れは以下です。

1. ブラウザから `/work-report-system/home` にアクセスする
2. Tomcatがアプリケーションへリクエストを渡す
3. `web.xml` の設定により `DispatcherServlet` がリクエストを受け取る
4. Spring MVCが `HomeController#home` を呼び出す
5. Controllerが `Model` に表示データを設定し、ビュー名 `home` を返す
6. ViewResolverが `/WEB-INF/views/home.jsp` を解決する
7. JSPがHTMLを生成してブラウザへ返す

## 静的リソース

CSSは以下に配置しています。

```text
src/main/webapp/resources/css/common.css
```

`dispatcher-servlet.xml` の以下の設定により、JSPから `/resources/css/common.css` として参照できます。

```xml
<mvc:resources mapping="/resources/**" location="/resources/"/>
```

JSPではJSTLの `c:url` を使い、コンテキストパスを意識せずCSSへリンクしています。

```jsp
<link rel="stylesheet" href="<c:url value='/resources/css/common.css' />">
```

## 今後の拡張

- ログイン画面用ControllerとJSPの追加
- 共通ヘッダー、メニュー、フッターの部品化
- 入力チェックとエラーメッセージ表示の追加
- Service / DAO / Spring JDBCとの接続
- `docs/controller-service-dao.md` へのレイヤ構成説明追加
