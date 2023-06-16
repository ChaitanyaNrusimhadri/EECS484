// Query 8
// Find the city average friend count per user using MapReduce.

let city_average_friendcount_mapper = function () {
    // TODO: Implement the map function
    emit(this.hometown.city, {users: 1, friends: this.friends.length});
};

let city_average_friendcount_reducer = function (key, values) {
    // TODO: Implement the reduce function
    var toReturn = {users: 0, friends: 0};
    for (let count in values) {
        toReturn.users += values[count].users;
        toReturn.friends += values[count].friends;
    }
    return toReturn;
};

let city_average_friendcount_finalizer = function (key, reduceVal) {
    // We've implemented a simple forwarding finalize function. This implementation
    // is naive: it just forwards the reduceVal to the output collection.
    // TODO: Feel free to change it if needed.
    return 1.0 * reduceVal.friends / reduceVal.users;
};
