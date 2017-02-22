package interfaces;


import payloads.Tweet;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface to be used from Dispatcher, which directly updates the timeline.
 * Created by pietro on 2017-02-22.
 */
public interface TimelineUpdateInterface extends Remote {
    public void addTweet(Tweet t) throws RemoteException;
}
