SELECT
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
    SUM(wr.work_hours) AS total_hours
FROM work_reports wr
INNER JOIN users u
    ON wr.user_id = u.user_id
INNER JOIN departments d
    ON wr.department_id = d.department_id
WHERE wr.work_date >= :dateFrom
  AND wr.work_date <= :dateTo
  AND wr.user_id = :userId
GROUP BY wr.work_category
ORDER BY wr.work_category
