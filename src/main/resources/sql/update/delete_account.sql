DELETE
FROM accounts
WHERE uuid = ?
  AND world IS NOT DISTINCT FROM ?;