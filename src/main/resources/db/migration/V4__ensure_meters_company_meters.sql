-- Ensure meters have include_website set to true and company_meters entries

-- Add include_website column if it doesn't exist
ALTER TABLE meters ADD COLUMN IF NOT EXISTS include_website BOOLEAN DEFAULT TRUE;

-- Create index on include_website
CREATE INDEX IF NOT EXISTS meters_include_website_index ON meters (include_website);

-- Set include_website to true for all meters
UPDATE meters SET include_website = TRUE WHERE include_website IS NULL;

-- Create company_meters entries for all meters that don't have them
INSERT INTO company_meters (company_id, meter_id, timestamp)
SELECT 
    cf.company_id, 
    m.id AS meter_id, 
    NOW() AS timestamp
FROM 
    meters m
JOIN 
    farms f ON m.farm_id = f.id
JOIN 
    company_farms cf ON f.id = cf.farm_id
LEFT JOIN 
    company_meters cm ON m.id = cm.meter_id AND cf.company_id = cm.company_id
WHERE 
    cm.id IS NULL;