// Query 1
// Find users who live in city "city".
// Return an array of user_ids. The order does not matter.

function find_user(city, dbname) {
    db = db.getSiblingDB(dbname);

    let results = [];
    // TODO: find all users who live in city
    // db.users.find(...);

    // See test.js for a partial correctness check.
   
    /*
    // Aggregate pipeline
    db.User_Hometown_Cities.aggregate([
        // First stage: left join with Cities on city_id
        {
            $lookup: {
                from: "Cities",
                localField: "hometown_city_id",
                foreignField: "city_id",
                as: "city_info"
            }
        },
        // Second stage: filter documents where city_name matches the given city
        {
            $match: { "city_info.city_name": city }
        }
    ]).forEach(
        function (user) {
            // push the user id for each user w/ matching hometown city
            results.push(user.user_id);
        }
    ); 
    */

    db.users.find({"hometown.city": city}).forEach(function(user) {results.push(user.user_id)});

    return results;
}
