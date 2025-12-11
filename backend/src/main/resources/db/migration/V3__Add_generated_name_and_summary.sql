-- Add generated name and summary columns
ALTER TABLE games ADD COLUMN IF NOT EXISTS generated_name VARCHAR(255);
ALTER TABLE games ADD COLUMN IF NOT EXISTS generated_summary VARCHAR(1000);

-- Optionally remove current_image_url if no longer needed
-- ALTER TABLE games DROP COLUMN IF EXISTS current_image_url;
