package project2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;

/*
    The StudentFakebookOracle class is derived from the FakebookOracle class and implements
    the abstract query functions that investigate the database provided via the <connection>
    parameter of the constructor to discover specific information.
*/
public final class StudentFakebookOracle extends FakebookOracle {
    // [Constructor]
    // REQUIRES: <connection> is a valid JDBC connection
    public StudentFakebookOracle(Connection connection) {
        oracle = connection;
    }

    @Override
    // Query 0
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the total number of users for which a birth month is listed
    //        (B) Find the birth month in which the most users were born
    //        (C) Find the birth month in which the fewest users (at least one) were born
    //        (D) Find the IDs, first names, and last names of users born in the month
    //            identified in (B)
    //        (E) Find the IDs, first names, and last name of users born in the month
    //            identified in (C)
    //
    // This query is provided to you completed for reference. Below you will find the appropriate
    // mechanisms for opening up a statement, executing a query, walking through results, extracting
    // data, and more things that you will need to do for the remaining nine queries
    public BirthMonthInfo findMonthOfBirthInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            // Step 1
            // ------------
            // * Find the total number of users with birth month info
            // * Find the month in which the most users were born
            // * Find the month in which the fewest (but at least 1) users were born
            ResultSet rst = stmt.executeQuery(
                    "SELECT COUNT(*) AS Birthed, Month_of_Birth " + // select birth months and number of uses with that birth month
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth IS NOT NULL " + // for which a birth month is available
                            "GROUP BY Month_of_Birth " + // group into buckets by birth month
                            "ORDER BY Birthed DESC, Month_of_Birth ASC"); // sort by users born in that month, descending; break ties by birth month

            int mostMonth = 0;
            int leastMonth = 0;
            int total = 0;
            while (rst.next()) { // step through result rows/records one by one
                if (rst.isFirst()) { // if first record
                    mostMonth = rst.getInt(2); //   it is the month with the most
                }
                if (rst.isLast()) { // if last record
                    leastMonth = rst.getInt(2); //   it is the month with the least
                }
                total += rst.getInt(1); // get the first field's value as an integer
            }
            BirthMonthInfo info = new BirthMonthInfo(total, mostMonth, leastMonth);

            // Step 2
            // ------------
            // * Get the names of users born in the most popular birth month
            rst = stmt.executeQuery(
                    "SELECT User_ID, First_Name, Last_Name " + // select ID, first name, and last name
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth = " + mostMonth + " " + // born in the most popular birth month
                            "ORDER BY User_ID"); // sort smaller IDs first

            while (rst.next()) {
                info.addMostPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 3
            // ------------
            // * Get the names of users born in the least popular birth month
            rst = stmt.executeQuery(
                    "SELECT User_ID, First_Name, Last_Name " + // select ID, first name, and last name
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth = " + leastMonth + " " + // born in the least popular birth month
                            "ORDER BY User_ID"); // sort smaller IDs first

            while (rst.next()) {
                info.addLeastPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 4
            // ------------
            // * Close resources being used
            rst.close();
            stmt.close(); // if you close the statement first, the result set gets closed automatically

            return info;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new BirthMonthInfo(-1, -1, -1);
        }
    }

    @Override
    // Query 1
    // -----------------------------------------------------------------------------------
    // GOALS: (A) The first name(s) with the most letters
    //        (B) The first name(s) with the fewest letters
    //        (C) The first name held by the most users
    //        (D) The number of users whose first name is that identified in (C)
    public FirstNameInfo findNameInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                FirstNameInfo info = new FirstNameInfo();
                info.addLongName("Aristophanes");
                info.addLongName("Michelangelo");
                info.addLongName("Peisistratos");
                info.addShortName("Bob");
                info.addShortName("Sue");
                info.addCommonName("Harold");
                info.addCommonName("Jessica");
                info.setCommonNameCount(42);
                return info;
            */
            
