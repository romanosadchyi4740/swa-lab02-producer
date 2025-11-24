-- Drop foreign key constraints from grades table
ALTER TABLE grades DROP CONSTRAINT IF EXISTS fkfs17qemf5ndxw3b7epeeep4ey;
ALTER TABLE grades DROP CONSTRAINT IF EXISTS fk566crflu2e9piq4ok465rvotr;

-- Add parent_id column to grades table
ALTER TABLE grades ADD COLUMN IF NOT EXISTS parent_id BIGINT;

-- Drop user-related tables (they are now managed by user-service)
DROP TABLE IF EXISTS teacher CASCADE;
DROP TABLE IF EXISTS student CASCADE;
DROP TABLE IF EXISTS parent CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Update grades table to use simple IDs (no foreign keys)
-- The columns already exist, we just removed the foreign key constraints

