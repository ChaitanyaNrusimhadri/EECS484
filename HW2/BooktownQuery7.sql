SELECT B.Title, SUM(E.Pages) AS Total_Pages
FROM Books B
JOIN Editions E ON B.Book_ID = E.Book_ID
GROUP BY B.Title
ORDER BY Total_Pages DESC;

/*
SELECT B.Title, SUM(E.Pages) AS Total_Pages
From Books B
JOIN Editions E ON B.Book_ID = E.Book_ID
ORDER BY Total_Pages DESC;
*/