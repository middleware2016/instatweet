import payloads.Tweet;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by pietro on 2017-02-19.
 */
public interface ClientInterface {
    public void tweet(String user, String text, Image image);
    public void subscribe(String follower, String toFollow);
    public void unsubscribe(String follower, String toUnfollow);
    public ImageIcon getFullImage(String user, int imgID);
    public List<Tweet> getLastTweets(String user, int quantity);
}
