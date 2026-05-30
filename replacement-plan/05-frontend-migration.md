# 05. フロントエンド移行計画

## 移行方針

JSPでサーバーサイドレンダリングしている画面を、Reactのページ、コンポーネント、API通信へ分解します。現行の画面URLは業務利用者に馴染みがあるため、Reactルートでもできるだけ近い導線を維持します。

## ルート対応

| 現行JSP | 現行URL | Reactルート案 | 備考 |
|---|---|---|---|
| `home.jsp` | `/home` | `/` または `/home` | ログイン誘導だけなら省略も検討 |
| `login.jsp` | `/login` | `/login` | 認証画面 |
| `dashboard.jsp` | `/dashboard` | `/dashboard` | 認証後トップ |
| `work-report-form.jsp` | `/work-reports/new` | `/work-reports/new` | 日報登録 |
| `work-report-complete.jsp` | `/work-reports/complete` | `/work-reports/complete` | toast化も検討 |
| `work-report-search.jsp` | `/work-reports/search` | `/work-reports` | 検索一覧 |
| `monthly-report-form.jsp` | `/monthly-reports/new` | `/monthly-reports/export` | 月次Excel出力 |
| `report-history-list.jsp` | `/report-histories` | `/report-histories` | 履歴検索一覧 |
| `report-history-detail.jsp` | `/report-histories/{id}` | `/report-histories/:id` | 履歴詳細 |

## コンポーネント候補

| コンポーネント | 役割 |
|---|---|
| `AppShell` | 認証済み画面の共通レイアウト |
| `GlobalHeader` | システム名、ログインユーザー、ログアウト |
| `SidebarNav` | ホーム、日報登録、実績検索、帳票出力、履歴 |
| `ErrorList` | 入力エラー表示 |
| `StatusBadge` | `SUCCESS`, `ERROR`, `PROCESSING` 表示 |
| `DashboardSummaryCard` | ダッシュボード集計カード |
| `RecentActivityTable` | 最近の活動一覧 |
| `WorkReportForm` | 作業日報登録 |
| `WorkReportSearchForm` | 作業実績検索条件 |
| `WorkReportTable` | 作業実績一覧 |
| `MonthlyReportExportForm` | 月次報告書出力条件 |
| `ReportHistorySearchForm` | 帳票履歴検索条件 |
| `ReportHistoryTable` | 帳票履歴一覧 |
| `ReportHistoryDetail` | 帳票履歴詳細 |
| `DownloadButton` | Blobダウンロード |

## 状態管理

| 状態 | 内容 |
|---|---|
| 認証状態 | `currentUser`, `roleCode`, `departmentName`, `employeeName` |
| 共通マスタ | 作業分類、帳票種別、ステータス、出力対象ユーザー |
| フォーム状態 | 日報登録、実績検索、月次出力、履歴検索 |
| 一覧状態 | 検索条件、結果、ページング、ソート |
| ダウンロード状態 | 出力中、取得中、成功、エラー |

小規模であれば React Context + TanStack Query を初期案とします。画面横断の編集状態や複雑なUI状態が増える場合は Zustand などを追加検討します。

## API通信

- APIクライアントは1箇所に集約します。
- Cookie認証案では `credentials: "include"` を共通設定にします。
- CSRFトークンはログイン後または初回アクセス時に取得し、POST/PUT/PATCH/DELETEへ `X-CSRF-TOKEN` を付与します。
- ExcelダウンロードはBlobとして受け取り、`Content-Disposition` のファイル名を使って保存します。

## 画面移行順序

1. ログイン、ログアウト、ログインユーザー取得。
2. `AppShell`、サイドバー、ヘッダー。
3. ダッシュボード。
4. 作業日報登録。
5. 作業実績検索。
6. 月次報告書出力。
7. 帳票履歴一覧、詳細、再ダウンロード。
8. ホーム、登録完了画面の扱い整理。
9. デザイン画像との差分吸収。

## デザイン移行観点

- `docs/designs/` の画像をReact実装時の見た目確認に使います。
- 現行JSPよりデザイン画像のほうが高機能な項目があるため、初期リリースで再現する範囲を決めます。
- 一覧テーブルは列数が多いため、横スクロール、列固定、レスポンシブ表示を検討します。
- `ADMIN` と `USER` で入力可能項目が変わるため、表示制御とAPI側認可を両方実装します。

## UXリスク

- 月次帳票出力が長時間化する場合、同期ダウンロードでは利用者が処理中か失敗か判断しにくくなります。
- `PROCESSING` ステータスがあるため、将来的には非同期ジョブ化と履歴画面での状態更新が自然です。
- 登録完了画面をtoastへ置き換えると画面遷移は軽くなりますが、業務操作の完了確認として専用画面を残す価値があります。
