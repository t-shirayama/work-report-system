-- Oracle Database sample data for work-report-system.
-- Execute after schema.sql.
-- User passwords are BCrypt hashes. The initial password for sample users is 'password'.

INSERT INTO departments (
    department_id, department_code, department_name, display_order, created_at, updated_at
) VALUES (
    1, 'DEV', '開発部', 1, SYSDATE, SYSDATE
);

INSERT INTO departments (
    department_id, department_code, department_name, display_order, created_at, updated_at
) VALUES (
    2, 'QA', '品質管理部', 2, SYSDATE, SYSDATE
);

INSERT INTO departments (
    department_id, department_code, department_name, display_order, created_at, updated_at
) VALUES (
    3, 'PMO', 'プロジェクト管理部', 3, SYSDATE, SYSDATE
);

INSERT INTO users (
    user_id, department_id, login_id, password, employee_name, role_code, created_at, updated_at
) VALUES (
    1, 1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '管理 太郎', 'ADMIN', SYSDATE, SYSDATE
);

INSERT INTO users (
    user_id, department_id, login_id, password, employee_name, role_code, created_at, updated_at
) VALUES (
    2, 1, 'sato', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '佐藤 花子', 'USER', SYSDATE, SYSDATE
);

INSERT INTO users (
    user_id, department_id, login_id, password, employee_name, role_code, created_at, updated_at
) VALUES (
    3, 1, 'suzuki', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '鈴木 一郎', 'USER', SYSDATE, SYSDATE
);

INSERT INTO users (
    user_id, department_id, login_id, password, employee_name, role_code, created_at, updated_at
) VALUES (
    4, 2, 'tanaka', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '田中 美咲', 'USER', SYSDATE, SYSDATE
);

INSERT INTO users (
    user_id, department_id, login_id, password, employee_name, role_code, created_at, updated_at
) VALUES (
    5, 3, 'yamada', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '山田 健', 'USER', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    1, 2, 1, DATE '2026-05-01', '作業日報システム', 'DESIGN', 3.50, '画面遷移と入力項目の整理', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    2, 2, 1, DATE '2026-05-01', '作業日報システム', 'MEETING', 1.50, '要件確認ミーティング', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    3, 3, 1, DATE '2026-05-01', '月次帳票改善', 'DEVELOPMENT', 5.00, '帳票出力処理の調査', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    4, 4, 2, DATE '2026-05-01', '受入テスト支援', 'TEST', 6.00, 'テスト観点表の作成', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    5, 5, 3, DATE '2026-05-01', '進捗管理', 'DOCUMENT', 2.50, '週次報告資料の更新', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    6, 2, 1, DATE '2026-05-05', '作業日報システム', 'DEVELOPMENT', 6.00, 'ログイン画面の設計検討', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    7, 3, 1, DATE '2026-05-05', '月次帳票改善', 'DESIGN', 4.00, 'Excelテンプレート項目の洗い出し', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    8, 4, 2, DATE '2026-05-05', '受入テスト支援', 'TEST', 7.00, '検索条件別のテストケース作成', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    9, 5, 3, DATE '2026-05-05', '進捗管理', 'MEETING', 2.00, '課題管理定例の実施', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    10, 2, 1, DATE '2026-05-06', '作業日報システム', 'DEVELOPMENT', 7.50, '日報登録処理の詳細設計', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    11, 3, 1, DATE '2026-05-06', '月次帳票改善', 'DEVELOPMENT', 6.50, 'POI利用箇所のサンプル検証', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    12, 4, 2, DATE '2026-05-06', '受入テスト支援', 'TEST', 5.50, 'テストデータの準備', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    13, 5, 3, DATE '2026-05-06', '進捗管理', 'DOCUMENT', 3.00, '月次報告フォーマット確認', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    14, 2, 1, DATE '2026-05-07', '作業日報システム', 'DEVELOPMENT', 6.00, '検索画面のSQL条件整理', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    15, 3, 1, DATE '2026-05-07', '月次帳票改善', 'TEST', 4.50, '帳票出力結果の確認観点作成', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    16, 4, 2, DATE '2026-05-07', '受入テスト支援', 'MEETING', 1.50, 'テスト進捗会議', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    17, 5, 3, DATE '2026-05-07', '進捗管理', 'OTHER', 2.00, '課題一覧の棚卸し', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    18, 2, 1, DATE '2026-05-08', '作業日報システム', 'DEVELOPMENT', 7.00, '登録処理のバリデーション検討', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    19, 3, 1, DATE '2026-05-08', '月次帳票改善', 'DEVELOPMENT', 6.00, '帳票履歴テーブルの設計確認', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    20, 4, 2, DATE '2026-05-08', '受入テスト支援', 'TEST', 6.50, '障害票の再現確認', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    21, 5, 3, DATE '2026-05-08', '進捗管理', 'DOCUMENT', 4.00, 'リスク管理表の更新', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    22, 2, 1, DATE '2026-05-11', '作業日報システム', 'DESIGN', 3.00, 'ダッシュボード集計項目の整理', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    23, 3, 1, DATE '2026-05-11', '月次帳票改善', 'DEVELOPMENT', 7.00, '月次集計SQLの検討', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    24, 4, 2, DATE '2026-05-11', '受入テスト支援', 'TEST', 7.50, '結合テスト結果の確認', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    25, 5, 3, DATE '2026-05-11', '進捗管理', 'MEETING', 2.50, '月次レビュー準備会議', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    26, 2, 1, DATE '2026-05-12', '作業日報システム', 'DEVELOPMENT', 7.00, 'DAO設計方針の整理', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    27, 3, 1, DATE '2026-05-12', '月次帳票改善', 'DOCUMENT', 2.00, '帳票項目定義書の更新', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    28, 4, 2, DATE '2026-05-12', '受入テスト支援', 'TEST', 6.00, '不具合修正確認', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    29, 5, 3, DATE '2026-05-12', '進捗管理', 'OTHER', 1.50, 'チケット棚卸しと優先度調整', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    30, 2, 1, DATE '2026-05-13', '作業日報システム', 'MEETING', 1.00, '実装方針レビュー', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    31, 3, 1, DATE '2026-05-13', '月次帳票改善', 'DEVELOPMENT', 5.50, '帳票再ダウンロード機能の設計', SYSDATE, SYSDATE
);

