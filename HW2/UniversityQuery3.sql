--SELECT DISTINCT S.sid
--FROM STUDENTS S 
--JOIN ENROLLMENTS E on E.sid = S.sid
--JOIN COURSES C on C.cid = E.cid
 --JOIN ( Don't need this JOIN
    SELECT DISTINCT E1.sid
    FROM ENROLLMENTS E1
    JOIN ENROLLMENTS E2 ON E1.sid = E2.sid
    JOIN ENROLLMENTS E3 ON E1.sid = E3.sid
    JOIN COURSES C1 ON C1.cid = E1.cid
    JOIN COURSES C2 ON C2.cid = E2.cid
    JOIN COURSES C3 ON C3.cid = E3.cid
    WHERE (C1.C_name = 'EECS442' AND C2.C_name = 'EECS445' AND C3.C_name = 'EECS492') 
        OR (C1.C_name = 'EECS482' AND C2.C_name = 'EECS486') 
        OR C1.C_name IN ('EECS281') 
ORDER BY E1.sid;