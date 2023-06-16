SELECT A.First_Name, A.Last_Name
FROM Authors A
WHERE A.Author_ID IN (
    SELECT B1.Author_ID
    FROM Books B1
    JOIN Subjects S1 on S1.Subject_ID = B1.Subject_ID
    WHERE S1.Subject = 'Children/YA'
)
AND A.Author_ID IN (
    SELECT B2.Author_ID
    FROM Books B2
    JOIN Subjects S2 on S2.Subject_ID = B2.Subject_ID
    WHERE S2.Subject = 'Fiction'
)
ORDER BY A.First_Name, A.Last_Name;

/*
JOIN Books B1 ON A.Author_ID = B1.Author_ID
JOIN Books B2 ON A.Author_ID = B2.Author_ID
JOIN Subjects S1 ON B1.Subject_ID = S1.subject_id
JOIN Subjects S2 ON B2.Subject_ID = S2.subject_id
WHERE S1.Subject = 'Children/YA' AND S2.Subject = 'Fiction'
ORDER BY A.First_Name, A.Last_Name;
*/
