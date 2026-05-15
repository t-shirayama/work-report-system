# Excel帳票出力

このドキュメントでは、月次報告書Excel出力機能の構成と処理の流れを説明します。

月次報告書出力は、入力条件の受け取り、DB検索、集計、Excelテンプレートへの差し込み、ファイル保存、履歴登録、ブラウザダウンロードがつながる機能です。Controller / Service / DAO / POI処理の役割分担を整理します。

## 対象機能

| URL | メソッド | 概要 |
|---|---|---|
| `/monthly-reports/new` | GET | 月次報告書出力画面を表示 |
| `/monthly-reports/export` | POST | Excel帳票を作成してダウンロード |

## テンプレート

テンプレートファイルは以下に配置します。

```text
src/main/resources/templates/monthly-report-template.xlsx
```

テンプレートには、基本情報、月次サマリー、作業分類別集計、日別作業実績の見出しと、明細行コピー用のテンプレート行を用意しています。

## レイヤ構成

| クラス | 役割 |
|---|---|
| `MonthlyReportController` | 入力画面表示、Excelダウンロードレスポンス作成 |
| `MonthlyReportService` | 入力チェック、対象年月の期間計算、帳票データ作成 |
| `MonthlyReportDao` | 月次サマリー、作業分類別集計、日別実績のSQL実行 |
| `ExcelReportService` | Apache POIでテンプレートExcelに値を差し込む |
| `ReportHistoryService` | 帳票作成履歴の登録、検索、詳細取得、再ダウンロード用確認 |
| `ReportHistoryDao` | `report_output_histories` へのINSERT、検索、詳細取得 |

Excel作成と履歴管理は関心が異なるため、`ExcelReportService` と `ReportHistoryService` に分けています。これにより、帳票レイアウトの変更と履歴一覧の変更を別々に追いやすくしています。

## 出力内容

月次報告書には以下を出力します。

### 基本情報

- 対象年月
- 社員名
- 部署名
- 作成日

### 月次サマリー

- 総作業時間
- 稼働日数
- 1日平均作業時間

### 作業分類別集計

- 作業分類
- 合計時間
- 割合

### 日別作業実績

- 日付
- 曜日
- プロジェクト名
- 作業分類
- 作業時間
- 作業内容

## SQLの考え方

`MonthlyReportDao` では、`work_reports`、`users`、`departments` をJOINし、対象年月、部署名、社員名で絞り込みます。

日付範囲はServiceで対象月の1日から月末日までを計算し、DAOへ渡します。

```sql
WHERE wr.work_date >= :dateFrom
  AND wr.work_date <= :dateTo
  AND d.department_name = :departmentName
  AND u.employee_name = :employeeName
```

作業分類別集計は `GROUP BY wr.work_category`、稼働日数は `COUNT(DISTINCT wr.work_date)` で算出します。

## ダウンロード処理

Controllerでは、Excelのレスポンスヘッダーを設定します。

```java
response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
response.setHeader("Content-Disposition", buildContentDisposition(fileName));
```

日本語ファイル名の文字化けを避けるため、`URLEncoder` でUTF-8エンコードした `filename*` を付与します。

ファイル名には社員名を含めますが、保存前に `FileNameUtils` でパス区切り文字、制御文字、`..` などを安全な文字へ置換します。これにより、入力値によって `generated-reports/` の外へ保存されることを防ぎます。

ファイル名例:

```text
月次報告書_202605_山田太郎.xlsx
```

## 履歴保存

月次報告書を出力したときは、`report_output_histories` に帳票作成履歴を保存します。

保存する主な内容は以下です。

- 出力ユーザー
- 対象年月
- 帳票種別
- ファイル名
- ファイルパス
- ステータス
- エラーメッセージ
- 出力日時

帳票種別は当面 `MONTHLY_WORK_REPORT` を使用します。ステータスは以下を想定します。

| ステータス | 意味 |
|---|---|
| `SUCCESS` | Excel作成・保存に成功。画面では「完了」と表示 |
| `ERROR` | Excel作成または保存に失敗 |
| `PROCESSING` | 作成中。画面では「処理中」と表示 |

出力開始時は履歴を `PROCESSING` として登録します。処理中の履歴にも、予定ファイル名と予定保存先を保持します。

成功時はExcelファイルを `generated-reports/` 配下へ保存し、同じ履歴を `SUCCESS`、保存先パス、`error_message = null` へ更新します。

失敗時は同じ履歴を `ERROR` として更新し、エラーメッセージを `error_message` に登録します。ファイル保存後に失敗した場合は、生成済みファイルの削除を試みます。削除に失敗した場合も元の例外を優先しつつ、ログへ警告を残します。

現在のExcel出力は同期処理です。非同期ジョブ化はしていませんが、履歴は以下の状態遷移で管理します。

1. 履歴を `PROCESSING` で作成する
2. Excel生成とファイル保存に成功したら `SUCCESS` へ更新する
3. 失敗したら `ERROR` と `error_message` へ更新する
4. ファイル削除失敗もログに残す

同一条件の二重実行や同名ファイルの扱いは、運用環境に合わせて今後ルール化します。

履歴を保存する理由は、利用者が後から同じ帳票を再取得できるようにするためです。また、いつ、誰が、どの対象年月の帳票を作成したかを残すことで、業務上の証跡にもなります。

## 再ダウンロード

帳票作成履歴一覧は以下のURLで表示します。

```text
http://localhost:8080/work-report-system/report-histories
```

成功済みの履歴にはダウンロードボタンを表示します。

履歴一覧では、対象年月、帳票種別、作成者、ステータスで検索できます。条件が指定された場合のみWHERE句に追加し、`NamedParameterJdbcTemplate` のバインド変数として値を渡します。

履歴詳細は以下のURLで表示します。

```text
GET /report-histories/{id}
```

詳細画面では、ファイルパスやエラーメッセージを確認できます。`SUCCESS` は「完了」、`ERROR` は「エラー」、`PROCESSING` は「処理中」として表示します。

帳票作成履歴はログインユーザーの権限に応じて参照範囲を制限します。`ADMIN` は全件を検索できますが、`USER` は自分が作成した履歴だけを検索・詳細表示・再ダウンロードできます。

```text
GET /report-histories/{id}/download
```

再ダウンロード処理では、履歴IDから `report_output_histories` を検索し、`file_path` に保存されたExcelファイルを読み込んでブラウザへ返します。

ファイルが存在しない場合、ステータスが `SUCCESS` ではない場合、または保存先パスが `generated-reports/` 配下ではない場合は、一覧画面にエラーメッセージを表示します。

## 例外とログ出力の考え方

Excel出力では、DB検索、テンプレート読み込み、Excel書き込み、ファイル保存、レスポンス出力の各段階で例外が発生する可能性があります。

ログには、対象年月、部署、社員、出力ユーザーID、ファイル名、履歴ID、例外内容を残すと調査しやすくなります。ただし、パスワードなどの秘密情報は出力しません。

利用者向けには、詳細なスタックトレースではなく「帳票の作成に失敗しました」のような分かりやすいメッセージを表示します。詳細はログと `report_output_histories.error_message` から確認する方針です。

## 今後の改善案

- 部署・社員のプルダウン化
- テンプレート差し替え機能
- 帳票レイアウトの詳細調整
- 出力対象データが0件の場合の警告表示
- 履歴一覧のページング、並び替え
- 古い帳票ファイルの削除運用
