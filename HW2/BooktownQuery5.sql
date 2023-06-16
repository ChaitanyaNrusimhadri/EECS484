/*
SELECT A.Author_ID, A.First_Name, A.Last_Name
FROM Authors A 
JOIN Books B ON B.author_id = A.author_id
JOIN Subjects S 
IN (
    SELECT DISTINCT S2.Subject 
    FROM Subjects S2
    JOIN Books B2 ON S2.Subject_ID = B2.subject_id
    JOIN Authors A2 ON A2.author_id = B2.author_id
        WHERE A2.Last_Name = 'Rowling' AND A2.First_Name = 'J. K.'
)
ON  S.Subject_ID = B.Subject_ID
ORDER BY A.Last_Name, A.Author_ID DESC;
*/

-- inner stuff: search for subjects written by JK Rowling

-- outer stuff: search for authors who have written in that specific subject

-- TODO: NEED EVERY SUBJECT


/*
SELECT A.Author_ID, A.First_Name, A.Last_Name
FROM Authors A, Books B, Subjects S
WHERE S.subject
IN (
    SELECT DISTINCT S2.Subject 
    FROM Subjects S2
    JOIN Books B2 ON S2.Subject_ID = B2.subject_id
    JOIN Authors A2 ON A2.author_id = B2.author_id
        WHERE A2.Last_Name = 'Rowling' AND A2.First_Name = 'J. K.'
)
AND B.author_id = A.author_id
AND S.Subject_ID = B.Subject_ID
ORDER BY A.Last_Name, A.Author_ID DESC;
*/

--MINUS set operator and WHERE NOT EXISTS
/*
SELECT A.Author_ID, A.First_Name, A.Last_Name
FROM Authors A, Books B, Subjects S
JOIN Book B ON A.Author_ID = B.Author_ID
JOIN Subjects S ON B.Subject_ID = S.Subject_ID
WHERE A.Author_ID IN (
    SELECT B2.Author_ID
    FROM Books B2
    JOIN Subjects S2 ON B2.Subject_ID = S2.Subject_ID
    JOIN Authors A2 ON A2.Author_ID = B2.Author_ID
    WHERE A2.Last_Name = 'Rowling' AND A2.First_Name = 'J. K.'
    GROUP BY B2.Subject_ID
    HAVING COUNT(DISTINCT B2.Subject_ID) = ( --equal to ensure at least one in every subject
        SELECT COUNT(DISTINCT S3.Subject_ID) -- count number of unique subjects by J.K. Rowling
        FROM Subjects S3
        JOIN Books B3 ON S3.Subject_ID = B3.Subject_ID
        JOIN Authors A3 ON A3.Author_ID = B3.Author_ID
        WHERE A3.Last_Name = 'Rowling' AND A3.First_Name = 'J. K.'
    )
)
ORDER BY A.Last_Name, A.Author_ID DESC;

*/
/*
SELECT A.Author_ID, A.First_Name, A.Last_Name
FROM Authors A 
JOIN Books B ON A.Author_ID = B.Author_ID 
JOIN Subjects S ON B.Subject_ID = S.Subject_ID
ORDER BY A.Last_Name, A.Author_ID DESC;
MINUS
SELECT A1.Author_ID, A1.First_Name, A1.Last_Name
FROM Authors A1
JOIN Books B1 ON A1.Author_ID = B1.Author_ID 
JOIN Subjects S1 ON B1.Subject_ID = S1.Subject_ID
 WHERE NOT EXISTS (
    SELECT 1
    FROM Authors A2
    JOIN Books B2 ON A2.Author_ID = B2.Author_ID 
    JOIN Subjects S2 ON B2.Subject_ID = S2.Subject_ID
    WHERE A2.Last_Name <> 'Rowling' AND A2.First_Name <> 'J. K.'
    AND 
 )
 */

SELECT A.Author_ID, A.First_Name, A.Last_Name
FROM Authors A
WHERE NOT EXISTS (
    (SELECT B.Subject_ID
    FROM Books B
    JOIN Authors A1 ON B.Author_ID = A1.Author_ID
    WHERE A1.First_Name = 'J. K.' AND A1.Last_Name = 'Rowling')
    MINUS
    (SELECT B.Subject_ID
    FROM Books B
    WHERE B.Author_ID = A.Author_ID)
)
ORDER BY A.Last_Name, A.Author_ID DESC;