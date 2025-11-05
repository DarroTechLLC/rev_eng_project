-- Create email_logs table for tracking email sends
CREATE TABLE IF NOT EXISTS email_logs (
    id VARCHAR(36) PRIMARY KEY,
    mail_from VARCHAR(255) NOT NULL,
    mail_to VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    attached_files JSON,
    mail_type VARCHAR(50),
    company_id VARCHAR(36),
    user_id VARCHAR(36),
    status ENUM('pending', 'sent', 'failed') DEFAULT 'pending',
    message_id VARCHAR(255),
    error_message TEXT,
    sent_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT email_logs_company_id
        FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
    
    CONSTRAINT email_logs_user_id
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Log of all email sends for weekly/daily reports with delivery status tracking';

CREATE INDEX idx_company_id ON email_logs (company_id);
CREATE INDEX idx_status ON email_logs (status);
CREATE INDEX idx_sent_at ON email_logs (sent_at);
CREATE INDEX idx_user_id ON email_logs (user_id);
CREATE INDEX idx_mail_type ON email_logs (mail_type);

-- Create user_subscriptions table for managing various notification subscriptions
CREATE TABLE IF NOT EXISTS user_subscriptions (
    user_id VARCHAR(36) NOT NULL,
    company_id VARCHAR(36) NOT NULL,
    subscription_key VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (user_id, company_id, subscription_key),
    
    CONSTRAINT user_subscriptions_company_id
        FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
    
    CONSTRAINT user_subscriptions_user_id
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User subscriptions for various notification types (weekly reports, daily reports, alerts, etc.)';

CREATE INDEX idx_user_subscriptions_user_id ON user_subscriptions (user_id);
CREATE INDEX idx_user_subscriptions_company_id ON user_subscriptions (company_id);
CREATE INDEX idx_user_subscriptions_subscription_key ON user_subscriptions (subscription_key);

-- Create email_logs_view
DROP VIEW IF EXISTS email_logs_view;

CREATE VIEW email_logs_view AS
SELECT
    el.id,
    el.mail_from,
    el.mail_to,
    el.subject,
    el.attached_files,
    el.mail_type,
    el.company_id,
    el.user_id,
    el.status,
    el.message_id,
    el.error_message,
    el.sent_at,
    el.created_at,
    el.updated_at,
    c.name AS company_name,
    c.display_name AS company_display_name,
    u.username,
    u.firstName,
    u.lastName
FROM
    email_logs el
    LEFT JOIN companies c ON c.id = el.company_id
    LEFT JOIN users u ON u.id = el.user_id;

-- Create user_subscriptions_view
DROP VIEW IF EXISTS user_subscriptions_view;

CREATE VIEW user_subscriptions_view AS
SELECT
    us.user_id,
    us.company_id,
    us.subscription_key,
    us.created_at,
    us.updated_at,
    u.username,
    u.firstName,
    u.lastName,
    c.name AS company_name,
    c.display_name AS company_display_name
FROM
    user_subscriptions us
    INNER JOIN users u ON u.id = us.user_id
    INNER JOIN companies c ON c.id = us.company_id;




