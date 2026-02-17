-- Align database constraints with current JPA mappings (source of truth)

-- Remove UNIQUE constraints that are not present in the entities
ALTER TABLE accounts DROP CONSTRAINT IF EXISTS uq_accounts_user_name;
ALTER TABLE categories DROP CONSTRAINT IF EXISTS uq_categories_user_name;
ALTER TABLE tags DROP CONSTRAINT IF EXISTS uq_tags_user_name;
