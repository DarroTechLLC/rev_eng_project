-- Create views for weekly report data

-- Create report_volume_daily_summary_view
CREATE OR REPLACE VIEW `report_volume_daily_summary_view` AS
SELECT 
    cm.company_id,
    m.farm_id,
    SUM(md.value) AS volume,
    DATE(md.timestamp) AS date
FROM 
    meter_daily md
    JOIN meters m ON md.meter_id = m.id
    JOIN company_meters cm ON m.id = cm.meter_id
GROUP BY 
    cm.company_id, m.farm_id, DATE(md.timestamp);

-- Create report_volume_monthly_summary_view
CREATE OR REPLACE VIEW `report_volume_monthly_summary_view` AS
SELECT 
    cm.company_id,
    m.farm_id,
    SUM(md.value) AS volume,
    DATE_FORMAT(md.timestamp, '%Y-%m-01') AS date
FROM 
    meter_daily md
    JOIN meters m ON md.meter_id = m.id
    JOIN company_meters cm ON m.id = cm.meter_id
GROUP BY 
    cm.company_id, m.farm_id, DATE_FORMAT(md.timestamp, '%Y-%m-01');

-- Create report_forecast_monthly_summary_view
CREATE OR REPLACE VIEW `report_forecast_monthly_summary_view` AS
SELECT 
    cm.company_id,
    m.farm_id,
    SUM(mf.value) AS forecast,
    DATE_FORMAT(mf.timestamp, '%Y-%m-01') AS date
FROM 
    meter_monthly_forecast mf
    JOIN meters m ON mf.meter_id = m.id
    JOIN company_meters cm ON m.id = cm.meter_id
GROUP BY 
    cm.company_id, m.farm_id, DATE_FORMAT(mf.timestamp, '%Y-%m-01');