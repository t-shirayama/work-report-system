<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>ダッシュボード | 作業日報・月次報告書作成システム</title>
    <link rel="stylesheet" href="<c:url value='/resources/css/common.css' />">
</head>
<body>
<div class="page">
    <header class="app-header app-header-row">
        <div>
            <p class="app-kicker">Work Report System</p>
            <h1>ダッシュボード</h1>
        </div>
        <form method="post" action="<c:url value='/logout' />">
            <button class="header-button" type="submit">ログアウト</button>
        </form>
    </header>

    <main class="main-content">
        <section class="section">
            <h2>ログインユーザー</h2>
            <table class="detail-table">
                <tr>
                    <th>社員名</th>
                    <td><c:out value="${loginUser.employeeName}" /></td>
                </tr>
                <tr>
                    <th>ログインID</th>
                    <td><c:out value="${loginUser.loginId}" /></td>
                </tr>
                <tr>
                    <th>部署</th>
                    <td><c:out value="${loginUser.departmentName}" /></td>
                </tr>
                <tr>
                    <th>権限</th>
                    <td><c:out value="${loginUser.roleCode}" /></td>
                </tr>
            </table>
        </section>

        <section class="section">
            <h2>機能メニュー</h2>
            <ul class="feature-list">
                <li>作業日報登録</li>
                <li>作業実績検索</li>
                <li>月次報告書Excel出力</li>
                <li>帳票作成履歴</li>
            </ul>
            <p class="note">各業務機能は今後の実装予定です。</p>
        </section>
    </main>
</div>
</body>
</html>
