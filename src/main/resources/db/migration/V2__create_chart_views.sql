-- Create chart_meter_daily_view for chart data

-- Create meters table if it doesn't exist
CREATE TABLE IF NOT EXISTS `meters` (
    `id` varchar(36) NOT NULL,
    `farm_id` varchar(36) NOT NULL,
    `name` varchar(255) NOT NULL,
    `display_name` varchar(255) DEFAULT NULL,
    `timestamp` datetime NOT NULL,
    PRIMARY KEY (`id`),
    KEY `meters_farm_id` (`farm_id`) USING BTREE,
    CONSTRAINT `meters_farm_id` FOREIGN KEY (`farm_id`) REFERENCES `farms` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='List of all meters related to farms';

-- Create meter_daily table if it doesn't exist
CREATE TABLE IF NOT EXISTS `meter_daily` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `meter_id` varchar(36) NOT NULL,
    `value` double NOT NULL,
    `timestamp` datetime NOT NULL,
    PRIMARY KEY (`id`),
    KEY `meter_daily_meter_id` (`meter_id`) USING BTREE,
    KEY `meter_daily_indexes` (`meter_id`,`timestamp`),
    CONSTRAINT `meter_daily_meter_id` FOREIGN KEY (`meter_id`) REFERENCES `meters` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Daily meter value';

-- Create chart_meter_daily_view
CREATE OR REPLACE VIEW `chart_meter_daily_view` AS
SELECT 
    md.id,
    m.id AS meter_id,
    m.farm_id,
    f.name AS farm_name,
    c.id AS company_id,
    UNIX_TIMESTAMP(DATE(md.timestamp)) AS timestamp,
    md.value
FROM 
    meter_daily md
JOIN 
    meters m ON md.meter_id = m.id
JOIN 
    farms f ON m.farm_id = f.id
JOIN 
    company_farms cf ON f.id = cf.farm_id
JOIN 
    companies c ON cf.company_id = c.id;