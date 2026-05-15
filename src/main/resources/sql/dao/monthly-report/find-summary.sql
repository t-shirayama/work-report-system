SELECT
    :targetYearMonth AS target_year_month,
    MAX(u.employee_name) AS employee_name,
    MAX(d.department_name) AS department_name,
    NVL(SUM(wr.work_hours), 0) AS total_work_hours,
    COUNT(DISTINCT wr.work_date) AS work_days
FROM work_reports wr
INNER JOIN users u
    ON wr.user_id = u.user_id
INNER JOIN departments d
    ON wr.department_id = d.department_id
WHERE wr.work_date >= :dateFrom
  AND wr.work_date <= :dateTo
  AND wr.user_id = :userId
