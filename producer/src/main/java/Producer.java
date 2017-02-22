import interfaces.AccessPointInterface;
import interfaces.TimelineInterface;
import payloads.Tweet;

import javax.jms.Destination;
import java.awt.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Producer implements ClientInterface {
    private Destination inputDestination;
    private Registry registry;
    private AccessPointInterface api;

    private static Logger logger = Logger.getLogger(Producer.class.getName());

    public Producer(Registry registry, String apiName) {
        this.registry = registry;

        try {
            this.api = (AccessPointInterface)registry.lookup(apiName);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    private TimelineInterface getTimelineForUser(String user) {
        TimelineInterface ti = null;
        try {
            ti = api.getTimeline(user);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ti;
    }

    @Override
    public void tweet(String user, String text, Image image) {
        Tweet t = new Tweet(user, text, image);
        try {
            getTimelineForUser(user).postTweet(t);
            logger.info("Sent a tweet: " + t.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void subscribe(String subscriber, String user) {
        try {
            getTimelineForUser(subscriber).subscribeTo(user);
            logger.info("A subscription was sent.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void unsubscribe(String subscriber, String user) {
        try {
            getTimelineForUser(subscriber).unsubscribeFrom(user);
            logger.info("A subscription removal was sent.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Tweet> getTimeline(String user) {
        try {
            return getTimelineForUser(user).getFrom(0, 100);
        } catch (RemoteException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