INSERT INTO work_reports (
    work_report_id, user_id, department_id, work_date, project_name, work_category, work_hours, work_content, created_at, updated_at
) VALUES (
    32, 4, 2, DATE '2026-05-13', '受入テスト支援', 'DOCUMENT', 3.50, 'テスト結果報告書の作成', SYSDATE, SYSDATE
);

INSERT INTO report_output_histories (
    report_output_history_id, target_year_month, created_by, target_user_id, report_type, file_name, file_path, status, error_message, created_at, updated_at
) VALUES (
    1, '202604', 2, 2, 'MONTHLY_WORK_REPORT', 'monthly-report-202604-sato.xlsx', 'generated-reports/202604/monthly-report-202604-sato.xlsx', 'SUCCESS', NULL, SYSDATE, SYSDATE
);

INSERT INTO report_output_histories (
    report_output_history_id, target_year_month, created_by, target_user_id, report_type, file_name, file_path, status, error_message, created_at, updated_at
) VALUES (
    2, '202604', 3, 3, 'MONTHLY_WORK_REPORT', 'monthly-report-202604-suzuki.xlsx', 'generated-reports/202604/monthly-report-202604-suzuki.xlsx', 'SUCCESS', NULL, SYSDATE, SYSDATE
);

INSERT INTO report_output_histories (
    report_output_history_id, target_year_month, created_by, target_user_id, report_type, file_name, file_path, status, error_message, created_at, updated_at
) VALUES (
    3, '202605', 2, 2, 'MONTHLY_WORK_REPORT', 'monthly-report-202605-sato.xlsx', 'generated-reports/202605/monthly-report-202605-sato.xlsx', 'SUCCESS', NULL, SYSDATE, SYSDATE
);

INSERT INTO report_output_histories (
    report_output_history_id, target_year_month, created_by, target_user_id, report_type, file_name, file_path, status, error_message, created_at, updated_at
) VALUES (
    4, '202605', 3, 3, 'MONTHLY_WORK_REPORT', 'monthly-report-202605-suzuki.xlsx', 'generated-reports/202605/monthly-report-202605-suzuki.xlsx', 'PROCESSING', NULL, SYSDATE, SYSDATE
);

INSERT INTO report_output_histories (
    report_output_history_id, target_year_month, created_by, target_user_id, report_type, file_name, file_path, status, error_message, created_at, updated_at
) VALUES (
    5, '202605', 4, 4, 'MONTHLY_WORK_REPORT', 'monthly-report-202605-tanaka.xlsx', 'generated-reports/202605/monthly-report-202605-tanaka.xlsx', 'ERROR', 'テンプレートファイルが見つかりません。', SYSDATE, SYSDATE
);

COMMIT;
