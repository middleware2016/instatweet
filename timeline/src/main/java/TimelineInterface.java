import javax.swing.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


/**
 * Created by alex on 16/02/2017.
 */
public interface TimelineInterface extends Remote {

    public void deleteTimeline() throws RemoteException;


    public int getIndexLast() throws RemoteException;
    public List<Tweet> getFrom(int ind, int quantity) throws RemoteException;
    public ImageIcon getFullImage(int imgID) throws RemoteException;

    public void addTweet(Tweet t) throws RemoteException;

}
