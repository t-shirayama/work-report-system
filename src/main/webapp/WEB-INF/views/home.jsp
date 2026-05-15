<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>作業日報・月次報告書作成システム</title>
    <link rel="stylesheet" href="<c:url value='/resources/css/common.css' />">
</head>
<body>
<div class="page">
    <header class="app-header">
        <div>
            <p class="app-kicker">Work Report System</p>
            <h1>作業日報・月次報告書作成システム</h1>
        </div>
    </header>

    <main class="main-content">
        <section class="section">
            <h2>システム概要</h2>
            <p>
                日々の作業実績を登録し、登録済みデータをもとに月次報告書をExcel形式で作成する業務システムです。
                Spring MVC、JSP、Spring JDBC、Apache POIを使用したMaven WARアプリケーションとして構築しています。
            </p>
        </section>

        <section class="section">
            <h2>メニュー</h2>
            <div class="action-row">
                <a class="primary-link" href="<c:url value='/login' />">ログイン画面へ</a>
            </div>
            <p class="note">サンプルユーザーでログインし、作業日報登録や帳票出力を確認できます。</p>
        </section>

        <section class="section">
            <h2>主な機能一覧</h2>
            <ul class="feature-list">
                <c:forEach var="feature" items="${features}">
                    <li><c:out value="${feature}" /></li>
                </c:forEach>
            </ul>
        </section>
    </main>
</div>
</body>
</html>
