UPDATE accounts
SET last_update = DEFAULT
WHERE uuid = ?
  AND world IS NOT DISTINCT FROM ?;