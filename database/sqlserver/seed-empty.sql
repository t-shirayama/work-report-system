SET IDENTITY_INSERT departments ON;
INSERT INTO departments (department_id, department_code, department_name, display_order) VALUES
(1, N'ADMIN', N'管理部', 1);
SET IDENTITY_INSERT departments OFF;

SET IDENTITY_INSERT users ON;
INSERT INTO users (user_id, department_id, login_id, password, employee_name, role_code) VALUES
(1, 1, N'admin', N'$2a$11$VeSZPhBiJhqgc5Dqqj34Fu/zDFw77Mq8BMnCVx2/HXzPEvNCFK6be', N'管理者', N'ADMIN');
SET IDENTITY_INSERT users OFF;
