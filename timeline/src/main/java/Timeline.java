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

    public Timeline(Registry registry, String username, DatabaseInterface db, JMSContext context, Destination dispatchDest) throws RemoteException {
        this.registry=registry;
        this.username=username;
        this.db=db;
        this.context = context;
        this.dispatchDestination = dispatchDest;

        timeline = new ArrayList<>();

        this.tweetProducer = context.createProducer();

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
    public synchronized List<Tweet> getFrom(int ind, int quantity) throws RemoteException {

        List<Tweet> list = new ArrayList<>();

        for(int i = ind-quantity; i<=ind && i< timeline.size(); i++){
            list.add(i, new Tweet(timeline.get(i)));
        }

        return list;
    }

    @Override
    public ImageIcon getFullImage(int imgID) throws RemoteException {
        ImageIcon temp = db.getImage(imgID);
        ImageIcon img = new ImageIcon(temp.getImage(),temp.getDescription());

        return img;

    }

    @Override
    public synchronized void postTweet(Tweet t) throws RemoteException {
        this.tweetProducer.send(dispatchDestination, t);
    }

    @Override
    public void subscribeTo(String userToFollow) throws RemoteException {
        db.addSubscriber(this.username, userToFollow);
    }

    @Override
    public void unsubscribeFrom(String userToUnfollow) throws RemoteException {
        db.removeSubscriber(this.username, userToUnfollow);
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
                System.out.println("Using default rmi ip and port");
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

            System.out.println("Timeline " + args[1] + " unbound");


        } catch (RemoteException | AlreadyBoundException | NotBoundException | InterruptedException | NamingException e){
            e.printStackTrace();
            System.out.println("Exiting the timeline " + args[1]);
            exit(-1);
        }

    }

    @Override
    public void addTweet(Tweet t) throws RemoteException {
        this.timeline.add(t);
    }
}
