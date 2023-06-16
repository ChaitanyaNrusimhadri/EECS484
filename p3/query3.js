// Query 3
// Create a collection "cities" to store every user that lives in every city. Each document(city) has following schema:
// {
//   _id: city
//   users:[userids]
// }
// Return nothing.

function cities_table(dbname) {
    db = db.getSiblingDB(dbname);

    // TODO: implement cities collection here
    db.users.aggregate([
        
        //replaced push with addToSet to ensure distinct user IDs are pushed
        { $group : {_id: "$current.city" , users: {$addToSet: "$user_id"} }},

        { $out : "cities"}
        
    ]);

    return;
}
