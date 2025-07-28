SELECT accounts.uuid     AS uuid,
       balances.balance  AS balance,
       balances.currency AS currency
FROM accounts
         LEFT JOIN balances ON balances.id = accounts.id
WHERE accounts.world IS NOT DISTINCT FROM ?;