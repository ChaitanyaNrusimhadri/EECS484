// Query 4
// Find user pairs (A,B) that meet the following constraints:
// i) user A is male and user B is female
// ii) their Year_Of_Birth difference is less than year_diff
// iii) user A and B are not friends
// iv) user A and B are from the same hometown city
// The following is the schema for output pairs:
// [
//      [user_id1, user_id2],
//      [user_id1, user_id3],
//      [user_id4, user_id2],
//      ...
//  ]
// user_id is the field from the users collection. Do not use the _id field in users.
// Return an array of arrays.

function suggest_friends(year_diff, dbname) {
    db = db.getSiblingDB(dbname);

    let maleUsers = db.users.find({gender: "male"}).toArray();
    let femaleUsers = db.users.find({gender: "female"}).toArray();

    let pairs = [];
    // TODO: implement suggest friends

    maleUsers.forEach(function(maleUser) {
        femaleUsers.forEach(function(femaleUser) {
            // Check if they are from the same hometown
            if(maleUser.hometown.city === femaleUser.hometown.city) {
                // Check if the difference in their year of birth is less than `year_diff`
                if(Math.abs(maleUser.YOB - femaleUser.YOB) < year_diff) {
                    // Check if they are not friends
                    if(maleUser.friends.indexOf(femaleUser.user_id) === -1 && femaleUser.friends.indexOf(maleUser.user_id) === -1) {
                        pairs.push([maleUser.user_id, femaleUser.user_id]);
                    }
                }
            }
        });
    });

    return pairs;
}
