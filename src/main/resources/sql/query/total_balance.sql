-- SELECT SUM(balance) as total_balance
-- FROM accounts
-- WHERE world IS NOT DISTINCT FROM ?;
-- todo: by currency and optionally by world (null is all)

SELECT SUM(balance) as total_balance
FROM accounts
         LEFT JOIN balances ON accounts.id = balances.id
WHERE world IS NOT DISTINCT FROM ? AND currency = ?;