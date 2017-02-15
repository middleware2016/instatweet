import java.io.Serializable;

/**
 * Class used as payload of JMS Message that represents the request of creating a subscription
 *
 * @author Alex Delbono
 */
public class NewFollower implements Serializable{

    private String username;
    private String toBeFollowedUsername;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToBeFollowedUsername() {
        return toBeFollowedUsername;
    }

    public void setToBeFollowedUsername(String toBeFollowedUsername) {
        this.toBeFollowedUsername = toBeFollowedUsername;
    }
}
