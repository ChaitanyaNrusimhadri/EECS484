// Query 6
// Find the average friend count per user.
// Return a decimal value as the average user friend count of all users in the users collection.

function find_average_friendcount(dbname) {
    db = db.getSiblingDB(dbname);

    // TODO: calculate the average friend count

    //make a for loop instead of count function

    //parseFloat ensures floating point arithmetic 
    //return parseFloat(db.flat_users.find().count()) / db.users.find().count();
    //return 0;

    let totalUsers = 0;
    let totalFriends = 0;


    // Count the total number of users
    db.users.find().forEach(function(user) {
        totalUsers++;
    });
   
    // ADDED: collection called flat_users not implemented here lol
    // implemented below, used later
    db.users.aggregate([
       
        { $unwind: "$friends" },
       
        { $project: { _id: 0, user_id: 1, friends: 1 } },
       
        { $out: "flat_users" }
    ]);


    // Count the total number of friends
    db.flat_users.find().forEach(function(user) {
        totalFriends++;
    });


    // Calculate the average friend count
    let avgFriendCount = totalFriends / totalUsers;


    return avgFriendCount;
}
