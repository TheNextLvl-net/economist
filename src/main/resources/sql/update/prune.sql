DELETE
FROM accounts
WHERE world IS NOT DISTINCT FROM world
  AND (strftime('%s', 'now') - strftime('%s', last_update)) > ?;