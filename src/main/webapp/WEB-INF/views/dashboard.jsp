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
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button class="logout-button" type="submit">ログアウト</button>
            </form>
        </div>
    </header>

    <div class="dashboard-body">
        <aside class="sidebar">
            <p class="sidebar-title">メニュー</p>
            <nav>
                <a class="sidebar-link active" href="<c:url value='/dashboard' />">ホーム</a>
                <a class="sidebar-link" href="<c:url value='/work-reports/new' />">作業日報登録</a>
                <a class="sidebar-link" href="<c:url value='/work-reports/search' />">作業実績検索</a>
                <a class="sidebar-link" href="<c:url value='/monthly-reports/new' />">月次報告書出力</a>
                <a class="sidebar-link" href="<c:url value='/report-histories' />">帳票作成履歴</a>
            </nav>
        </aside>

        <main class="dashboard-main">
            <h2 class="page-title">ダッシュボード</h2>

            <section class="summary-grid">
                <div class="summary-card">
                    <div class="summary-icon blue">□</div>
                    <p class="summary-label">本日の登録件数</p>
                    <p class="summary-value blue-text"><c:out value="${dashboard.todayWorkReportCount}" /> <span>件</span></p>
                    <p class="summary-sub">本日登録された作業日報</p>
                </div>
                <div class="summary-card">
                    <div class="summary-icon green">○</div>
                    <p class="summary-label">今月の総作業時間</p>
                    <p class="summary-value green-text"><c:out value="${dashboard.currentMonthTotalHours}" /> <span>時間</span></p>
                    <p class="summary-sub">当月の作業実績合計</p>
                </div>
                <div class="summary-card">
                    <div class="summary-icon orange">□</div>
                    <p class="summary-label">未出力の月次報告</p>
                    <p class="summary-value orange-text"><c:out value="${dashboard.notOutputMonthlyReportCount}" /> <span>件</span></p>
                    <p class="summary-sub">当月の未出力ユーザー数</p>
                </div>
                <div class="summary-card">
                    <div class="summary-icon purple">□</div>
                    <p class="summary-label">最終ログイン</p>
                    <p class="summary-value purple-text">ログイン中</p>
                    <p class="summary-sub"><c:out value="${loginAt}" /></p>
                </div>
            </section>

            <section class="activity-card">
                <div class="activity-header">
                    <h3>最近の活動</h3>
                    <a href="<c:url value='/report-histories' />">帳票履歴を見る</a>
                </div>
                <table class="activity-table">
                    <thead>
                    <tr>
                        <th>日時</th>
                        <th>種別</th>
                        <th>内容</th>
                        <th>ユーザー</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty dashboard.recentActivities}">
                            <tr>
                                <td colspan="4" class="empty-cell">最近の活動はありません。</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="activity" items="${dashboard.recentActivities}">
                                <tr>
                                    <td><c:out value="${activity.activityAt}" /></td>
                                    <td><span class="badge ${activity.badgeClass}"><c:out value="${activity.activityTypeName}" /></span></td>
                                    <td><c:out value="${activity.content}" /></td>
                                    <td><c:out value="${activity.employeeName}" /></td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </section>

            <section class="info-banner">
                <strong>システムに関するお知らせ</strong>
                <span>2026年5月のシステムメンテナンスは、5月18日（月）0:00 ～ 6:00 に予定されています。</span>
            </section>
        </main>
    </div>
</div>
</body>
</html>
