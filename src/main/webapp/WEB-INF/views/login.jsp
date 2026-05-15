<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>ログイン | 作業日報・月次報告書作成システム</title>
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

    <main class="main-content narrow">
        <section class="section">
            <h2>ログイン</h2>

            <c:if test="${not empty errorMessage}">
                <div class="error-message">
                    <c:out value="${errorMessage}" />
                </div>
            </c:if>

            <form class="form" method="post" action="<c:url value='/login' />">
                <div class="form-row">
                    <label for="loginId">ログインID</label>
                    <input type="text" id="loginId" name="loginId" autocomplete="username">
                </div>

                <div class="form-row">
                    <label for="password">パスワード</label>
                    <input type="password" id="password" name="password" autocomplete="current-password">
                </div>

                <div class="button-row">
                    <button class="primary-button" type="submit">ログイン</button>
                    <a class="secondary-link" href="<c:url value='/home' />">トップへ戻る</a>
                </div>
            </form>

            <p class="note">サンプルデータ投入後は `admin` / `password` などで確認できます。</p>
        </section>
    </main>
</div>
</body>
</html>
