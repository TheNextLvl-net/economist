SELECT uuid, balances.balance AS balance
FROM accounts
         LEFT JOIN balances ON balances.id = accounts.id AND currency = ?
WHERE world IS NOT DISTINCT FROM ?
  AND (? OR balance != 0)
ORDER BY balance DESC
LIMIT ? OFFSET ?;