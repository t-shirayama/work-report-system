<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>月次報告書出力 | 作業日報・月次報告書作成システム</title>
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
                <a class="sidebar-link" href="<c:url value='/work-reports/new' />">作業日報登録</a>
                <a class="sidebar-link" href="<c:url value='/work-reports/search' />">作業実績検索</a>
                <a class="sidebar-link active" href="<c:url value='/monthly-reports/new' />">月次報告書出力</a>
                <a class="sidebar-link" href="<c:url value='/report-histories' />">帳票作成履歴</a>
            </nav>
        </aside>

        <main class="dashboard-main">
            <div class="breadcrumb">ホーム &gt; 月次報告書 &gt; 出力</div>
            <h2 class="page-title">月次報告書出力</h2>

            <section class="report-card">
                <h3>出力条件</h3>

                <c:if test="${not empty errors}">
                    <div class="error-message">
                        <ul class="error-list">
                            <c:forEach var="error" items="${errors}">
                                <li><c:out value="${error}" /></li>
                            </c:forEach>
                        </ul>
                    </div>
                </c:if>

                <form:form method="post" action="${pageContext.request.contextPath}/monthly-reports/export" modelAttribute="monthlyReportForm">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <div class="search-grid">
                        <div class="search-field">
                            <label for="targetYear">対象年 <span class="required">*</span></label>
                            <form:input path="targetYear" id="targetYear" cssClass="entry-input" placeholder="2026" maxlength="4" />
                        </div>
                        <div class="search-field">
                            <label for="targetMonth">対象月 <span class="required">*</span></label>
                            <form:select path="targetMonth" id="targetMonth" cssClass="entry-input">
                                <form:option value="" label="選択してください" />
                                <form:option value="1" label="1月" />
                                <form:option value="2" label="2月" />
                                <form:option value="3" label="3月" />
                                <form:option value="4" label="4月" />
                                <form:option value="5" label="5月" />
                                <form:option value="6" label="6月" />
                                <form:option value="7" label="7月" />
                                <form:option value="8" label="8月" />
                                <form:option value="9" label="9月" />
                                <form:option value="10" label="10月" />
                                <form:option value="11" label="11月" />
                                <form:option value="12" label="12月" />
                            </form:select>
                        </div>
                        <div class="search-field">
                            <label for="userId">社員 <span class="required">*</span></label>
                            <c:choose>
                                <c:when test="${loginUser.roleCode == 'ADMIN'}">
                                    <form:select path="userId" id="userId" cssClass="entry-input">
                                        <form:option value="" label="選択してください" />
                                        <c:forEach var="targetUser" items="${targetUsers}">
                                            <form:option value="${targetUser.userId}" label="${targetUser.departmentName} / ${targetUser.employeeName}" />
                                        </c:forEach>
                                    </form:select>
                                </c:when>
                                <c:otherwise>
                                    <form:hidden path="userId" />
                                    <form:hidden path="departmentName" />
                                    <form:hidden path="employeeName" />
                                    <div class="readonly-field">
                                        <c:out value="${monthlyReportForm.departmentName}" /> / <c:out value="${monthlyReportForm.employeeName}" />
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="report-note">
                        入力条件に一致する作業実績を集計し、Excelテンプレートへ差し込んでダウンロードします。
                        一般ユーザーは自分の月次報告書のみ出力できます。
                    </div>

                    <div class="search-actions">
                        <button class="primary-action" type="submit">Excel出力</button>
                        <a class="secondary-action" href="<c:url value='/monthly-reports/new' />">条件クリア</a>
                    </div>
                </form:form>
            </section>
        </main>
    </div>
</div>
</body>
</html>
