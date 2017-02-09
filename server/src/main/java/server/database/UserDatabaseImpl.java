package server.database;

import server.user.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple and not persistent implementation of the database
 *
 * @auth Alex Delbono
 */
public class UserDatabaseImpl implements UserDatabase {

    private Map<String, User> data;


    public UserDatabaseImpl(){
        data = new HashMap<>();
    }


    @Override
    public void addUser(User user) {
        data.put(user.getUserName(), user);
    }

    @Override
    public User getUser(String name) {
        return data.get(name);
    }

    @Override
    public void removeUser(String name) {
        data.remove(name);
    }

}
