<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
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

            <section class="search-card">
                <h3>検索条件</h3>
                <form:form method="get" action="${pageContext.request.contextPath}/report-histories" modelAttribute="reportHistorySearchForm">
                    <div class="search-grid">
                        <div class="search-field">
                            <label for="targetYearMonth">対象年月</label>
                            <form:input path="targetYearMonth" id="targetYearMonth" cssClass="entry-input" placeholder="202605" maxlength="6" />
                        </div>
                        <div class="search-field">
                            <label for="reportType">帳票種別</label>
                            <form:select path="reportType" id="reportType" cssClass="entry-input">
                                <form:option value="" label="すべて" />
                                <form:option value="MONTHLY_WORK_REPORT" label="月次作業報告書" />
                            </form:select>
                        </div>
                        <div class="search-field">
                            <label for="createdByName">作成者</label>
                            <c:choose>
                                <c:when test="${loginUser.roleCode == 'ADMIN'}">
                                    <form:input path="createdByName" id="createdByName" cssClass="entry-input" placeholder="社員名を入力" />
                                </c:when>
                                <c:otherwise>
                                    <form:hidden path="createdByName" />
                                    <div class="readonly-field"><c:out value="${reportHistorySearchForm.createdByName}" /></div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="search-field">
                            <label for="status">ステータス</label>
                            <form:select path="status" id="status" cssClass="entry-input">
                                <form:option value="" label="すべて" />
                                <form:option value="SUCCESS" label="完了" />
                                <form:option value="ERROR" label="エラー" />
                                <form:option value="PROCESSING" label="処理中" />
                            </form:select>
                        </div>
                    </div>
                    <div class="search-actions">
                        <button class="primary-action" type="submit">検索</button>
                        <a class="secondary-action" href="<c:url value='/report-histories' />">条件クリア</a>
                    </div>
                </form:form>
            </section>

            <section class="result-card">
                <div class="result-header">
                    <h3>履歴一覧 <span><c:out value="${fn:length(reportHistories)}" /> 件</span></h3>
                    <a class="secondary-action compact-action" href="<c:url value='/report-histories' />">更新</a>
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
                                                    <a class="detail-button" href="<c:url value='/report-histories/${history.reportOutputHistoryId}' />">詳細</a>
                                                </c:when>
                                                <c:otherwise>
                                                    <a class="detail-button" href="<c:url value='/report-histories/${history.reportOutputHistoryId}' />">詳細</a>
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
