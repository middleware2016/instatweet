import java.io.Serializable;

/**
 * Class used as payload of JMS Message that represents the request of creating a new user
 *
 * @author Alex Delbono
 */
public class NewUser implements Serializable {

    private String username;

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "NewUser{" +
                "username='" + username + '\'' +
                '}';
    }
}
