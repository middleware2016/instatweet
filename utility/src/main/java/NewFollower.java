import java.io.Serializable;

/**
 * Class used as payload of JMS Message that represents the request of creating or removing a subscription
 *
 * @author Alex Delbono
 */
public class NewFollower implements Serializable{

    private String username;
    private String toBeFollowedUsername;
    private boolean remove;

    public String getUsername() {
        return username;
    }

    public String getToBeFollowedUsername() {
        return toBeFollowedUsername;
    }

    public boolean isRemove() { return remove; }

    public NewFollower(String username, String toBeFollowedUsername, boolean removeSubscription) {
        this.username = username;
        this.toBeFollowedUsername = toBeFollowedUsername;
        this.remove = removeSubscription;
    }

    @Override
    public String toString() {
        return "NewFollower{" +
                "username='" + username + '\'' +
                ", toBeFollowedUsername='" + toBeFollowedUsername + '\'' +
                ", remove=" + remove +
                '}';
    }
}
