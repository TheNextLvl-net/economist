SELECT balances.balance  AS balance,
       balances.currency AS currency
FROM balances
         INNER JOIN accounts ON balances.id = accounts.id
WHERE accounts.uuid = ?
  AND accounts.world IS NOT DISTINCT FROM ?;