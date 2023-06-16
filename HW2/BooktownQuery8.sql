SELECT DISTINCT P.Publisher_ID, P.Name
FROM Publishers P 
JOIN Editions E ON P.Publisher_ID = E.Publisher_ID
JOIN Books B ON B.Book_ID = E.Book_ID
WHERE B.Author_ID IN (
    SELECT B2.Author_ID
    FROM Books B2
    GROUP BY B2.Author_ID
    HAVING COUNT(*) = 3
)
ORDER BY P.Publisher_ID DESC;