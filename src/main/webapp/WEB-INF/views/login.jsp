<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>ログイン | 作業日報・月次報告書作成システム</title>
    <link rel="stylesheet" href="<c:url value='/resources/css/common.css' />">
</head>
<body class="login-body">
<div class="login-page">
    <header class="login-header">
        <h1>作業日報・月次報告書作成システム</h1>
        <p>作業日報および月次報告書の作成・管理を行うシステムです</p>
    </header>

    <main class="login-main">
        <section class="login-card">
            <div class="login-icon" aria-hidden="true">□</div>
            <h2>ログイン</h2>

            <c:if test="${not empty errorMessage}">
                <div class="error-message">
                    <c:out value="${errorMessage}" />
                </div>
            </c:if>

            <form class="form" method="post" action="<c:url value='/login' />">
                <div class="form-row">
                    <label for="loginId">ログインID</label>
                    <div class="input-wrap">
                        <span class="input-icon" aria-hidden="true">ID</span>
                        <input type="text" id="loginId" name="loginId" placeholder="ログインIDを入力してください" autocomplete="username">
                    </div>
                </div>

                <div class="form-row">
                    <label for="password">パスワード</label>
                    <div class="input-wrap">
                        <span class="input-icon" aria-hidden="true">PW</span>
                        <input type="password" id="password" name="password" placeholder="パスワードを入力してください" autocomplete="current-password">
                    </div>
                </div>

                <div class="checkbox-row">
                    <input type="checkbox" id="rememberLogin" name="rememberLogin">
                    <label for="rememberLogin">ログイン状態を保持</label>
                </div>

                <div class="button-row">
                    <button class="primary-button" type="submit">ログイン</button>
                </div>
            </form>

            <p class="login-note">社内業務向けシステム</p>
        </section>
    </main>
</div>
</body>
</html>
