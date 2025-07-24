SELECT accounts.uuid     AS uuid,
       accounts.world    AS world,
       balances.balance  AS balance,
       balances.currency AS currency
FROM accounts
         LEFT JOIN balances ON balances.id = accounts.id
WHERE accounts.last_update > ?;