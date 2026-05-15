-- Oracle Database schema for work-report-system.
-- Target: Oracle Database 11g / 12c / 19c.
-- This script intentionally avoids ORM-specific definitions.

CREATE TABLE departments (
    department_id      NUMBER(10)      NOT NULL,
    department_code    VARCHAR2(20)    NOT NULL,
    department_name    VARCHAR2(100)   NOT NULL,
    display_order      NUMBER(5)       DEFAULT 0 NOT NULL,
    created_at         DATE            DEFAULT SYSDATE NOT NULL,
    updated_at         DATE            DEFAULT SYSDATE NOT NULL,
    CONSTRAINT pk_departments PRIMARY KEY (department_id),
    CONSTRAINT uk_departments_code UNIQUE (department_code)
);

CREATE TABLE users (
    user_id            NUMBER(10)      NOT NULL,
    department_id      NUMBER(10)      NOT NULL,
    login_id           VARCHAR2(50)    NOT NULL,
    password           VARCHAR2(255)   NOT NULL,
    employee_name      VARCHAR2(100)   NOT NULL,
    role_code          VARCHAR2(20)    NOT NULL,
    created_at         DATE            DEFAULT SYSDATE NOT NULL,
    updated_at         DATE            DEFAULT SYSDATE NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uk_users_login_id UNIQUE (login_id),
    CONSTRAINT fk_users_department
        FOREIGN KEY (department_id) REFERENCES departments (department_id),
    CONSTRAINT ck_users_role_code
        CHECK (role_code IN ('ADMIN', 'USER'))
);

CREATE TABLE work_reports (
    work_report_id     NUMBER(10)      NOT NULL,
    user_id            NUMBER(10)      NOT NULL,
    department_id      NUMBER(10)      NOT NULL,
    work_date          DATE            NOT NULL,
    project_name       VARCHAR2(100)   NOT NULL,
    work_category      VARCHAR2(30)    NOT NULL,
    work_hours         NUMBER(4,2)     NOT NULL,
    work_content       VARCHAR2(1000)  NOT NULL,
    created_at         DATE            DEFAULT SYSDATE NOT NULL,
    updated_at         DATE            DEFAULT SYSDATE NOT NULL,
    CONSTRAINT pk_work_reports PRIMARY KEY (work_report_id),
    CONSTRAINT fk_work_reports_user
        FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_work_reports_department
        FOREIGN KEY (department_id) REFERENCES departments (department_id),
    CONSTRAINT ck_work_reports_category
        CHECK (work_category IN ('DESIGN', 'DEVELOPMENT', 'TEST', 'MEETING', 'DOCUMENT', 'OTHER')),
    CONSTRAINT ck_work_reports_hours
        CHECK (work_hours > 0 AND work_hours <= 24)
);

CREATE TABLE report_output_histories (
    report_output_history_id NUMBER(10)     NOT NULL,
    target_year_month        VARCHAR2(6)    NOT NULL,
    created_by               NUMBER(10)     NOT NULL,
    target_user_id           NUMBER(10)     NOT NULL,
    report_type              VARCHAR2(50)   NOT NULL,
    file_name                VARCHAR2(255)  NOT NULL,
    file_path                VARCHAR2(500)  NOT NULL,
    status                   VARCHAR2(20)   NOT NULL,
    error_message            VARCHAR2(1000),
    created_at               DATE           DEFAULT SYSDATE NOT NULL,
    updated_at               DATE           DEFAULT SYSDATE NOT NULL,
    CONSTRAINT pk_report_output_histories PRIMARY KEY (report_output_history_id),
    CONSTRAINT fk_report_histories_created_by
        FOREIGN KEY (created_by) REFERENCES users (user_id),
    CONSTRAINT fk_report_histories_target_user
        FOREIGN KEY (target_user_id) REFERENCES users (user_id),
    CONSTRAINT ck_report_histories_status
        CHECK (status IN ('SUCCESS', 'ERROR', 'PROCESSING')),
    CONSTRAINT ck_report_histories_type
        CHECK (report_type IN ('MONTHLY_WORK_REPORT')),
    CONSTRAINT ck_report_histories_ym
        CHECK (REGEXP_LIKE(target_year_month, '^[0-9]{6}$'))
);

CREATE SEQUENCE seq_departments
    START WITH 1001
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE seq_users
    START WITH 1001
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE seq_work_reports
    START WITH 1001
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE seq_report_output_histories
    START WITH 1001
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE INDEX idx_users_department_id
    ON users (department_id);

CREATE INDEX idx_work_reports_user_date
    ON work_reports (user_id, work_date);

CREATE INDEX idx_work_reports_department_date
    ON work_reports (department_id, work_date);

CREATE INDEX idx_work_reports_project_name
    ON work_reports (project_name);

CREATE INDEX idx_report_histories_ym
    ON report_output_histories (target_year_month);

CREATE INDEX idx_report_histories_created_by
    ON report_output_histories (created_by);

CREATE INDEX idx_report_histories_target_user
    ON report_output_histories (target_user_id);
