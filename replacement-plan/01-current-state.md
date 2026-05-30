# 01. 現行システム棚卸し

## 概要

現行 `work-report-system` は、作業日報登録と月次作業報告書Excel出力を行うSpring MVC業務システムです。Tomcat 8.5上のMaven WAR、JSP / JSTL画面、Spring Security、Spring JDBC、Oracle Database、Apache POI 3.17を前提にしています。

## 画面一覧

| 画面 | 現行URL | JSP | 主な役割 |
|---|---|---|---|
| ホーム | `/home` | `home.jsp` | 未認証で表示できる案内画面 |
| ログイン | `/login` | `login.jsp` | ログインID、パスワードによる認証 |
| ダッシュボード | `/dashboard` | `dashboard.jsp` | 本日登録件数、今月作業時間、未出力件数、最近の活動 |
| 作業日報登録 | `/work-reports/new` | `work-report-form.jsp` | 作業日、プロジェクト、分類、時間、内容を登録 |
| 作業日報登録完了 | `/work-reports/complete` | `work-report-complete.jsp` | 登録完了内容を表示 |
| 作業実績検索 | `/work-reports/search` | `work-report-search.jsp` | 期間、社員、部署、分類、プロジェクトで検索 |
| 月次報告書出力 | `/monthly-reports/new` | `monthly-report-form.jsp` | 対象年月、対象社員を指定してExcel出力 |
| 帳票作成履歴 | `/report-histories` | `report-history-list.jsp` | 帳票履歴を検索、一覧表示 |
| 帳票作成履歴詳細 | `/report-histories/{id}` | `report-history-detail.jsp` | 出力結果、保存先、エラー内容を確認 |
| 帳票再ダウンロード | `/report-histories/{id}/download` | なし | 成功済みExcelファイルを取得 |

## 主要機能

| 機能 | 現行仕様 |
|---|---|
| 認証 | Spring Security Form Login、BCrypt、セッション認証 |
| CSRF | POSTフォームにSpring SecurityのCSRF hidden tokenを付与 |
| 権限 | `ADMIN` と `USER` |
| ダッシュボード | `work_reports` と `report_output_histories` から集計 |
| 作業日報登録 | 入力チェック後、ログインユーザーの `user_id` と `department_id` で登録 |
| 作業実績検索 | `ADMIN` は全件、`USER` は自分の実績のみ検索 |
| 月次帳票出力 | 対象月の作業実績を集計し、Excelテンプレートへ差し込み |
| 帳票履歴 | `PROCESSING`, `SUCCESS`, `ERROR` を管理 |
| 再ダウンロード | `generated-reports/` 配下の成功済みファイルのみ返却 |

## 入力チェック

| 対象 | 主なチェック |
|---|---|
| 作業日報登録 | 作業日必須、日付形式、プロジェクト名100文字以内、作業分類固定値、作業時間0超24以下、作業内容1000文字以内 |
| 作業実績検索 | From/Toの日付形式、From <= To、作業分類固定値 |
| 月次報告書出力 | 対象年4桁、対象月1から12、`ADMIN`は対象社員必須、`USER`は自分のみ |
| 帳票履歴検索 | 対象年月、帳票種別、作成者、ステータスによる検索 |

## DBテーブル

| テーブル | 役割 |
|---|---|
| `departments` | 部署マスタ |
| `users` | ログインユーザー、所属部署、権限、BCryptパスワード |
| `work_reports` | 日々の作業実績 |
| `report_output_histories` | 帳票出力履歴、対象年月、対象者、ファイル名、保存先、ステータス |

## 帳票仕様

| 項目 | 内容 |
|---|---|
| 帳票ID | `MONTHLY_WORK_REPORT` |
| 帳票名 | 月次作業報告書 |
| 形式 | Excel `.xlsx` |
| テンプレート | `src/main/resources/templates/monthly-report-template.xlsx` |
| 出力内容 | 対象年月、社員名、部署名、作成日、総作業時間、稼働日数、平均作業時間、分類別集計、日別明細 |
| 保存先 | `generated-reports/{YYYYMM}/` |
| ファイル名 | `月次報告書_{YYYYMM}_{社員名}_履歴ID{id}.xlsx` |
| 0件時 | 対象データが0件でもExcelを作成する方針 |

## 認証認可

| 対象 | 現行制御 |
|---|---|
| 未認証許可 | `/home`, `/login`, `/resources/**` |
| 認証必須 | 上記以外の全URL |
| ログイン成功 | `/dashboard` へ遷移 |
| ログアウト | セッション破棄、`JSESSIONID` 削除 |
| 一般ユーザー | 自分の作業実績、自分が対象の帳票履歴のみ参照 |
| 管理者 | 全ユーザーの作業実績、帳票履歴を参照可能 |
| 月次出力 | `USER` は自分のみ、`ADMIN` は一般ユーザーを選択可能 |
| 帳票詳細 | 内部ファイルパスは管理者のみ表示 |

## 現行仕様からの注意点

- JSPのModel表示、入力フォーム、権限制御をReact向けAPI仕様へ分解する必要があります。
- Oracle固有SQL、日付関数、採番、正規表現制約をSQL Server向けに再設計します。
- Excelテンプレートのセル位置依存があるため、見た目と値の互換検証が必要です。
- `PROCESSING` のまま残る帳票履歴の扱いは運用設計で決めます。
- 作業日報の編集、削除、承認、ページングは現行では未実装または拡張候補として扱います。
