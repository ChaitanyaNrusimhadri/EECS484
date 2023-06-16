import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeSet;
import java.util.Vector;

import org.json.JSONObject;
import org.json.JSONArray;

//Added the following hashmap data structure
import java.util.HashMap;

public class GetData {

    static String prefix = "project3.";

    // You must use the following variable as the JDBC connection
    Connection oracleConnection = null;

    // You must refer to the following variables for the corresponding 
    // tables in your database
    String userTableName = null;
    String friendsTableName = null;
    String cityTableName = null;
    String currentCityTableName = null;
    String hometownCityTableName = null;

    // DO NOT modify this constructor
    public GetData(String u, Connection c) {
        super();
        String dataType = u;
        oracleConnection = c;
        userTableName = prefix + dataType + "_USERS";
        friendsTableName = prefix + dataType + "_FRIENDS";
        cityTableName = prefix + dataType + "_CITIES";
        currentCityTableName = prefix + dataType + "_USER_CURRENT_CITIES";
        hometownCityTableName = prefix + dataType + "_USER_HOMETOWN_CITIES";
    }

    // TODO: Implement this function
    @SuppressWarnings("unchecked")
    public JSONArray toJSON() throws SQLException {

        // This is the data structure to store all users' information
        JSONArray users_info = new JSONArray();


        try (Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            // Your implementation goes here....

            ResultSet rst = stmt.executeQuery(
                "SELECT * " +
                "FROM " + userTableName
            );

            while(rst.next()) {
                JSONObject my_obj = new JSONObject();
                my_obj.put("user_id", rst.getInt(1));
                my_obj.put("first_name", rst.getString(2));
                my_obj.put("last_name", rst.getString(3));
                my_obj.put("YOB", rst.getInt(4));
                my_obj.put("MOB", rst.getInt(5));
                my_obj.put("DOB", rst.getInt(6));
                my_obj.put("gender", rst.getString(7));
                my_obj.put("friends", new JSONArray());
                my_obj.put("current", new JSONObject());
                my_obj.put("hometown", new JSONObject());

                try (Statement stmt2 = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

                    //Hometown Query
                    ResultSet rst2 = stmt2.executeQuery(
                        "SELECT C.city_name, C.state_name, C.country_name " +
                        "FROM " + hometownCityTableName + " H, " + cityTableName + " C " + 
                        "WHERE C.city_id = H.hometown_city_id AND H.user_id = " + rst.getInt(1)
                    );

                    if(rst2.next()) {
                        my_obj.getJSONObject("hometown").put("city", rst2.getString(1));
                        my_obj.getJSONObject("hometown").put("state", rst2.getString(2));
                        my_obj.getJSONObject("hometown").put("country", rst2.getString(3));
                    }

                    rst2.close();


                    //Current City Query
                    ResultSet rst3 = stmt2.executeQuery(
                        "SELECT C.city_name, C.state_name, C.country_name " +
                        "FROM " + currentCityTableName + " CC, " + cityTableName + " C " +
                        "WHERE C.city_id = CC.current_city_id AND CC.user_id = " + rst.getInt(1)
                    );

                    if(rst3.next()) {
                        my_obj.getJSONObject("current").put("city", rst3.getString(1));
                        my_obj.getJSONObject("current").put("state", rst3.getString(2));
                        my_obj.getJSONObject("current").put("country", rst3.getString(3));
                    }
                    
                    rst3.close();

                    //Friends Query
                    ResultSet rst4 = stmt2.executeQuery(
                        "SELECT user2_id " +
                        "FROM " + friendsTableName + " " +
                        "WHERE user1_id = " + rst.getInt(1)  
                    );

                    while(rst4.next()) {
                        my_obj.getJSONArray("friends").put(rst4.getInt(1)); 
                    }

                    rst4.close();

                    //rst2.close();
                    stmt2.close();
                    
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }
                
                //Inserted into users_info -------------------------------------------------------
                users_info.put(my_obj);
            }

                rst.close();
                stmt.close();

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }

        return users_info;
    }

    // This outputs to a file "output.json"
    // DO NOT MODIFY this function
    public void writeJSON(JSONArray users_info) {
        try {
            FileWriter file = new FileWriter(System.getProperty("user.dir") + "/output.json");
            file.write(users_info.toString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
