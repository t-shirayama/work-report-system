SELECT COUNT(*)
FROM users u
WHERE u.role_code = 'USER'
  AND NOT EXISTS (
    SELECT 1
    FROM report_output_histories roh
    WHERE roh.target_user_id = u.user_id
      AND roh.target_year_month = TO_CHAR(SYSDATE, 'YYYYMM')
      AND roh.report_type = 'MONTHLY_WORK_REPORT'
      AND roh.status = 'SUCCESS'
)
