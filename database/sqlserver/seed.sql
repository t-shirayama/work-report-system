SET IDENTITY_INSERT departments ON;
INSERT INTO departments (department_id, department_code, department_name, display_order) VALUES
(1, N'DEV', N'開発部', 1),
(2, N'QA', N'品質管理部', 2),
(3, N'PMO', N'プロジェクト管理部', 3);
SET IDENTITY_INSERT departments OFF;

SET IDENTITY_INSERT users ON;
INSERT INTO users (user_id, department_id, login_id, password, employee_name, role_code) VALUES
(1, 1, N'admin', N'$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'管理 太郎', N'ADMIN'),
(2, 1, N'sato', N'$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'佐藤 花子', N'USER'),
(3, 1, N'suzuki', N'$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'鈴木 一郎', N'USER'),
(4, 2, N'tanaka', N'$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'田中 美咲', N'USER'),
(5, 3, N'yamada', N'$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'山田 健', N'USER');
SET IDENTITY_INSERT users OFF;

SET IDENTITY_INSERT work_reports ON;
INSERT INTO work_reports (work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content) VALUES
(1, 2, 1, '2026-05-01', N'作業日報システム', N'DESIGN', 3.50, N'画面遷移と入力項目の整理'),
(2, 2, 1, '2026-05-01', N'作業日報システム', N'MEETING', 1.50, N'要件確認ミーティング'),
(3, 3, 1, '2026-05-01', N'月次帳票改善', N'DEVELOPMENT', 5.00, N'帳票出力処理の調査'),
(4, 4, 2, '2026-05-01', N'受入テスト支援', N'TEST', 6.00, N'テスト観点表の作成'),
(5, 5, 3, '2026-05-01', N'進捗管理', N'DOCUMENT', 2.50, N'週次報告資料の更新'),
(6, 2, 1, '2026-05-08', N'作業日報システム', N'DEVELOPMENT', 7.00, N'登録処理のバリデーション検討'),
(7, 3, 1, '2026-05-08', N'月次帳票改善', N'DEVELOPMENT', 6.00, N'帳票履歴テーブルの設計確認'),
(8, 4, 2, '2026-05-08', N'受入テスト支援', N'TEST', 6.50, N'障害票の再現確認');
SET IDENTITY_INSERT work_reports OFF;

SET IDENTITY_INSERT report_output_histories ON;
INSERT INTO report_output_histories (
    report_output_history_id, target_year_month, created_by, target_user_id,
    report_type, file_name, file_path, status, error_message
) VALUES
(1, '202604', 2, 2, N'MONTHLY_WORK_REPORT', N'monthly-report-202604-sato.xlsx', N'generated-reports/202604/monthly-report-202604-sato.xlsx', N'SUCCESS', NULL),
(2, '202605', 3, 3, N'MONTHLY_WORK_REPORT', N'monthly-report-202605-suzuki.xlsx', N'generated-reports/202605/monthly-report-202605-suzuki.xlsx', N'PROCESSING', NULL),
(3, '202605', 4, 4, N'MONTHLY_WORK_REPORT', N'monthly-report-202605-tanaka.xlsx', N'generated-reports/202605/monthly-report-202605-tanaka.xlsx', N'ERROR', N'テンプレートファイルが見つかりません。');
SET IDENTITY_INSERT report_output_histories OFF;
