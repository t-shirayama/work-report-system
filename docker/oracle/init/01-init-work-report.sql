WHENEVER SQLERROR EXIT SQL.SQLCODE

ALTER SESSION SET CONTAINER = FREEPDB1;
ALTER SESSION SET CURRENT_SCHEMA = WORK_REPORT;

@@/opt/work-report-system/sql/schema.sql
@@/opt/work-report-system/sql/sample-data.sql

EXIT;
