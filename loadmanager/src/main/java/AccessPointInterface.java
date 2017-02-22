import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by pietro on 2017-02-22.
 */
public interface AccessPointInterface extends Remote {
    public TimelineInterface getTimeline(String user) throws RemoteException;
}
