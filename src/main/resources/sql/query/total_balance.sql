SELECT SUM(balance) as total_balance
FROM accounts
WHERE world IS NOT DISTINCT FROM ?;
-- todo: by currency and optionally by world (null is all)