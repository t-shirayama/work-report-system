CREATE TABLE departments (
    department_id int IDENTITY(1,1) NOT NULL,
    department_code nvarchar(20) NOT NULL,
    department_name nvarchar(100) NOT NULL,
    display_order int NOT NULL CONSTRAINT df_departments_display_order DEFAULT 0,
    created_at datetime2(0) NOT NULL CONSTRAINT df_departments_created_at DEFAULT sysdatetime(),
    updated_at datetime2(0) NOT NULL CONSTRAINT df_departments_updated_at DEFAULT sysdatetime(),
    CONSTRAINT pk_departments PRIMARY KEY (department_id),
    CONSTRAINT uk_departments_code UNIQUE (department_code)
);

CREATE TABLE users (
    user_id int IDENTITY(1,1) NOT NULL,
    department_id int NOT NULL,
    login_id nvarchar(50) NOT NULL,
    password nvarchar(255) NOT NULL,
    employee_name nvarchar(100) NOT NULL,
    role_code nvarchar(20) NOT NULL,
    created_at datetime2(0) NOT NULL CONSTRAINT df_users_created_at DEFAULT sysdatetime(),
    updated_at datetime2(0) NOT NULL CONSTRAINT df_users_updated_at DEFAULT sysdatetime(),
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uk_users_login_id UNIQUE (login_id),
    CONSTRAINT fk_users_department FOREIGN KEY (department_id) REFERENCES departments (department_id),
    CONSTRAINT ck_users_role_code CHECK (role_code IN (N'ADMIN', N'USER'))
);

CREATE TABLE work_reports (
    work_report_id int IDENTITY(1,1) NOT NULL,
    user_id int NOT NULL,
    department_id int NOT NULL,
    work_date date NOT NULL,
    project_name nvarchar(100) NOT NULL,
    work_category nvarchar(30) NOT NULL,
    work_hours decimal(4,2) NOT NULL,
    work_content nvarchar(1000) NOT NULL,
    created_at datetime2(0) NOT NULL CONSTRAINT df_work_reports_created_at DEFAULT sysdatetime(),
    updated_at datetime2(0) NOT NULL CONSTRAINT df_work_reports_updated_at DEFAULT sysdatetime(),
    CONSTRAINT pk_work_reports PRIMARY KEY (work_report_id),
    CONSTRAINT fk_work_reports_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_work_reports_department FOREIGN KEY (department_id) REFERENCES departments (department_id),
    CONSTRAINT ck_work_reports_category CHECK (work_category IN (N'DESIGN', N'DEVELOPMENT', N'TEST', N'MEETING', N'DOCUMENT', N'OTHER')),
    CONSTRAINT ck_work_reports_hours CHECK (work_hours > 0 AND work_hours <= 24)
);

CREATE TABLE report_output_histories (
    report_output_history_id int IDENTITY(1,1) NOT NULL,
    target_year_month char(6) NOT NULL,
    created_by int NOT NULL,
    target_user_id int NOT NULL,
    report_type nvarchar(50) NOT NULL,
    file_name nvarchar(255) NOT NULL,
    file_path nvarchar(500) NOT NULL,
    status nvarchar(20) NOT NULL,
    error_message nvarchar(1000) NULL,
    created_at datetime2(0) NOT NULL CONSTRAINT df_report_histories_created_at DEFAULT sysdatetime(),
    updated_at datetime2(0) NOT NULL CONSTRAINT df_report_histories_updated_at DEFAULT sysdatetime(),
    CONSTRAINT pk_report_output_histories PRIMARY KEY (report_output_history_id),
    CONSTRAINT fk_report_histories_created_by FOREIGN KEY (created_by) REFERENCES users (user_id),
    CONSTRAINT fk_report_histories_target_user FOREIGN KEY (target_user_id) REFERENCES users (user_id),
    CONSTRAINT ck_report_histories_status CHECK (status IN (N'SUCCESS', N'ERROR', N'PROCESSING')),
    CONSTRAINT ck_report_histories_type CHECK (report_type IN (N'MONTHLY_WORK_REPORT')),
    CONSTRAINT ck_report_histories_ym CHECK (target_year_month NOT LIKE '%[^0-9]%' AND len(target_year_month) = 6)
);

CREATE INDEX idx_users_department_id ON users (department_id);
CREATE INDEX idx_work_reports_user_date ON work_reports (user_id, work_date);
CREATE INDEX idx_work_reports_department_date ON work_reports (department_id, work_date);
CREATE INDEX idx_work_reports_project_name ON work_reports (project_name);
CREATE INDEX idx_report_histories_ym ON report_output_histories (target_year_month);
CREATE INDEX idx_report_histories_created_by ON report_output_histories (created_by);
CREATE INDEX idx_report_histories_target_user ON report_output_histories (target_user_id);
