SELECT C.CID
FROM COURSES C, STUDENTS S, ENROLLMENTS E
WHERE S.sid = E.sid AND E.cid = C.cid 
-----WHERE statement
GROUP BY C.CID
HAVING COUNT(DISTINCT CASE WHEN S.Major <> 'CS' THEN S.sid END) < 10
--10 > (SELECT COUNT(*) FROM STUDENTS S2 WHERE S2.major <> 'CS')
ORDER BY C.CID DESC;


---- alternate code for WHERE statement above
/*
WHERE C.CID IN (
    SELECT S2.major 
    FROM Students S2
    GROUP BY S2.major 
    HAVING COUNT(*) < 10 WHERE S2.major <> 'CS'
)
ORDER BY C.CID DESC
*/