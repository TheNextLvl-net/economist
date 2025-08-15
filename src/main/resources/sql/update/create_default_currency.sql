INSERT INTO translations
SELECT 'default_currency', 'Coin', 'Coins', 'en_US'
WHERE NOT EXISTS (SELECT 1 FROM currencies);

INSERT INTO currencies
SELECT 'default', '$', 2, 'default_currency'
WHERE NOT EXISTS (SELECT 1 FROM currencies);