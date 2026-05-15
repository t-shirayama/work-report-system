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
                <button class="logout-button" type="submit">ログアウト</button>
            </form>
        </div>
    </header>

    <div class="dashboard-body">
        <aside class="sidebar">
            <p class="sidebar-title">メニュー</p>
            <nav>
                <a class="sidebar-link active" href="#">作業日報登録</a>
                <a class="sidebar-link" href="#">作業実績検索</a>
                <a class="sidebar-link" href="#">月次報告書出力</a>
                <a class="sidebar-link" href="#">帳票作成履歴</a>
            </nav>
        </aside>

        <main class="dashboard-main">
            <h2 class="page-title">ダッシュボード</h2>

            <section class="summary-grid">
                <div class="summary-card">
                    <div class="summary-icon blue">□</div>
                    <p class="summary-label">本日の登録件数</p>
                    <p class="summary-value blue-text">12 <span>件</span></p>
                    <p class="summary-sub">前日比 +3件 ↑</p>
                </div>
                <div class="summary-card">
                    <div class="summary-icon green">○</div>
                    <p class="summary-label">今月の総作業時間</p>
                    <p class="summary-value green-text">128:45</p>
                    <p class="summary-sub">前月比 +12:30 ↑</p>
                </div>
                <div class="summary-card">
                    <div class="summary-icon orange">□</div>
                    <p class="summary-label">未出力の月次報告</p>
                    <p class="summary-value orange-text">3 <span>件</span></p>
                    <p class="summary-sub">対象月: 2026年5月 ほか</p>
                </div>
                <div class="summary-card">
                    <div class="summary-icon purple">□</div>
                    <p class="summary-label">最終ログイン</p>
                    <p class="summary-value purple-text">本日 08:45</p>
                    <p class="summary-sub">2026/05/15 08:45</p>
                </div>
            </section>

            <section class="activity-card">
                <div class="activity-header">
                    <h3>最近の活動</h3>
                    <a href="#">すべてを見る</a>
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
                    <tr>
                        <td>2026/05/15 10:15</td>
                        <td><span class="badge blue">登録</span></td>
                        <td>作業日報を登録しました（2026/05/15分）</td>
                        <td><c:out value="${loginUser.employeeName}" /></td>
                    </tr>
                    <tr>
                        <td>2026/05/15 09:47</td>
                        <td><span class="badge blue">登録</span></td>
                        <td>作業日報を登録しました（2026/05/15分）</td>
                        <td>佐藤 花子</td>
                    </tr>
                    <tr>
                        <td>2026/05/14 16:30</td>
                        <td><span class="badge green">出力</span></td>
                        <td>月次報告書を出力しました（2026年5月分）</td>
                        <td>鈴木 一郎</td>
                    </tr>
                    <tr>
                        <td>2026/05/14 15:10</td>
                        <td><span class="badge blue">登録</span></td>
                        <td>作業日報を登録しました（2026/05/14分）</td>
                        <td><c:out value="${loginUser.employeeName}" /></td>
                    </tr>
                    <tr>
                        <td>2026/05/14 11:05</td>
                        <td><span class="badge purple">検索</span></td>
                        <td>作業実績を検索しました（期間: 2026/05/01 ～ 2026/05/31）</td>
                        <td>佐藤 花子</td>
                    </tr>
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
