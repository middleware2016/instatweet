import javax.swing.*;
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

    public void addUser(String name) throws RemoteException, IllegalArgumentException;
    public void removeUser(String username) throws RemoteException;
    public boolean isUser(String username) throws RemoteException;

    public void addTimeline(String username, Object timeline) throws RemoteException;
    public void removeTimeline(String username) throws RemoteException;
    public Object getTimeline(String username) throws RemoteException;

    public List<String> getSubscribers(String username) throws RemoteException;
    public void addSubscriber(String username, String subscriberUsername) throws RemoteException;
    public void removeSubscriber(String username, String subscriberUsername) throws RemoteException;

    public int addImage(ImageIcon img) throws RemoteException;
    public ImageIcon getImage(int imageID) throws RemoteException;
    public void removeImage(int imageID) throws RemoteException;

    public List<Object> getTimelinesAsList() throws RemoteException;
    public void stopDatabase() throws RemoteException;
}
