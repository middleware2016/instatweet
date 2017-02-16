import javax.swing.*;
import java.awt.*;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.List;

import static java.lang.System.exit;

/**
 * Database that manages users, subscriptions and images
 *
 * @author Alex Delbono
 */
public class Database extends UnicastRemoteObject implements DatabaseInterface{

    private Registry registry;
    private String rmi_name;
    private Map<Integer, String> users;
    private int nextUserID;
    private Map<Integer, Object> timelines;
    private Map<Integer, List<Integer>> subscriptions;
    private Map<Integer, ImageIcon> images;
    private int nextImageID;

    private Database(Registry registry, String rmi_name) throws RemoteException {
        this.registry=registry;
        this.rmi_name=rmi_name;
        users = new HashMap<>();
        nextUserID = 0;
        timelines = new HashMap<>();
        subscriptions = new HashMap<>();
        images = new HashMap<>();
        nextImageID = 0;
    }

    public synchronized void runDatabase() throws AlreadyBoundException, RemoteException, NotBoundException, InterruptedException {
        registry.bind(rmi_name, this);

        wait();

        registry.unbind(rmi_name);
    }

    @Override
    public synchronized void stopDatabase(){
        notify();
    }

    public static void main(String args[]){
        if(args.length<1) {
            System.out.println("Database arguments: database-rmi-name [rmi-registry-ip  rmi-registry-port]");
            return;
        }

        try {
            Registry registry;
            if(args.length < 3) {
                System.out.println("Using default rmi ip and port");
                registry = LocateRegistry.getRegistry();
            } else
                registry = LocateRegistry.getRegistry(args[1], Integer.parseInt(args[2]));

            Database db = new Database(registry, args[0]);

            db.runDatabase();


        } catch (RemoteException | AlreadyBoundException | NotBoundException | InterruptedException e){
            e.printStackTrace();
            System.out.println("Exiting the database");
            exit(-1);
        }

    }

    @Override
    public int addUser(String name) throws RemoteException, IllegalArgumentException {
        synchronized (users){
            if(users.containsValue(name))
                throw new IllegalArgumentException("The username is already in the database");
            users.put(nextUserID, name);
            return nextUserID++;
        }
    }

    @Override
    public void removeUser(int userID) throws RemoteException {
        synchronized (users){
            users.remove(userID);
        }
    }

    @Override
    public String getName(int userID) throws RemoteException {
        synchronized (users){
            return users.get(userID);
        }
    }

    @Override
    public void addTimeline(int userID, Object timeline) throws RemoteException {
        timelines.put(userID,timeline);
    }

    @Override
    public void removeTimeline(int userID) throws RemoteException {
        timelines.remove(userID);
    }

    @Override
    public Object getTimeline(int userID) throws RemoteException {
        return timelines.get(userID);
    }

    @Override
    public List<Integer> getSubscribers(int userID) throws RemoteException {

        List<Integer> list;

        synchronized (subscriptions) {
            //Map.get returns null if the key is not present
            list = subscriptions.get(userID);
        }

        if (list != null)
            synchronized (list) {
                return new ArrayList<>(list);
            }
        return null;
    }

    @Override
    public void addSubscriber(int userID, int subscriberID) throws RemoteException {

        List<Integer> list;

        synchronized (subscriptions) {
            //Map.get returns null if the key is not present
            list = subscriptions.get(userID);
            if(list == null){
                list = new ArrayList<>();
                subscriptions.put(userID, list);
            }
        }

        synchronized (list) {
            list.add(subscriberID);
        }


    }

    @Override
    public void removeSubscriber(int userID, int subscriberID) throws RemoteException {

        List<Integer> list;

        synchronized (subscriptions) {
            //Map.get returns null if the key is not present
            subscriptions.remove(userID);
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
            return images.get(imageID);
        }
    }

    @Override
    public void removeImage(int imageID) throws RemoteException {
        synchronized (images){
            images.remove(imageID);
        }
    }
}