            FirstNameInfo info = new FirstNameInfo();
            //Step A -- The first name(s) with the most letters
            ResultSet rst = stmt.executeQuery(
                "SELECT DISTINCT First_Name " + 
                "FROM " + UsersTable + " " +
                "WHERE LENGTH(First_Name) = (SELECT MAX(LENGTH(First_Name)) FROM " + UsersTable + ")" +
                "ORDER BY First_Name ASC"
            );
            while(rst.next()) { //add Max Length to longestFirstName
                info.addLongName(rst.getString(1));
            }

            //Step B -- The first name(s) with the fewest letters
            rst = stmt.executeQuery(
                "SELECT DISTINCT First_Name " + 
                "FROM " + UsersTable + " " +
                "WHERE LENGTH(First_Name) = (SELECT MIN(LENGTH(First_Name)) FROM " + UsersTable + ")" +
                "ORDER BY First_Name ASC"
            );
            while(rst.next()) { //add Min Length to shortestFirstName
                info.addShortName(rst.getString(1));
            }

            //Step C, D -- The first name held by the most users and the number of times they appear
            rst = stmt.executeQuery(
                "SELECT First_Name, COUNT(*) as count " +
                "FROM " + UsersTable + " " +
                "GROUP BY First_Name " +
                "HAVING COUNT(*) = (SELECT MAX(cnt) FROM (SELECT COUNT(*) AS cnt FROM " 
                        + UsersTable + " GROUP BY First_Name))" +
                "ORDER BY First_Name ASC"
            );
            while(rst.next()) {
                info.addCommonName(rst.getString(1));
                info.setCommonNameCount(rst.getLong(2));
            }

            //Close Statement and Resources used
            rst.close();
            stmt.close();

