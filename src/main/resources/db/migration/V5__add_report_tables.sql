-- Add tables for PDF reports

-- HQ.weekly_report_companies definition
CREATE TABLE `weekly_report_companies` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `company_id` varchar(36) NOT NULL,
    `pdf` longblob NOT NULL,
    `timestamp` datetime NOT NULL,
    PRIMARY KEY (`id`),
    KEY `weekly_report_companies_company_id` (`company_id`) USING BTREE,
    KEY `weekly_report_companies_indexes` (`company_id`,`timestamp`) USING BTREE,
    CONSTRAINT `weekly_report_companies_company_id` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='PDFs of Weekly Reports combined for companies';

-- HQ.daily_report_companies definition
CREATE TABLE `daily_report_companies` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `company_id` varchar(36) NOT NULL,
    `pdf` longblob NOT NULL,
    `timestamp` datetime NOT NULL,
    PRIMARY KEY (`id`),
    KEY `daily_report_companies_company_id` (`company_id`) USING BTREE,
    KEY `daily_report_companies_indexes` (`company_id`,`timestamp`) USING BTREE,
    CONSTRAINT `daily_report_companies_company_id` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='PDFs of Daily Reports combined for companies';

-- Insert sample data for testing
INSERT INTO `weekly_report_companies` (`company_id`, `pdf`, `timestamp`)
SELECT 
    c.id, 
    LOAD_FILE('C:/Workspaces/JAVA/ReverseEngineer/doubleFolder/roeslein-dashboard-nextjs/_REPORTS/pdf-report-examples/Monarch -  Weekly.pdf'), 
    NOW()
FROM `companies` c
LIMIT 1;

INSERT INTO `daily_report_companies` (`company_id`, `pdf`, `timestamp`)
SELECT 
    c.id, 
    LOAD_FILE('C:/Workspaces/JAVA/ReverseEngineer/doubleFolder/roeslein-dashboard-nextjs/_REPORTS/pdf-report-examples/Monarch - Daily.pdf'), 
    NOW()
FROM `companies` c
LIMIT 1;