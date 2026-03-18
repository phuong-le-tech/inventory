-- Create a default "Personnel" workspace for each existing user
INSERT INTO workspaces (id, name, owner_id, is_default, created_at, updated_at, version)
SELECT gen_random_uuid(), 'Personnel', id, true, NOW(), NOW(), 0
FROM users;

-- Create OWNER membership for each user in their default workspace
INSERT INTO workspace_members (id, workspace_id, user_id, role, created_at)
SELECT gen_random_uuid(), w.id, w.owner_id, 'OWNER', NOW()
FROM workspaces w;

-- Move all existing lists into their owner's default workspace
UPDATE item_lists il
SET workspace_id = (
    SELECT w.id FROM workspaces w
    WHERE w.owner_id = il.user_id AND w.is_default = true
);

-- Now enforce NOT NULL on workspace_id
ALTER TABLE item_lists ALTER COLUMN workspace_id SET NOT NULL;
