-- Update chart_meter_daily_view to match NextJS implementation

-- Drop existing view
DROP VIEW IF EXISTS `chart_meter_daily_view`;

-- Create updated view
CREATE OR REPLACE VIEW `chart_meter_daily_view` AS
SELECT 
    md.id,
    m.id AS meter_id,
    m.farm_id,
    f.name AS farm_name,
    cm.company_id,
    md.timestamp,
    md.value
FROM 
    meter_daily md
JOIN 
    meters m ON md.meter_id = m.id
JOIN 
    farms f ON m.farm_id = f.id
JOIN 
    company_meters cm ON m.id = cm.meter_id
WHERE 
    m.include_website = true;