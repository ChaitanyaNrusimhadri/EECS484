SELECT S.Subject 
FROM Subjects S 
LEFT JOIN Books B ON S.Subject_ID = B.Subject_ID 
WHERE B.Book_ID IS NULL
ORDER BY S.Subject;