            //return new FirstNameInfo(); // placeholder for compilation
            return info;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new FirstNameInfo();
        }
    }

    @Override
    // Query 2
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users without any friends
    //
    // Be careful! Remember that if two users are friends, the Friends table only contains
    // the one entry (U1, U2) where U1 < U2.
    public FakebookArrayList<UserInfo> lonelyUsers() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(15, "Abraham", "Lincoln");
                UserInfo u2 = new UserInfo(39, "Margaret", "Thatcher");
                results.add(u1);
                results.add(u2);
            */
            
            //SQL Query 
            ResultSet rst = stmt.executeQuery(
                "SELECT User_ID, First_Name, Last_Name " +
                "FROM " + UsersTable +
                " WHERE User_ID NOT IN (" +
                    "SELECT User1_ID FROM " + FriendsTable + 
                    " UNION " +
                    "SELECT User2_ID FROM " + FriendsTable +
                ") " + 
                "ORDER BY " + UsersTable + ".User_ID ASC"
            );
            
            // Instantiate userInfo as an object, add its values to array<UserInfo> results
            while (rst.next()) {
                long userID = rst.getLong(1);
                String firstName = rst.getString(2);
                String lastName = rst.getString(3);

                UserInfo userInfo = new UserInfo(userID, firstName, lastName);
                results.add(userInfo);
            }

            //close statements
            rst.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 3
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users who no longer live
    //            in their hometown (i.e. their current city and their hometown are different)
    public FakebookArrayList<UserInfo> liveAwayFromHome() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(9, "Meryl", "Streep");
                UserInfo u2 = new UserInfo(104, "Tom", "Hanks");
                results.add(u1);
                results.add(u2);
            */

        ResultSet rst = stmt.executeQuery(
            "SELECT U.user_ID, U.First_Name, U.Last_Name " +
            "FROM " + UsersTable + " U " +
            "JOIN " + CurrentCitiesTable + " C ON U.user_ID = C.user_ID " + 
            "JOIN " + HometownCitiesTable + " H ON U.user_ID = H.user_ID " +
            "WHERE C.current_city_ID <> H.hometown_city_ID " + 
            "ORDER BY U.user_ID ASC" 
        );
        while (rst.next()) {
            long userID = rst.getLong(1);
            String firstName = rst.getString(2);
            String lastName = rst.getString(3); 

            // Instantiate userInfo as an object, add its values to array<UserInfo> results
            UserInfo userInfo = new UserInfo(userID, firstName, lastName);
            results.add(userInfo);
        }

        //close statements
        rst.close();
        stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 4
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, links, and IDs and names of the containing album of the top
    //            <num> photos with the most tagged users
    //        (B) For each photo identified in (A), find the IDs, first names, and last names
    //            of the users therein tagged
    public FakebookArrayList<TaggedPhotoInfo> findPhotosWithMostTags(int num) throws SQLException {
        FakebookArrayList<TaggedPhotoInfo> results = new FakebookArrayList<TaggedPhotoInfo>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                PhotoInfo p = new PhotoInfo(80, 5, "www.photolink.net", "Winterfell S1");
                UserInfo u1 = new UserInfo(3901, "Jon", "Snow");
                UserInfo u2 = new UserInfo(3902, "Arya", "Stark");
                UserInfo u3 = new UserInfo(3903, "Sansa", "Stark");
                TaggedPhotoInfo tp = new TaggedPhotoInfo(p);
                tp.addTaggedUser(u1);
                tp.addTaggedUser(u2);
                tp.addTaggedUser(u3);
                results.add(tp);
            */
            ResultSet rst = stmt.executeQuery(
                "SELECT * FROM ( " +
                "SELECT P.photo_ID, A.album_ID, P.photo_link, A.album_name, COUNT(*) as tag_count " +
                "FROM " + PhotosTable + " P " +
                "JOIN " + AlbumsTable + " A ON A.album_ID = P.album_ID " + 
                "JOIN " + TagsTable + " T ON T.tag_photo_ID = P.photo_ID " + 
                "GROUP BY P.photo_ID, A.album_ID, P.photo_link, A.album_name " +
                "ORDER BY tag_count DESC, P.photo_ID ASC" + " ) " + 
                "WHERE ROWNUM <= " + num
            );

        while (rst.next()) {
            long photoID = rst.getLong(1);
            long albumID = rst.getLong(2);
            String link = rst.getString(3);
            String albumName = rst.getString(4);

            PhotoInfo p = new PhotoInfo(photoID, albumID, link, albumName);

            try (Statement stmt2 = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {

                ResultSet rst2 = stmt2.executeQuery(
                    "SELECT U.user_ID, U.First_Name, U.Last_Name " +
                    "FROM " + PhotosTable + " P " + 
                    "JOIN " + TagsTable + " T ON T.tag_photo_ID = P.photo_ID " +
                    "JOIN " + UsersTable + " U ON U.user_ID = T.tag_subject_ID " +
                    "WHERE P.photo_id = " + photoID + 
                    "ORDER BY U.user_ID ASC"
                );

                TaggedPhotoInfo tp = new TaggedPhotoInfo(p);
                while(rst2.next()){
                    long userID = rst2.getLong(1);
                    String first = rst2.getString(2);
                    String last = rst2.getString(3);
                    UserInfo u = new UserInfo(userID, first, last);
                    tp.addTaggedUser(u);
                }
                results.add(tp);
                rst2.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        rst.close();
        stmt.close();
        
    } catch (SQLException e) {
        System.err.println(e.getMessage());
    }

    return results;
}

    @Override
    // Query 5
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, last names, and birth years of each of the two
    //            users in the top <num> pairs of users that meet each of the following
    //            criteria:
    //              (i) same gender
    //              (ii) tagged in at least one common photo
    //              (iii) difference in birth years is no more than <yearDiff>
    //              (iv) not friends
    //        (B) For each pair identified in (A), find the IDs, links, and IDs and names of
    //            the containing album of each photo in which they are tagged together
    public FakebookArrayList<MatchPair> matchMaker(int num, int yearDiff) throws SQLException {
        FakebookArrayList<MatchPair> results = new FakebookArrayList<MatchPair>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(93103, "Romeo", "Montague");
                UserInfo u2 = new UserInfo(93113, "Juliet", "Capulet");
                MatchPair mp = new MatchPair(u1, 1597, u2, 1597);
                PhotoInfo p = new PhotoInfo(167, 309, "www.photolink.net", "Tragedy");
                mp.addSharedPhoto(p);
                results.add(mp);
            */
            ResultSet rst = stmt.executeQuery(
                "SELECT * FROM (" +
                    "SELECT u1.user_ID as user_id_1, u1.first_name as first_name_1, u1.last_name as last_name_1, u1.year_of_birth as year_of_birth_1, " + 
                    "u2.user_ID as user_id_2, u2.first_name as first_name_2, u2.last_name as last_name_2, u2.year_of_birth as year_of_birth_2, " + 
                    "COUNT(DISTINCT p.photo_ID) AS shared_photo_count " + 
                    "FROM " + UsersTable + " u1 " +
                    "JOIN " + TagsTable + " t1 ON t1.tag_subject_id = u1.user_ID " +
                    "JOIN " + TagsTable + " t2 ON t1.tag_photo_id = t2.tag_photo_id AND t2.tag_subject_id <> u1.user_ID " +
                    "JOIN " + UsersTable + " u2 ON t2.tag_subject_id = u2.user_ID AND u1.user_ID < u2.user_ID " +
                    "JOIN " + PhotosTable + " p ON p.photo_ID = t1.tag_photo_id " + 
                    "LEFT JOIN " + FriendsTable + " f ON (f.user1_ID = u1.user_ID AND f.user2_ID = u2.user_ID) OR (f.user1_ID = u2.user_ID AND f.user2_ID = u1.user_ID) " +
                    "WHERE u1.gender = u2.gender AND ABS(u1.year_of_birth - u2.year_of_birth) <= " + yearDiff + " " +
                     "GROUP BY t1.tag_photo_id, u1.user_ID, u1.first_name, u1.last_name, u1.year_of_birth, u2.user_ID, u2.first_name, u2.last_name, u2.year_of_birth " +
                     "ORDER BY shared_photo_count DESC, u1.user_ID ASC, u2.user_ID ASC " +
                ") WHERE ROWNUM <= " + num
             );
    
             while(rst.next()){
                long userID = rst.getLong("user_id_1");
                String first = rst.getString("first_name_1");
                String last = rst.getString("last_name_1");
                UserInfo u1 = new UserInfo(userID, first, last);
                long birthYear = rst.getLong("year_of_birth_1");
    
                long userID2 = rst.getLong("user_id_2");
                String first2 = rst.getString("first_name_2");
                String last2 = rst.getString("last_name_2");
                UserInfo u2 = new UserInfo(userID2, first2, last2);
                long birthYear2 = rst.getLong("year_of_birth_2");
    
                MatchPair mp = new MatchPair(u1, birthYear, u2, birthYear2);

                try (Statement stmt2 = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
                ResultSet rst2 = stmt2.executeQuery(
                    "SELECT DISTINCT P.photo_ID, P.photo_link, A.album_ID, A.album_name " + 
                    "FROM " + PhotosTable + " P " +
                    "JOIN " + AlbumsTable + " A ON P.album_ID = A.album_ID " + 
                    "JOIN " + TagsTable + " T1 ON T1.tag_photo_ID = P.photo_ID AND T1.tag_subject_ID = " + userID + " " + 
                    "JOIN " + TagsTable + " T2 ON T2.tag_photo_ID = P.photo_ID AND T2.tag_subject_ID = " + userID2 + " " +
                    "ORDER BY P.photo_ID ASC"
                );

                while(rst2.next()){
                    long photoID = rst2.getLong(1);
                    String photoLink = rst2.getString(2);
                    long albumID = rst2.getLong(3);
                    String albumName = rst2.getString(4);
    
                    PhotoInfo p = new PhotoInfo(photoID, albumID, photoLink, albumName);
                    mp.addSharedPhoto(p);
                }
                results.add(mp);
                rst2.close();

                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }
        }
            rst.close();
            stmt.close();
             
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    
        return results;
    }

    @Override
    // Query 6
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of each of the two users in
    //            the top <num> pairs of users who are not friends but have a lot of
    //            common friends
    //        (B) For each pair identified in (A), find the IDs, first names, and last names
    //            of all the two users' common friends
    public FakebookArrayList<UsersPair> suggestFriends(int num) throws SQLException {
        FakebookArrayList<UsersPair> results = new FakebookArrayList<UsersPair>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /* 
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(16, "The", "Hacker");
                UserInfo u2 = new UserInfo(80, "Dr.", "Marbles");
                UserInfo u3 = new UserInfo(192, "Digit", "Le Boid");
                UsersPair up = new UsersPair(u1, u2);
                up.addSharedFriend(u3);
                results.add(up);
            */
                
                ResultSet rst = stmt.executeQuery(
                "SELECT * FROM (" +
                "SELECT u1.user_id AS user_id1, u1.first_name AS first_name1, u1.last_name AS last_name1, u2.user_id AS user_id2, u2.first_name AS first_name2, u2.last_name AS last_name2, COUNT(*) as mutual_friends_count " + 
                "FROM " + UsersTable + " u1 " +
                "JOIN " + FriendsTable + " f1 ON f1.user1_id = u1.user_id " +
                "JOIN " + FriendsTable + " f2 ON f1.user2_id = f2.user1_id OR f1.user2_id = f2.user2_id OR f1.user1_id = f2.user1_id " +
                "JOIN " + UsersTable + " u2 ON u2.user_id = f2.user2_id OR u2.user_id = f2.user1_id " +
                "WHERE u1.user_id < u2.user_id AND " +
                "NOT EXISTS ( " +
                "SELECT * FROM " + FriendsTable + " F WHERE F.user1_id = u1.user_id AND F.user2_id = u2.user_ID) " +
                "GROUP BY u1.user_id, u1.first_name, u1.last_name, u2.user_id, u2.first_name, u2.last_name " +
                "ORDER BY mutual_friends_count DESC, u1.user_id ASC, u2.user_id ASC " +
                ") WHERE ROWNUM <= " + num
                
             );

             while(rst.next()){
                long userID1 = rst.getLong("user_id1");
                String firstName1 = rst.getString("first_name1");
                String lastName1 = rst.getString("last_name1");

                long userID2 = rst.getLong("user_id2");
                String firstName2 = rst.getString("first_name2");
                String lastName2 = rst.getString("last_name2");

                UserInfo u1 = new UserInfo(userID1, firstName1, lastName1);
                UserInfo u2 = new UserInfo(userID2, firstName2, lastName2);

                UsersPair up = new UsersPair(u1, u2);

                try (Statement stmt2 = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
                ResultSet rst2 = stmt2.executeQuery(
                    
                    "SELECT u.user_id, u.first_name, u.last_name " +
                    "FROM " + UsersTable + " u " +
                    "JOIN " + FriendsTable + " f1 ON (f1.user1_id = " + userID1 + " AND f1.user2_ID = u.user_ID) " +
                        "OR (f1.user2_id = " + userID1 + " AND f1.user1_id = u.user_ID) " +
                    "JOIN " + FriendsTable + " f2 ON (f2.user2_id = " + userID2 + " AND f2.user1_ID = u.user_ID) " +
                        "OR (f2.user1_id = " + userID2 + " AND f2.user2_ID = u.user_ID) " + 
                    "ORDER BY u.user_id ASC"
                    
                );

                while(rst2.next()){
                    long userID3 = rst2.getLong(1);
                    String firstName3 = rst2.getString(2);
                    String lastName3 = rst2.getString(3);

                    UserInfo u3 = new UserInfo(userID3, firstName3, lastName3);
                    up.addSharedFriend(u3);
                }

                results.add(up);
                rst2.close();

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }

             }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
}

    @Override
    // Query 7
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the name of the state or states in which the most events are held
    //        (B) Find the number of events held in the states identified in (A)
    public EventStateInfo findEventStates() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                EventStateInfo info = new EventStateInfo(50);
                info.addState("Kentucky");
                info.addState("Hawaii");
                info.addState("New Hampshire");
                return info;
            */

            EventStateInfo info; //declare object
            long maxCount = 0;

            /*
             * ResultSet rst = stmt.executeQuery("select distinct state_name, count(*) " +
        			"from " + eventTableName + " E left join " + cityTableName + " C on E.event_city_id = C.city_id " +
        			"where state_name is not null " +
                    "group by state_name " +
                    "having count(*) = (select max(count(*)) from " + eventTableName + " E left join " + cityTableName + " C on E.event_city_id = C.city_id where state_name is not null group by state_name)");
             */

            //Step 1 -- calculate state with the most events with separate rst, returns only maxCount
            ResultSet rst = stmt.executeQuery(  
                "SELECT MAX(count) " + 
                "FROM (SELECT COUNT(*) AS count FROM " + EventsTable + " E " +
                "JOIN " + CitiesTable + " C ON E.event_city_ID = C.city_ID " + 
                "GROUP BY C.state_Name)" 
            );

            if(rst.next()) {
                maxCount = rst.getLong(1);
            }

            info = new EventStateInfo(maxCount);

            //Step 2 -- return list of states that correspond to the number of events they hold if it equals maxCount
            rst = stmt.executeQuery(
                "SELECT C.state_name " + 
                "FROM " + EventsTable + " E " +
                "JOIN " + CitiesTable + " C ON E.event_city_ID = C.city_ID " +
                "GROUP BY C.state_name " + 
                "HAVING COUNT(*) = " + maxCount +
                "ORDER BY C.state_name ASC" 
            );

            while(rst.next()) {
                String stateName = rst.getString(1);
                info.addState(stateName);
            }

            //close statements
            rst.close();
            stmt.close();

            //return new EventStateInfo(-1); // placeholder for compilation
            return info;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new EventStateInfo(-1);
        }
    }

    @Override
    // Query 8
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the ID, first name, and last name of the oldest friend of the user
    //            with User ID <userID>
    //        (B) Find the ID, first name, and last name of the youngest friend of the user
    //            with User ID <userID>
    public AgeInfo findAgeInfo(long userID) throws SQLException {

        UserInfo oldestFriend = null;
        UserInfo youngestFriend = null;

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo old = new UserInfo(12000000, "Galileo", "Galilei");
                UserInfo young = new UserInfo(80000000, "Neil", "deGrasse Tyson");
                return new AgeInfo(old, young);
            */
            
            //Need to set both to NULL otherwise the line return new AgeInfo(oldestFriend, youngestFriend)
            //throws an erro saying the variables might not have been initialized
            
            //for oldestFriend, set ORDER BY userID to ASC i year of birth, month, and day, and DESC for User
            ResultSet rst = stmt.executeQuery(
                "SELECT user_ID, First_Name, Last_Name FROM (" +
                "SELECT U.user_ID, U.First_Name, U.Last_Name " +
                "FROM " + UsersTable + " U " + 
                "JOIN " + FriendsTable + " F ON (U.user_id = F.user1_id OR U.user_id = F.user2_id) " +
                "WHERE (F.user1_id = " + userID + " OR F.user2_id = " + userID + ") AND U.user_ID <> " + userID + " " +
                "ORDER BY U.year_of_birth ASC, U.month_of_birth ASC, U.day_of_birth ASC, U.user_id DESC" + ") " +
                "WHERE ROWNUM <= 1"
            );

            if(rst.next()) {
                oldestFriend = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3));
            }
            
            //For youngestFriend, set ORDER BY to the opposite of oldestFriend
            rst = stmt.executeQuery(
                "SELECT user_ID, First_Name, Last_Name FROM (" +
                "SELECT U.user_ID, U.First_Name, U.Last_Name " +
                "FROM " + UsersTable + " U " + 
                "JOIN " + FriendsTable + " F ON (U.user_id = F.user1_id OR U.user_id = F.user2_id) " +
                "WHERE (F.user1_id = " + userID + " OR F.user2_id = " + userID + ") AND U.user_ID <> " + userID + " " +
                "ORDER BY U.year_of_birth DESC, U.month_of_birth DESC, U.day_of_birth DESC, U.user_id ASC" + ") " +
                "WHERE ROWNUM <= 1"
            );

            if (rst.next()) {
                youngestFriend = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3));
            }

            //close statements 
            rst.close();
            stmt.close();

           // return new AgeInfo(new UserInfo(-1, "UNWRITTEN", "UNWRITTEN"), new UserInfo(-1, "UNWRITTEN", "UNWRITTEN")); // placeholder for compilation
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new AgeInfo(new UserInfo(-1, "ERROR", "ERROR"), new UserInfo(-1, "ERROR", "ERROR"));
        }

        return new AgeInfo(oldestFriend, youngestFriend);
    }

    @Override
    // Query 9
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find all pairs of users that meet each of the following criteria
    //              (i) same last name
    //              (ii) same hometown
    //              (iii) are friends
    //              (iv) less than 10 birth years apart
    public FakebookArrayList<SiblingInfo> findPotentialSiblings() throws SQLException {
        FakebookArrayList<SiblingInfo> results = new FakebookArrayList<SiblingInfo>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(81023, "Kim", "Kardashian");
                UserInfo u2 = new UserInfo(17231, "Kourtney", "Kardashian");
                SiblingInfo si = new SiblingInfo(u1, u2);
                results.add(si);
            */

            ResultSet rst = stmt.executeQuery(
                "SELECT U1.user_ID, U1.First_Name, U1.Last_Name, " +
                "U2.user_ID, U2.First_Name, U2.Last_Name " +
                "FROM " + UsersTable + " U1 " +
                "JOIN " + FriendsTable + " F ON U1.user_ID = F.user1_id " +
                "JOIN " + UsersTable + " U2 ON U2.user_ID = F.user2_id " +
                "JOIN " + HometownCitiesTable + " H1 ON U1.user_ID = H1.user_ID " +
                "JOIN " + HometownCitiesTable + " H2 ON U2.user_ID = H2.user_ID " +
                "WHERE U1.Last_Name = U2.Last_Name " +
                "AND H1.hometown_city_ID = H2.hometown_city_ID " +
                "AND ABS(U1.year_of_birth - U2.year_of_birth) < 10 " +
                "AND U1.user_ID < U2.user_ID " +
                "ORDER BY U1.user_ID ASC, U2.user_ID ASC"
            );

            while(rst.next()) {
                UserInfo potentialSibling1 = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3));
                UserInfo potentialSibling2 = new UserInfo(rst.getLong(4), rst.getString(5), rst.getString(6));
                SiblingInfo siblingsInfo = new SiblingInfo(potentialSibling1, potentialSibling2);
                results.add(siblingsInfo);
            }

            //close statements
            rst.close();
            stmt.close();

            //return results;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    // Member Variables
    private Connection oracle;
    private final String UsersTable = FakebookOracleConstants.UsersTable;
    private final String CitiesTable = FakebookOracleConstants.CitiesTable;
    private final String FriendsTable = FakebookOracleConstants.FriendsTable;
    private final String CurrentCitiesTable = FakebookOracleConstants.CurrentCitiesTable;
    private final String HometownCitiesTable = FakebookOracleConstants.HometownCitiesTable;
    private final String ProgramsTable = FakebookOracleConstants.ProgramsTable;
    private final String EducationTable = FakebookOracleConstants.EducationTable;
    private final String EventsTable = FakebookOracleConstants.EventsTable;
    private final String AlbumsTable = FakebookOracleConstants.AlbumsTable;
    private final String PhotosTable = FakebookOracleConstants.PhotosTable;
    private final String TagsTable = FakebookOracleConstants.TagsTable;
}
