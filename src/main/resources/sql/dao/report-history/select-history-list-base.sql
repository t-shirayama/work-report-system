SELECT
    roh.report_output_history_id,
    TO_CHAR(roh.created_at, 'YYYY/MM/DD HH24:MI') AS output_at,
    roh.target_year_month,
    roh.report_type,
    CASE roh.report_type
        WHEN 'MONTHLY_WORK_REPORT' THEN '月次作業報告書'
        ELSE roh.report_type
    END AS report_type_name,
    roh.created_by,
    u.employee_name AS created_by_name,
    roh.target_user_id,
    target_user.employee_name AS target_user_name,
    roh.status,
    CASE roh.status
        WHEN 'SUCCESS' THEN '完了'
        WHEN 'ERROR' THEN 'エラー'
        WHEN 'PROCESSING' THEN '処理中'
        ELSE roh.status
    END AS status_name,
    roh.file_name,
    roh.file_path,
    roh.error_message
FROM report_output_histories roh
INNER JOIN users u
    ON roh.created_by = u.user_id
INNER JOIN users target_user
    ON roh.target_user_id = target_user.user_id
