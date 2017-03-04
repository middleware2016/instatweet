import configurator.Configurator;
import interfaces.DatabaseInterface;
import interfaces.TimelineInterface;

import javax.swing.*;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.Logger;

import static java.lang.System.exit;

/**
 * Database that manages users, subscriptions and images
 *
 * @author Alex Delbono
 */
public class Database extends UnicastRemoteObject implements DatabaseInterface {

    private Registry registry;
    private String rmi_name;
    private Map<String, TimelineInterface> users;
    private Map<String, Set<String>> subscriptions;
    private Map<Integer, ImageIcon> images;
    private int nextImageID;
    private boolean stop = false;

    private static Logger logger = Logger.getLogger(Database.class.getName());

    public Database(Registry registry, String rmi_name) throws RemoteException {
        super();
        this.registry=registry;
        this.rmi_name=rmi_name;
        users = new HashMap<>();
        subscriptions = new HashMap<>();
        images = new HashMap<>();
        nextImageID = 0;
    }

    public synchronized void stop() throws RemoteException{
        stop = true;
        notify();
    }

    public synchronized void start() {
        while(!stop) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]){
        try {
            Configurator config = Configurator.getInstance();
            Registry registry;
            //Initialize RMI
            if (config.get("rmi_ip") == null) {
                logger.fine("Using default rmi ip and port");
                registry = LocateRegistry.getRegistry();
            } else {
                registry = LocateRegistry.getRegistry(config.get("rmi_ip"), Integer.parseInt(config.get("rmi_port")));
            }

            DatabaseInterface db = new Database(registry, config.get("database_rmi_name"));
            logger.fine("Binding database");
            registry.bind(config.get("database_rmi_name"), db);
            db.start();
            registry.unbind(config.get("database_rmi_name"));

            exit(0);


        } catch (RemoteException | AlreadyBoundException | NotBoundException e){
            e.printStackTrace();
            logger.severe("Exiting the database: " + e.toString());
            exit(-1);
        }

    }

    @Override
    public void addUser(String username, TimelineInterface timeline) throws RemoteException, IllegalArgumentException {
        synchronized (subscriptions){
            if(isUser(username))
                throw new IllegalArgumentException("The username is already in the database");
            users.put(username, timeline);
            subscriptions.put(username, new TreeSet<>());
        }
    }

    @Override
    public TimelineInterface removeUser(String username) throws RemoteException {
        synchronized (subscriptions){
            subscriptions.remove(username);
            return users.remove(username);
        }
    }

    @Override
    public boolean isUser(String username) throws RemoteException {
        return users.containsKey(username);
    }


    @Override
    public TimelineInterface getTimeline(String username) throws RemoteException {
        return users.get(username);
    }

    @Override
    public List<String> getSubscribers(String username) throws RemoteException {

        Set<String> followersSet;

        synchronized (subscriptions) {
            // Map.get returns null if the key is not present
            followersSet = subscriptions.get(username);
        }

        if (followersSet != null) {
            synchronized (followersSet) {
                return new ArrayList<>(followersSet);
            }
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void addSubscriber(String username, String subscriberUsername) throws RemoteException {

        Set<String> followersSet;

        synchronized (subscriptions) {
            // Map.get returns null if the key is not present
            followersSet = subscriptions.get(username);
        }

        synchronized (followersSet) {
            followersSet.add(subscriberUsername);
        }

    }

    @Override
    public void removeSubscriber(String username, String subscriberUsername) throws RemoteException {

        Set<String> followersSet;

        synchronized (subscriptions) {
            // Map.get returns null if the key is not present
            followersSet = subscriptions.get(username);
        }

        synchronized (followersSet){
            followersSet.remove(subscriberUsername);
        }
    }

    @Override
    public int addImage(ImageIcon img) throws RemoteException {
        synchronized (images) {
            images.put(nextImageID, img);
            return nextImageID++;
        }
    }

    @Override
    public ImageIcon getImage(int imageID) throws RemoteException {
        synchronized (images){
            ImageIcon ii = images.get(imageID);
            return ii != null ? ii : new ImageIcon();
        }
    }

    @Override
    public void removeImage(int imageID) throws RemoteException {
        synchronized (images){
            images.remove(imageID);
        }
    }

    @Override
    public List<TimelineInterface> getTimelinesAsList() throws RemoteException {
        Collection<TimelineInterface> coll = users.values();
        List<TimelineInterface> list = new ArrayList<>();

        for(TimelineInterface o : coll)
            list.add(o);

        return list;
    }
}
