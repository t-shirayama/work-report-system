# API設計

Base URL: `/api`

## 認証

| Method | Path | 説明 | 認証 |
|---|---|---|---|
| GET | `/auth/csrf` | CSRFトークン取得 | 不要 |
| POST | `/auth/login` | ログイン | 不要 |
| POST | `/auth/logout` | ログアウト | 必要 |
| GET | `/auth/me` | ログインユーザー取得 | 必要 |

`POST /auth/login`

```json
{
  "loginId": "admin",
  "password": "password"
}
```

## ダッシュボード

| Method | Path | 説明 | 認証 |
|---|---|---|---|
| GET | `/dashboard` | 本日登録件数、当月時間、未出力帳票、最近の活動 | 必要 |

## 作業日報

| Method | Path | 説明 | 認証 |
|---|---|---|---|
| POST | `/work-reports` | 作業日報登録 | 必要 |
| GET | `/work-reports` | 作業実績検索 | 必要 |

`POST /work-reports`

```json
{
  "workDate": "2026-05-31",
  "projectName": "作業日報システム",
  "workCategory": "DEVELOPMENT",
  "workHours": 7.5,
  "workContent": "API移行"
}
```

検索条件:

| Query | 説明 |
|---|---|
| `dateFrom` | 作業日From |
| `dateTo` | 作業日To |
| `employeeName` | 社員名部分一致 |
| `departmentName` | 部署名部分一致 |
| `workCategory` | 作業分類 |
| `projectName` | プロジェクト名部分一致 |

## 月次帳票

| Method | Path | 説明 | 認証 |
|---|---|---|---|
| GET | `/monthly-reports/target-users` | 帳票対象ユーザー一覧 | 管理者 |
| POST | `/monthly-reports/export` | 月次報告書Excel出力 | 必要 |

`POST /monthly-reports/export`

```json
{
  "targetYearMonth": "202605",
  "targetUserId": 2
}
```

レスポンスは `.xlsx` のBlobです。

## 帳票履歴

| Method | Path | 説明 | 認証 |
|---|---|---|---|
| GET | `/report-histories` | 帳票履歴検索 | 必要 |
| GET | `/report-histories/{id}` | 帳票履歴詳細 | 必要 |
| GET | `/report-histories/{id}/download` | 帳票再ダウンロード | 必要 |

## マスタ管理

すべて管理者権限が必要です。

| Method | Path | 説明 | 認証 |
|---|---|---|---|
| GET | `/master/departments` | 部署一覧 | 管理者 |
| POST | `/master/departments` | 部署追加 | 管理者 |
| PUT | `/master/departments/{id}` | 部署更新 | 管理者 |
| GET | `/master/users` | ユーザー一覧 | 管理者 |
| POST | `/master/users` | ユーザー追加 | 管理者 |
| PUT | `/master/users/{id}` | ユーザー更新 | 管理者 |

`POST /master/departments`

```json
{
  "departmentCode": "OPS",
  "departmentName": "運用部",
  "displayOrder": 10
}
```

`POST /master/users`

```json
{
  "departmentId": 1,
  "loginId": "ito",
  "password": "password",
  "employeeName": "伊藤 次郎",
  "roleCode": "USER"
}
```

## エラー

入力チェックエラーは以下の形式で返します。

```json
{
  "errors": [
    "作業日は必須です。"
  ]
}
```
