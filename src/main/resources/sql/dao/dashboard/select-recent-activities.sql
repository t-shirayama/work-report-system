SELECT *
FROM (
    SELECT
        activity_date,
        activity_at,
        activity_type,
        activity_type_name,
        badge_class,
        content,
        employee_name
    FROM (
        SELECT
            wr.created_at AS activity_date,
            TO_CHAR(wr.created_at, 'YYYY/MM/DD HH24:MI') AS activity_at,
            'WORK_REPORT' AS activity_type,
            '登録' AS activity_type_name,
            'blue' AS badge_class,
            '作業日報を登録しました（' || TO_CHAR(wr.work_date, 'YYYY/MM/DD') || '）' AS content,
            u.employee_name
        FROM work_reports wr
        INNER JOIN users u
            ON wr.user_id = u.user_id
        UNION ALL
        SELECT
            roh.created_at AS activity_date,
            TO_CHAR(roh.created_at, 'YYYY/MM/DD HH24:MI') AS activity_at,
            'REPORT_OUTPUT' AS activity_type,
            CASE roh.status
                WHEN 'ERROR' THEN 'エラー'
                WHEN 'PROCESSING' THEN '処理中'
                ELSE '出力'
            END AS activity_type_name,
            CASE roh.status
                WHEN 'ERROR' THEN 'red'
                WHEN 'PROCESSING' THEN 'purple'
                ELSE 'green'
            END AS badge_class,
            CASE roh.status
                WHEN 'ERROR' THEN '月次報告書の出力に失敗しました（' || SUBSTR(roh.target_year_month, 1, 4) || '年' || TO_NUMBER(SUBSTR(roh.target_year_month, 5, 2)) || '月分）'
                WHEN 'PROCESSING' THEN '月次報告書を作成中です（' || SUBSTR(roh.target_year_month, 1, 4) || '年' || TO_NUMBER(SUBSTR(roh.target_year_month, 5, 2)) || '月分）'
                ELSE '月次報告書を出力しました（' || SUBSTR(roh.target_year_month, 1, 4) || '年' || TO_NUMBER(SUBSTR(roh.target_year_month, 5, 2)) || '月分）'
            END AS content,
            u.employee_name
        FROM report_output_histories roh
        INNER JOIN users u
            ON roh.created_by = u.user_id
    ) activities
    ORDER BY activity_date DESC
)
WHERE ROWNUM <= 6
