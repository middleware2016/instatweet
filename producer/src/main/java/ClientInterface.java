import java.awt.*;
import java.util.List;

/**
 * Created by pietro on 2017-02-19.
 */
public interface ClientInterface {
    public void tweet(String user, String text, Image image);
    public void subscribe(String subscriber, String user);
    public void unsubscribe(String subscriber, String user);
    public List<Tweet> getTimeline();
}
