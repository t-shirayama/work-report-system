<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>作業日報登録完了 | 作業日報・月次報告書作成システム</title>
    <link rel="stylesheet" href="<c:url value='/resources/css/common.css' />">
</head>
<body>
<div class="dashboard-shell">
    <header class="dashboard-header">
        <div class="brand-area">
            <button class="menu-button" type="button" aria-label="メニュー">☰</button>
            <h1>作業日報・月次報告書作成システム</h1>
        </div>
        <div class="user-area">
            <span class="user-avatar" aria-hidden="true">人</span>
            <span><c:out value="${loginUser.employeeName}" /> 様</span>
            <form method="post" action="<c:url value='/logout' />">
                <button class="logout-button" type="submit">ログアウト</button>
            </form>
        </div>
    </header>

    <div class="dashboard-body">
        <aside class="sidebar">
            <p class="sidebar-title">メニュー</p>
            <nav>
                <a class="sidebar-link" href="<c:url value='/dashboard' />">ホーム</a>
                <a class="sidebar-link active" href="<c:url value='/work-reports/new' />">作業日報登録</a>
                <a class="sidebar-link" href="<c:url value='/work-reports/search' />">作業実績検索</a>
                <a class="sidebar-link" href="#">月次報告書出力</a>
                <a class="sidebar-link" href="#">帳票作成履歴</a>
            </nav>
        </aside>

        <main class="dashboard-main">
            <div class="breadcrumb">ホーム &gt; 作業日報 &gt; 登録完了</div>
            <h2 class="page-title">作業日報登録完了</h2>

            <section class="complete-card">
                <div class="complete-icon">✓</div>
                <h3>作業日報を登録しました</h3>
                <p>入力された作業実績を保存しました。</p>

                <c:if test="${not empty registeredWorkReport}">
                    <table class="complete-table">
                        <tr>
                            <th>作業日</th>
                            <td><c:out value="${registeredWorkReport.workDate}" /></td>
                        </tr>
                        <tr>
                            <th>プロジェクト名</th>
                            <td><c:out value="${registeredWorkReport.projectName}" /></td>
                        </tr>
                        <tr>
                            <th>作業分類</th>
                            <td><c:out value="${registeredWorkReport.workCategory}" /></td>
                        </tr>
                        <tr>
                            <th>作業時間</th>
                            <td><c:out value="${registeredWorkReport.workHours}" /> 時間</td>
                        </tr>
                    </table>
                </c:if>

                <div class="complete-actions">
                    <a class="primary-link-button" href="<c:url value='/work-reports/new' />">続けて登録</a>
                    <a class="secondary-action" href="<c:url value='/dashboard' />">ダッシュボードへ戻る</a>
                </div>
            </section>
        </main>
    </div>
</div>
</body>
</html>
