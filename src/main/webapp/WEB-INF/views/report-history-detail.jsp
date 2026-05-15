<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>帳票作成履歴詳細 | 作業日報・月次報告書作成システム</title>
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
                <a class="sidebar-link" href="<c:url value='/monthly-reports/new' />">月次報告書出力</a>
                <a class="sidebar-link active" href="<c:url value='/report-histories' />">帳票作成履歴</a>
            </nav>
        </aside>

        <main class="dashboard-main">
            <div class="breadcrumb">ホーム &gt; 帳票作成履歴 &gt; 詳細</div>
            <h2 class="page-title">帳票作成履歴詳細</h2>

            <section class="detail-card">
                <div class="detail-header">
                    <h3>履歴情報</h3>
                    <span class="status-badge ${history.status}">
                        <c:out value="${history.statusName}" />
                    </span>
                </div>

                <table class="detail-table">
                    <tbody>
                    <tr>
                        <th>出力日時</th>
                        <td><c:out value="${history.outputAt}" /></td>
                    </tr>
                    <tr>
                        <th>対象年月</th>
                        <td><c:out value="${history.targetYearMonth}" /></td>
                    </tr>
                    <tr>
                        <th>帳票種別</th>
                        <td><c:out value="${history.reportTypeName}" /></td>
                    </tr>
                    <tr>
                        <th>作成者</th>
                        <td><c:out value="${history.createdByName}" /></td>
                    </tr>
                    <tr>
                        <th>ファイル名</th>
                        <td><c:out value="${history.fileName}" /></td>
                    </tr>
                    <tr>
                        <th>ファイルパス</th>
                        <td><c:out value="${history.filePath}" /></td>
                    </tr>
                    <tr>
                        <th>エラーメッセージ</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty history.errorMessage}">
                                    <c:out value="${history.errorMessage}" />
                                </c:when>
                                <c:otherwise>
                                    <span class="muted-text">なし</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    </tbody>
                </table>

                <div class="detail-actions">
                    <c:if test="${history.status == 'SUCCESS'}">
                        <a class="primary-action button-link" href="<c:url value='/report-histories/${history.reportOutputHistoryId}/download' />">ダウンロード</a>
                    </c:if>
                    <a class="secondary-action" href="<c:url value='/report-histories' />">一覧へ戻る</a>
                </div>
            </section>
        </main>
    </div>
</div>
</body>
</html>
