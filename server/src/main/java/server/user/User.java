package server.user;


import javax.print.attribute.standard.Destination;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that wrap up all the JMS resources of a user
 *
 * @author Alex Delbono
 */
public class User {

    /**
     * Name of the user
     */
    private final String userName;

    /**
     * Input destination used for getting text and photo
     * from the user
     */
    private final Destination inputJMSResource;
    private final String inputJMSResourceJNDI;

    /**
     * Resource used for notifying to all the subscribers
     * that the user has published something
     */
    private final Destination publisherJMSResource;
    private final String publisherJMSResourceJNDI;

    /**
     * Resource used for constructing the timeline
     */
    private final Destination timelineJMSResource;
    private final String timelineJMSResourceJNDI;

    /**
     * Map that contains all the subscriptions of the user
     */
    private Map<String, UserSubscription> subscriptions;



    public User(String userName,
                Destination inputJMSResource, String inputJMSResourceJNDI,
                Destination publisherJMSResource, String publisherJMSResourceJNDI,
                Destination timelineJMSResource, String timelineJMSResourceJNDI){

        this.userName=userName;

        this.inputJMSResource=inputJMSResource;
        this.inputJMSResourceJNDI=inputJMSResourceJNDI;

        this.publisherJMSResource=publisherJMSResource;
        this.publisherJMSResourceJNDI=publisherJMSResourceJNDI;

        this.timelineJMSResource=timelineJMSResource;
        this.timelineJMSResourceJNDI=timelineJMSResourceJNDI;

        subscriptions = new HashMap<>();
    }

    public String getUserName() {
        return userName;
    }

    public Destination getInputJMSResource() {
        return inputJMSResource;
    }

    public String getInputJMSResourceJNDI() {
        return inputJMSResourceJNDI;
    }

    public Destination getPublisherJMSResource() {
        return publisherJMSResource;
    }

    public String getPublisherJMSResourceJNDI() {
        return publisherJMSResourceJNDI;
    }

    public Destination getTimelineJMSResource() {
        return timelineJMSResource;
    }

    public String getTimelineJMSResourceJNDI() {
        return timelineJMSResourceJNDI;
    }

    /**
     * Add new subscription.
     * If {@paramref name} is already present, it replaces the previous subscription
     *
     * @param name this is the name of the user who is followed
     *
     * @param subscription this is the object that manages the subscription
     */
    public void addSubscription(String name, UserSubscription subscription){
        subscriptions.put(name,subscription);
    }

    /**
     * Removes the mapping for the specified {@paramref name} from this map if present
     * @param name this is the name of the user who is followed
     */
    public void deleteSubscription(String name){
        subscriptions.remove(name);
    }

}
