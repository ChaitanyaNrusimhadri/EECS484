SELECT B.Title, E.Publication_Date, A.Author_ID, A.First_Name, A.Last_Name
FROM Authors A 
JOIN Books B ON B.Author_ID = A.author_id
JOIN Editions E ON E.Book_ID = B.Book_ID
WHERE A.Author_ID IN (
    SELECT B2.Author_ID
    FROM Books B2
    JOIN Editions E2 ON E2.Book_ID = B2.Book_ID
    WHERE (E2.Publication_Date >= '2003-01-01' AND E2.Publication_Date <= '2008-12-31')
)
ORDER BY A.Author_ID, B.Title, E.Publication_Date DESC;