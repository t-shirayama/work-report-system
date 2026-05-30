# 04. API設計計画

## API設計方針

- Reactから利用しやすいJSON APIとして設計します。
- ControllerはHTTP入出力に集中し、業務判断はApplication Serviceへ置きます。
- 権限制御はAPI側で必ず実施します。
- 検索条件はクエリ文字列、登録や出力指示はJSON bodyを基本にします。
- ExcelダウンロードはBlobレスポンスとして扱います。
- エラー形式を統一し、React側で表示しやすくします。

主要APIの処理順序は [09-api-processing-flow.md](09-api-processing-flow.md) にMermaidで整理します。

## 認証API

| Method | Path | 内容 |
|---|---|---|
| `POST` | `/api/auth/login` | ログインID、パスワードで認証しCookieを発行 |
| `POST` | `/api/auth/logout` | ログアウト、Cookie破棄 |
| `GET` | `/api/auth/me` | ログインユーザー、ロール、部署を取得 |
| `GET` | `/api/auth/csrf` | Antiforgery tokenを取得 |

## 業務API候補

| Method | Path | 内容 | 権限 |
|---|---|---|---|
| `GET` | `/api/dashboard` | ダッシュボード集計 | 認証済み |
| `POST` | `/api/work-reports` | 作業日報登録 | 認証済み |
| `GET` | `/api/work-reports` | 作業実績検索 | 認証済み |
| `GET` | `/api/monthly-reports/options` | 月次出力画面の選択肢 | 認証済み |
| `GET` | `/api/users/report-targets` | 管理者向け帳票対象ユーザー一覧 | `ADMIN` |
| `POST` | `/api/monthly-reports/export` | 月次報告書を作成してダウンロード | 認証済み |
| `GET` | `/api/report-histories` | 帳票履歴検索 | 認証済み |
| `GET` | `/api/report-histories/{id}` | 帳票履歴詳細 | 認証済み |
| `GET` | `/api/report-histories/{id}/download` | 帳票再ダウンロード | 認証済み |

## リクエスト例

### 作業日報登録

```json
{
  "workDate": "2026-05-31",
  "projectName": "作業日報システム",
  "workCategory": "DEVELOPMENT",
  "workHours": 7.5,
  "workContent": "API設計と画面移行計画の作成"
}
```

### 作業実績検索

```text
GET /api/work-reports?dateFrom=2026-05-01&dateTo=2026-05-31&employeeName=佐藤&departmentName=開発部&workCategory=DEVELOPMENT&projectName=作業日報&page=1&pageSize=50
```

### 月次報告書出力

```json
{
  "targetYear": 2026,
  "targetMonth": 5,
  "userId": 2
}
```

## エラー形式

```json
{
  "code": "VALIDATION_ERROR",
  "message": "入力内容を確認してください。",
  "details": [
    {
      "field": "workHours",
      "message": "作業時間は0より大きい数値で入力してください。"
    }
  ],
  "traceId": "00-..."
}
```

| HTTPステータス | 用途 |
|---|---|
| `400` | 入力形式不正 |
| `401` | 未認証 |
| `403` | 権限不足 |
| `404` | 対象データなし |
| `409` | 業務的な競合、状態不整合 |
| `500` | 想定外エラー |

他人の履歴や帳票ファイルへのアクセスは、情報秘匿を優先する場合 `403` ではなく `404` を返す方針も検討します。

## 権限制御

| API | `ADMIN` | `USER` |
|---|---|---|
| `GET /api/work-reports` | 全ユーザー検索可 | 自分の実績のみ |
| `POST /api/work-reports` | 自分の実績として登録 | 自分の実績として登録 |
| `POST /api/monthly-reports/export` | 一般ユーザーを対象に出力可 | 自分のみ出力可 |
| `GET /api/report-histories` | 全履歴検索可 | 自分が対象の履歴のみ |
| `GET /api/report-histories/{id}` | 全履歴参照可 | 自分が対象の履歴のみ |
| `GET /api/report-histories/{id}/download` | 全成功履歴を取得可 | 自分が対象の成功履歴のみ |

## 実装順序

1. `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me`
2. 共通エラー形式、入力検証、認可Policy
3. `GET /api/dashboard`
4. `POST /api/work-reports`
5. `GET /api/work-reports`
6. `GET /api/monthly-reports/options`, `GET /api/users/report-targets`
7. `POST /api/monthly-reports/export`
8. `GET /api/report-histories`, `GET /api/report-histories/{id}`
9. `GET /api/report-histories/{id}/download`

## API設計上の未決事項

- Cookie認証かJWT認証か。
- EF Core、Dapper、ADO.NETのどれを採用するか。
- 月次帳票出力を同期ダウンロードにするか、非同期ジョブ + 履歴画面更新にするか。
- ページング、ソート、CSV出力など現行にない機能を初期スコープへ含めるか。
- `home` と登録完了画面をReactで独立ページにするか、ログイン誘導やtoastへ統合するか。
