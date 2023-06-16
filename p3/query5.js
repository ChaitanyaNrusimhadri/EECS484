// Query 5
// Find the oldest friend for each user who has a friend. For simplicity,
// use only year of birth to determine age, if there is a tie, use the
// one with smallest user_id. You may find query 2 and query 3 helpful.
// You can create selections if you want. Do not modify users collection.
// Return a javascript object : key is the user_id and the value is the oldest_friend id.
// You should return something like this (order does not matter):
// {user1:userx1, user2:userx2, user3:userx3,...}

function oldest_friend(dbname) {
    db = db.getSiblingDB(dbname);


    let results = {};
    let users = db.users.find();
    
    // Recreate the 'flat_users' collection using the code from query 2
    db.users.aggregate([
        { $unwind: "$friends" },
        { $project: { _id: 0, user_id: 1, friends: 1 } },
        { $out: "flat_users" }
    ]);

    db.flat_users.find().forEach(function(document) {
            //Arigatou Gozaimashta Richard-Sama.
            //*kowtows 90 degrees*
        db.flat_users.insert({ user_id: document.friends, friends: document.user_id });
    });

    users.forEach(function(user) {
        // Find all friends' user_id
        let friendsUserIds = db.flat_users.find(
            { $or: [
                { user_id: user.user_id },
                { friends: user.user_id }
            ]}
        ).map(function(doc) { return doc.user_id == user.user_id ? doc.friends : doc.user_id });

        let friends = db.users.find(
            { user_id: { $in: friendsUserIds } }
        ).sort({ "YOB": 1, "user_id": 1 }).limit(1);
        
        if (friends.hasNext()) {
            let oldest_friend = friends.next();
            results[user.user_id] = oldest_friend.user_id;
        }
    });

    return results;
}

//query users. In the .find(), check it they are friends
//if user_id == friend
