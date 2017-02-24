import interfaces.DatabaseInterface;
import interfaces.TimelineInterface;
import interfaces.TimelineUpdateInterface;
import payloads.Tweet;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.*;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.System.exit;

/**
 * Class that manages the timeline of the user
 *
 * @author Alex Delbono
 */
public class Timeline extends UnicastRemoteObject implements TimelineInterface, TimelineUpdateInterface {

    private Registry registry;
    private String username;
    private DatabaseInterface db;

    private List<Tweet> timeline;

    private JMSContext context;
    private Destination dispatchDestination;
    private JMSProducer tweetProducer;

    private static Logger logger = Logger.getLogger(Timeline.class.getName());

    public Timeline(Registry registry, String username, DatabaseInterface db, JMSContext context, Destination dispatchDest) throws RemoteException {
        super();
        this.registry=registry;
        this.username=username;
        this.db=db;
        this.context = context;
        this.dispatchDestination = dispatchDest;

        timeline = new ArrayList<>();

        this.tweetProducer = context.createProducer();

        db.addUser(username, this);

        // By default, every user subscribes to himself. In this way no tweets are "lost".
        this.subscribeTo(this.username);
    }

    public synchronized void runTimeline() throws AlreadyBoundException, RemoteException, NotBoundException, InterruptedException {

        registry.bind("user_"+username, this);

        wait();

        registry.unbind("user_"+username);
    }

    @Override
    public synchronized void deleteTimeline() throws RemoteException {
        notify();
    }

    @Override
    public synchronized int getIndexLast() throws RemoteException {
        return timeline.size()-1;
    }

    @Override
    public synchronized List<Tweet> getLastTweets(int quantity) throws RemoteException {
        if(quantity > timeline.size())
            quantity = timeline.size();
        return new ArrayList<>(timeline.subList(timeline.size()-quantity, timeline.size()));
    }

    @Override
    public ImageIcon getFullImage(int imgID) throws RemoteException {
        ImageIcon temp = db.getImage(imgID);
        if (temp.getImage()!= null) {
            return new ImageIcon(temp.getImage());
        }
        return temp;

    }

    @Override
    public synchronized void postTweet(Tweet t) throws RemoteException {
        this.tweetProducer.send(dispatchDestination, t);
    }

    @Override
    public void subscribeTo(String toFollow) throws RemoteException {
        db.addSubscriber(toFollow, this.username);
    }

    @Override
    public void unsubscribeFrom(String toUnfollow) throws RemoteException {
        db.removeSubscriber(toUnfollow, this.username);
    }

    public static void main(String args[]){
        if(args.length<2) {
            System.out.println("Timeline arguments: username database-rmi-name connection-factory-name dispatch-queue" +
                    "[rmi-registry-ip  rmi-registry-port]");
            return;
        }

        try {
            Registry registry;
            if(args.length < 5) {
                logger.fine("Using default rmi ip and port");
                registry = LocateRegistry.getRegistry();
            } else
                registry = LocateRegistry.getRegistry(args[4], Integer.parseInt(args[5]));

            DatabaseInterface db = (DatabaseInterface) registry.lookup(args[1]);

            // JMS init
            Context jndiContext = new InitialContext();
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(args[2]);
            Destination dispatchDest = (Destination) jndiContext.lookup(args[3]);
            JMSContext context = connectionFactory.createContext();

            Timeline timeline = new Timeline(registry, args[0], db, context, dispatchDest);

            timeline.runTimeline();

            logger.fine("Timeline " + args[1] + " unbound");

            exit(0);

        } catch (RemoteException | AlreadyBoundException | NotBoundException | InterruptedException | NamingException e){
            logger.severe("Exiting the timeline " + args[1] + ": " + e.toString());
            exit(-1);
        }

    }

    @Override
    public void addTweet(Tweet t) throws RemoteException {
        this.timeline.add(t);
        logger.info(String.format("[@%s timeline] %s", this.username, t.toString()));
    }
}
