SELECT CASE
           WHEN NOT EXISTS (SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'accounts') THEN 0
           WHEN (SELECT COUNT(*)
                 FROM pragma_table_info('accounts')
                 WHERE name IN ('id', 'last_update')) = 2
               THEN 0
           ELSE 1
           END;