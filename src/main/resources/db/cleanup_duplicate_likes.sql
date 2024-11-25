
-- Delete duplicate likes keeping only one record for each post-user combination
WITH DuplicateLikes AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY post_id, user_id ORDER BY id) as rn
    FROM post_likes
)
DELETE FROM post_likes
WHERE id IN (
    SELECT id
    FROM DuplicateLikes
    WHERE rn > 1
);
