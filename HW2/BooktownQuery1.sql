SELECT A.Author_ID
FROM AUTHORS A, Books B
WHERE B.Author_ID = A.Author_ID
GROUP BY A.Author_ID
HAVING COUNT(B.Book_ID) = 1
ORDER BY A.Author_ID ASC;


-- author_id, book_id