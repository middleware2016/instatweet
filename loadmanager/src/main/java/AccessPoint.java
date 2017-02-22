import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by pietro on 2017-02-22.
 */
public class AccessPoint extends UnicastRemoteObject implements AccessPointInterface {
    private DatabaseInterface db;

    public AccessPoint(DatabaseInterface db) throws RemoteException {
        this.db = db;
    }

    @Override
    public TimelineInterface getTimeline(String user) throws RemoteException {
        TimelineInterface ti = (TimelineInterface)db.getTimeline(user);
        if(ti == null) {
            ti = createTimeline(user);
        }
        return ti;
    }

    private TimelineInterface createTimeline(String user) {
        try {
            LoadManager.createTimeline(user);
            return (TimelineInterface)db.getTimeline(user);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
