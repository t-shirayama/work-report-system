# AGENTS.md

このリポジトリは React + ASP.NET Core Web API + SQL Server の業務システムです。

## 固定スタック

- フロントエンドは React + TypeScript + Vite を使用する。
- バックエンドは C# ASP.NET Core Web API を使用する。
- DBは SQL Server を前提にする。
- DBアクセスは Dapper と `Microsoft.Data.SqlClient` を基本にする。
- 認証は Cookie認証、CSRF対策は ASP.NET Core Antiforgery を基本にする。
- 帳票出力は ClosedXML を使用する。
- テストは xUnit を使用する。

## 作業前に読む場所

- 全体索引: `docs/README.md`
- アーキテクチャ: `docs/architecture.md`
- API設計: `docs/api.md`
- DB設計: `docs/database.md`
- テスト方針: `docs/testing.md`
- 運用メモ: `docs/operations.md`

## 実装ルール

- API Endpoint、Application Service、Repository、Reporting の責務を分離する。
- EndpointにはSQLや重い業務判断を書かない。
- Application Serviceに入力チェック、業務判断、権限判断、トランザクション境界を置く。
- RepositoryにSQL Server向けSQL、バインド変数、DB例外に近い処理を置く。
- ユーザー入力をSQL文字列へ直接連結せず、Dapperのパラメータを使う。
- Reactコンポーネントは画面状態と表示に寄せ、API仕様は `frontend/src/api.ts` に集約する。
- unsafe methodでは `X-CSRF-TOKEN` を付与する。
- 帳票ファイル名、保存パス、履歴IDはAPI側で管理する。
- 生成物、IDE固有ファイル、DBデータファイルはコミットしない。

## ドキュメント更新

- APIを変更したら `docs/api.md` を更新する。
- DB、DDL、SQLを変更したら `docs/database.md` を更新する。
- レイヤ構成や責務を変更したら `docs/architecture.md` を更新する。
- テストを追加・変更したら `docs/testing.md` を更新する。
- 起動・運用手順を変えたら `README.md` と `docs/operations.md` を更新する。

## 検証ルール

- C#変更後は可能な限り `dotnet test WorkReport.slnx` を実行する。
- フロントエンド変更後は可能な限り `npm run build` を `frontend/` で実行する。
- DB関連変更では、SQL Server起動後の確認手順も最終報告に含める。
