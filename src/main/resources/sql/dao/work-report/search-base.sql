SELECT
    TO_CHAR(wr.work_date, 'YYYY/MM/DD') AS work_date,
    u.employee_name,
    d.department_name,
    wr.project_name,
    wr.work_category,
    CASE wr.work_category
        WHEN 'DESIGN' THEN '設計'
        WHEN 'DEVELOPMENT' THEN '開発'
        WHEN 'TEST' THEN 'テスト'
        WHEN 'MEETING' THEN '会議'
        WHEN 'DOCUMENT' THEN '資料作成'
        WHEN 'OTHER' THEN 'その他'
        ELSE wr.work_category
    END AS work_category_name,
    wr.work_hours,
    wr.work_content
FROM work_reports wr
INNER JOIN users u
    ON wr.user_id = u.user_id
INNER JOIN departments d
    ON wr.department_id = d.department_id
WHERE 1 = 1
