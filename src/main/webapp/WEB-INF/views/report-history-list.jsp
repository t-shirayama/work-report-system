<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>帳票作成履歴 | 作業日報・月次報告書作成システム</title>
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
                <a class="sidebar-link" href="<c:url value='/work-reports/new' />">作業日報登録</a>
                <a class="sidebar-link" href="<c:url value='/work-reports/search' />">作業実績検索</a>
                <a class="sidebar-link" href="<c:url value='/monthly-reports/new' />">月次報告書出力</a>
                <a class="sidebar-link active" href="<c:url value='/report-histories' />">帳票作成履歴</a>
            </nav>
        </aside>

        <main class="dashboard-main">
            <div class="breadcrumb">ホーム &gt; 帳票作成履歴</div>
            <h2 class="page-title">帳票作成履歴</h2>

            <section class="result-card">
                <div class="result-header">
                    <h3>履歴一覧 <span><c:out value="${fn:length(reportHistories)}" /> 件</span></h3>
                </div>

                <c:if test="${not empty errorMessage}">
                    <div class="error-message">
                        <c:out value="${errorMessage}" />
                    </div>
                </c:if>

                <div class="result-table-wrap">
                    <table class="activity-table result-table history-table">
                        <thead>
                        <tr>
                            <th>出力日時</th>
                            <th>対象年月</th>
                            <th>帳票種別</th>
                            <th>作成者</th>
                            <th>ステータス</th>
                            <th>ファイル名</th>
                            <th>操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty reportHistories}">
                                <tr>
                                    <td colspan="7" class="empty-cell">帳票作成履歴はありません。</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="history" items="${reportHistories}">
                                    <tr>
                                        <td><c:out value="${history.outputAt}" /></td>
                                        <td><c:out value="${history.targetYearMonth}" /></td>
                                        <td><c:out value="${history.reportTypeName}" /></td>
                                        <td><c:out value="${history.createdByName}" /></td>
                                        <td>
                                            <span class="status-badge ${history.status}">
                                                <c:out value="${history.statusName}" />
                                            </span>
                                        </td>
                                        <td>
                                            <c:out value="${history.fileName}" />
                                            <c:if test="${history.status == 'ERROR' && not empty history.errorMessage}">
                                                <div class="history-error"><c:out value="${history.errorMessage}" /></div>
                                            </c:if>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${history.status == 'SUCCESS'}">
                                                    <a class="download-button" href="<c:url value='/report-histories/${history.reportOutputHistoryId}/download' />">ダウンロード</a>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="muted-text">不可</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>
            </section>
        </main>
    </div>
</div>
</body>
</html>
