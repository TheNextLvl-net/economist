INSERT
INTO balances (id, balance, currency)
SELECT (SELECT id FROM accounts WHERE uuid = ? AND world IS NOT DISTINCT FROM ? LIMIT 1), ?, ?
ON CONFLICT(id, currency) DO UPDATE SET balance = excluded.balance;