UPDATE users
SET kakao_id = NULL
WHERE deleted_at IS NOT NULL
  AND kakao_id IS NOT NULL;
