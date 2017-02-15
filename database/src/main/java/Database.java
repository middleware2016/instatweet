import java.awt.*;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database that manages subscriptions and images
 *
 * @author Alex Delbono
 */
public class Database extends UnicastRemoteObject implements DatabaseInterface{

    private Map<Integer, List<Integer>> subscriptions;
    private Map<Integer, Image> images;
    private int nextImageID;

    private Database() throws RemoteException {
        subscriptions = new HashMap<>();
        images = new HashMap<>();
        nextImageID = 0;
    }

    public static void main(String args[]){
        if(args.length<2) {
            System.out.println("Database arguments: database-rmi-name [rmi-registry-ip  rmi-registry-port]");
            return;
        }

        try {
            Registry registry;
            if(args.length < 4) {
                System.out.println("Using default rmi ip and port");
                registry = LocateRegistry.getRegistry();
            } else
                registry = LocateRegistry.getRegistry(args[2], Integer.parseInt(args[3]));

            Database db = new Database();

            registry.bind(args[1], db);


        } catch (RemoteException | AlreadyBoundException e){
            e.printStackTrace();
            System.out.println("Exiting the database");
        }

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
    public int addImage(Image img) throws RemoteException {
        synchronized (images) {
            images.put(nextImageID, img);
            return nextImageID++;
        }
    }

    @Override
    public Image getImage(int imageID) throws RemoteException {
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
