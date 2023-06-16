SELECT E.ISBN 
FROM Editions E, Books B, Authors A 
WHERE E.Book_ID = B.Book_ID AND B.Author_ID = A.Author_ID AND A.First_Name = 'Agatha' AND A.Last_Name = 'Christie'
ORDER BY E.ISBN DESC;
