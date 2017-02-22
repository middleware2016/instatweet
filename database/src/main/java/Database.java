import interfaces.DatabaseInterface;

import javax.swing.*;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import static java.lang.System.exit;

/**
 * Database that manages users, subscriptions and images
 *
 * @author Alex Delbono
 */
public class Database extends UnicastRemoteObject implements DatabaseInterface {

    private Registry registry;
    private String rmi_name;
    private Map<String, Object> users;
    private Map<String, List<String>> subscriptions;
    private Map<Integer, ImageIcon> images;
    private int nextImageID;

    public Database(Registry registry, String rmi_name) throws RemoteException {
        super();
        this.registry=registry;
        this.rmi_name=rmi_name;
        users = new HashMap<>();
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

            exit(0);


        } catch (RemoteException | AlreadyBoundException | NotBoundException | InterruptedException e){
            e.printStackTrace();
            System.out.println("Exiting the database");
            exit(-1);
        }

    }

    @Override
    public void addUser(String username, Object timeline) throws RemoteException, IllegalArgumentException {
        synchronized (users){
            if(isUser(username))
                throw new IllegalArgumentException("The username is already in the database");
            users.put(username, timeline);
        }
    }

    @Override
    public Object removeUser(String username) throws RemoteException {
        synchronized (users){
            return users.remove(username);
        }
    }

    @Override
    public boolean isUser(String username) throws RemoteException {
        return users.containsKey(username);
    }


    @Override
    public Object getTimeline(String username) throws RemoteException {
        return users.get(username);
    }

    @Override
    public List<String> getSubscribers(String username) throws RemoteException {

        List<String> list;

        synchronized (subscriptions) {
            //Map.get returns null if the key is not present
            list = subscriptions.get(username);
        }

        if (list != null)
            synchronized (list) {
                return new ArrayList<>(list);
            }
        return null;
    }

    @Override
    public void addSubscriber(String username, String subscriberUsername) throws RemoteException {

        List<String> list;

        synchronized (subscriptions) {
            //Map.get returns null if the key is not present
            list = subscriptions.get(username);
            if(list == null){
                list = new ArrayList<>();
                subscriptions.put(username, list);
            }
        }

        synchronized (list) {
            list.add(subscriberUsername);
        }


    }

    @Override
    public void removeSubscriber(String username, String subscriberUsername) throws RemoteException {

        List<String> list;

        synchronized (subscriptions) {
            //Map.get returns null if the key is not present
            list = subscriptions.get(username);
        }

        if(list!=null){
            synchronized (list){
                list.remove(subscriberUsername);
            }
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

    @Override
    public List<Object> getTimelinesAsList() throws RemoteException {
        Collection<Object> coll = users.values();
        List<Object> list = new ArrayList<>();

        for(Object o : coll)
            list.add(o);

        return list;
    }
}
