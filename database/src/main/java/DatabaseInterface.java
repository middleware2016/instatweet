import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface of the database that manages users, subscriptions and images
 *
 * @author Alex Delbono
 */
public interface DatabaseInterface extends Remote{

    public int addUser(String name) throws RemoteException, IllegalArgumentException;
    public void removeUser(int userID) throws RemoteException;
    public String getName(int userID) throws RemoteException;

    public List<Integer> getSubscribers(int userID) throws RemoteException;
    public void addSubscriber(int userID, int subscriberID) throws RemoteException;
    public void removeSubscriber(int userID, int subscriberID) throws RemoteException;

    public int addImage(Image img) throws RemoteException;
    public Image getImage(int imageID) throws RemoteException;
    public void removeImage(int imageID) throws RemoteException;

}
