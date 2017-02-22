import interfaces.AccessPointInterface;
import interfaces.TimelineInterface;
import payloads.Tweet;

import javax.jms.Destination;
import javax.swing.*;
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
            logger.fine("Sent a tweet: " + t.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void subscribe(String subscriber, String toFollow) {
        try {
            getTimelineForUser(subscriber).subscribeTo(toFollow);
            logger.fine("A subscription was sent.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void unsubscribe(String subscriber, String toUnfollow) {
        try {
            getTimelineForUser(subscriber).unsubscribeFrom(toUnfollow);
            logger.fine("A subscription removal was sent.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ImageIcon getFullImage(String user, int imgID) {
        try{
            return getTimelineForUser(user).getFullImage(imgID);
        } catch (RemoteException | NullPointerException e) {
            e.printStackTrace();
            return new ImageIcon();
        }
    }

    @Override
    public List<Tweet> getLastTweets(String user, int quantity) {
        if(quantity < 0)
            throw new IllegalArgumentException("The number of requested tweets cannot be negative.");
        try {
            return getTimelineForUser(user).getLastTweets(quantity);
        } catch (RemoteException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
