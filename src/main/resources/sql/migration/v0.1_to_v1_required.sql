SELECT CASE
           WHEN (SELECT COUNT(*)
                 FROM pragma_table_info('accounts')
                 WHERE name IN ('id', 'last_update')) = 2
               THEN 0
           ELSE 1
           END;