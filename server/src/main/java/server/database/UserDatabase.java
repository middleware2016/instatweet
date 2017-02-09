package server.database;

import server.user.User;
import server.user.UserSubscription;

/**
 * Interface that provides method to add and remove
 * users and subscriptions
 *
 * @author Alex Delbono
 */
public interface UserDatabase {

    public void addUser(User user);
    public User getUser(String name);
    public void removeUser(String name);

}
