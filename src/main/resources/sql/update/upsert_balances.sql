WITH account AS (SELECT id FROM accounts WHERE uuid = ? AND world = ? LIMIT 1)

INSERT
INTO balances (id, balance, currency)
SELECT account.id, ?, ?
ON CONFLICT(id, currency) DO UPDATE SET balance = excluded.balance;

UPDATE accounts
SET last_update = DEFAULT
WHERE id = account.id;
-- todo: test if this actually works
-- todo: test if this can be a transaction or if this causes problems with batching