<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>作業実績検索 | 作業日報・月次報告書作成システム</title>
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
                <a class="sidebar-link active" href="<c:url value='/work-reports/search' />">作業実績検索</a>
                <a class="sidebar-link" href="#">月次報告書出力</a>
                <a class="sidebar-link" href="#">帳票作成履歴</a>
            </nav>
        </aside>

        <main class="dashboard-main">
            <div class="breadcrumb">ホーム &gt; 作業実績検索</div>
            <h2 class="page-title">作業実績検索</h2>

            <section class="search-card">
                <h3>検索条件</h3>

                <c:if test="${not empty errors}">
                    <div class="error-message">
                        <ul class="error-list">
                            <c:forEach var="error" items="${errors}">
                                <li><c:out value="${error}" /></li>
                            </c:forEach>
                        </ul>
                    </div>
                </c:if>

                <form:form method="post" action="${pageContext.request.contextPath}/work-reports/search" modelAttribute="workReportSearchForm">
                    <div class="search-grid">
                        <div class="search-field period-field">
                            <label>対象期間</label>
                            <div class="period-inputs">
                                <form:input path="dateFrom" type="date" cssClass="entry-input" />
                                <span>～</span>
                                <form:input path="dateTo" type="date" cssClass="entry-input" />
                            </div>
                        </div>

                        <div class="search-field">
                            <label for="employeeName">社員</label>
                            <form:input path="employeeName" id="employeeName" cssClass="entry-input" placeholder="社員名を入力" />
                        </div>

                        <div class="search-field">
                            <label for="departmentName">部署</label>
                            <form:input path="departmentName" id="departmentName" cssClass="entry-input" placeholder="部署名を入力" />
                        </div>

                        <div class="search-field">
                            <label for="workCategory">作業分類</label>
                            <form:select path="workCategory" id="workCategory" cssClass="entry-input">
                                <form:option value="" label="すべて" />
                                <form:option value="DESIGN" label="設計" />
                                <form:option value="DEVELOPMENT" label="開発" />
                                <form:option value="TEST" label="テスト" />
                                <form:option value="MEETING" label="会議" />
                                <form:option value="DOCUMENT" label="資料作成" />
                                <form:option value="OTHER" label="その他" />
                            </form:select>
                        </div>

                        <div class="search-field wide-field">
                            <label for="projectName">プロジェクト名</label>
                            <form:input path="projectName" id="projectName" cssClass="entry-input" placeholder="プロジェクト名を入力" />
                        </div>
                    </div>

                    <div class="search-actions">
                        <button class="primary-action" type="submit">検索</button>
                        <a class="secondary-action" href="<c:url value='/work-reports/search' />">条件クリア</a>
                    </div>
                </form:form>
            </section>

            <section class="result-card">
                <div class="result-header">
                    <h3>検索結果 <span><c:out value="${fn:length(searchResults)}" /> 件</span></h3>
                </div>

                <div class="result-table-wrap">
                    <table class="activity-table result-table">
                        <thead>
                        <tr>
                            <th>日付</th>
                            <th>社員名</th>
                            <th>部署</th>
                            <th>プロジェクト名</th>
                            <th>作業分類</th>
                            <th>作業時間</th>
                            <th>作業内容</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty searchResults}">
                                <tr>
                                    <td colspan="7" class="empty-cell">該当する作業実績はありません。</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="result" items="${searchResults}">
                                    <tr>
                                        <td><c:out value="${result.workDate}" /></td>
                                        <td><c:out value="${result.employeeName}" /></td>
                                        <td><c:out value="${result.departmentName}" /></td>
                                        <td><c:out value="${result.projectName}" /></td>
                                        <td><c:out value="${result.workCategoryName}" /></td>
                                        <td><c:out value="${result.workHours}" /> 時間</td>
                                        <td><c:out value="${result.workContent}" /></td>
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
