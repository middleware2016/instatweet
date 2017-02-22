import interfaces.AccessPointInterface;
import interfaces.DatabaseInterface;
import interfaces.TimelineInterface;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

/**
 * Created by pietro on 2017-02-22.
 */
public class AccessPoint extends UnicastRemoteObject implements AccessPointInterface {
    private DatabaseInterface db;
    private static Logger logger = Logger.getLogger(AccessPoint.class.getName());

    public AccessPoint(DatabaseInterface db) throws RemoteException {
        super();
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
        logger.info("Trying to create a new timeline for user " + user);
        try {
            LoadManager.createTimeline(user);
            while(!db.isUser(user)) {
                sleep(100);
            }
            return (TimelineInterface)db.getTimeline(user);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
