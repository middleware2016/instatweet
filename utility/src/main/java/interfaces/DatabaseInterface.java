package interfaces;

import javax.swing.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface of the database that manages users, subscriptions and images
 *
 * @author Alex Delbono
 */
public interface DatabaseInterface extends Remote{

    public void addUser(String username, TimelineInterface timeline) throws RemoteException, IllegalArgumentException;
    public boolean isUser(String username) throws RemoteException;
    public TimelineInterface removeUser(String username) throws RemoteException;
    public TimelineInterface getTimeline(String username) throws RemoteException;

    public List<String> getSubscribers(String username) throws RemoteException;
    public void addSubscriber(String username, String subscriberUsername) throws RemoteException;
    public void removeSubscriber(String username, String subscriberUsername) throws RemoteException;

    public int addImage(ImageIcon img) throws RemoteException;
    public ImageIcon getImage(int imageID) throws RemoteException;
    public void removeImage(int imageID) throws RemoteException;

    public List<TimelineInterface> getTimelinesAsList() throws RemoteException;
    public void stop() throws RemoteException;
    public void start() throws RemoteException;
}
