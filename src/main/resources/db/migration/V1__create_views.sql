-- Drop existing views if they exist
DROP VIEW IF EXISTS user_company_view;
DROP VIEW IF EXISTS company_user_view;
DROP VIEW IF EXISTS client_admin_user_view;
DROP VIEW IF EXISTS super_admin_user_view;

-- Create user_company_view
CREATE VIEW user_company_view AS
SELECT
    cu.user_id,
    cu.company_id,
    c.name as company_name,
    c.display_name,
    c.logo_url
FROM users u
JOIN company_users cu ON u.id = cu.user_id
JOIN companies c ON c.id = cu.company_id;

-- Create company_user_view
CREATE VIEW company_user_view AS
SELECT
    cu.user_id,
    cu.company_id,
    u.username,
    u.first_name,
    u.last_name,
    c.name as company_name,
    c.display_name
FROM users u
JOIN company_users cu ON u.id = cu.user_id
JOIN companies c ON c.id = cu.company_id;

-- Create client_admin_user_view
CREATE VIEW client_admin_user_view AS
SELECT DISTINCT
    u.id as user_id,
    u.username,
    u.first_name as firstName,
    u.last_name as lastName,
    uci.value as email
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
LEFT JOIN user_contact_info uci ON u.id = uci.user_id
WHERE r.name = 'CLIENT_ADMIN';

-- Create super_admin_user_view
CREATE VIEW super_admin_user_view AS
SELECT DISTINCT
    u.id as user_id,
    u.username,
    u.first_name as firstName,
    u.last_name as lastName,
    uci.value as email
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
LEFT JOIN user_contact_info uci ON u.id = uci.user_id
WHERE r.name = 'SUPER_ADMIN'; 