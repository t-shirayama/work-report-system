<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>作業日報登録 | 作業日報・月次報告書作成システム</title>
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
                <a class="sidebar-link" href="<c:url value='/dashboard' />">ホーム</a>
                <a class="sidebar-link active" href="<c:url value='/work-reports/new' />">作業日報登録</a>
                <a class="sidebar-link" href="<c:url value='/work-reports/search' />">作業実績検索</a>
                <a class="sidebar-link" href="<c:url value='/monthly-reports/new' />">月次報告書出力</a>
                <a class="sidebar-link" href="<c:url value='/report-histories' />">帳票作成履歴</a>
            </nav>
        </aside>

        <main class="dashboard-main">
            <div class="breadcrumb">ホーム &gt; 作業日報 &gt; 作業日報登録</div>
            <h2 class="page-title">作業日報登録</h2>

            <section class="entry-card">
                <c:if test="${not empty errors}">
                    <div class="error-message">
                        <ul class="error-list">
                            <c:forEach var="error" items="${errors}">
                                <li><c:out value="${error}" /></li>
                            </c:forEach>
                        </ul>
                    </div>
                </c:if>

                <form:form class="entry-form" method="post" action="${pageContext.request.contextPath}/work-reports" modelAttribute="workReportForm">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <div class="entry-row">
                        <label for="workDate">作業日 <span class="required">*</span></label>
                        <form:input path="workDate" id="workDate" type="date" cssClass="entry-input medium" />
                    </div>

                    <div class="entry-row readonly-row">
                        <label>社員名 <span class="required">*</span></label>
                        <div class="readonly-field"><c:out value="${loginUser.employeeName}" /></div>
                    </div>

                    <div class="entry-row readonly-row">
                        <label>部署 <span class="required">*</span></label>
                        <div class="readonly-field"><c:out value="${loginUser.departmentName}" /></div>
                    </div>

                    <div class="entry-row">
                        <label for="projectName">プロジェクト名 <span class="required">*</span></label>
                        <form:input path="projectName" id="projectName" cssClass="entry-input large" maxlength="100" />
                    </div>

                    <div class="entry-row">
                        <label for="workCategory">作業分類 <span class="required">*</span></label>
                        <form:select path="workCategory" id="workCategory" cssClass="entry-input medium">
                            <form:option value="" label="選択してください" />
                            <form:option value="DESIGN" label="設計" />
                            <form:option value="DEVELOPMENT" label="開発" />
                            <form:option value="TEST" label="テスト" />
                            <form:option value="MEETING" label="会議" />
                            <form:option value="DOCUMENT" label="資料作成" />
                            <form:option value="OTHER" label="その他" />
                        </form:select>
                    </div>

                    <div class="entry-row">
                        <label for="workHours">作業時間 <span class="required">*</span></label>
                        <div class="inline-input">
                            <form:input path="workHours" id="workHours" type="number" step="0.25" min="0" cssClass="entry-input small" />
                            <span>時間</span>
                        </div>
                    </div>

                    <div class="entry-row top-align">
                        <label for="workContent">作業内容 <span class="required">*</span></label>
                        <form:textarea path="workContent" id="workContent" cssClass="entry-textarea" maxlength="1000" />
                    </div>

                    <div class="entry-actions">
                        <button class="primary-action" type="submit">登録</button>
                        <a class="secondary-action" href="<c:url value='/work-reports/new' />">クリア</a>
                        <a class="secondary-action" href="<c:url value='/dashboard' />">戻る</a>
                    </div>
                </form:form>
            </section>
        </main>
    </div>
</div>
</body>
</html>
