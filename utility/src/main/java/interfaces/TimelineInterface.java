package interfaces;

import payloads.Tweet;

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
    public List<Tweet> getLastTweets(int quantity) throws RemoteException;
    public ImageIcon getFullImage(int imgID) throws RemoteException;

    public void postTweet(Tweet t) throws RemoteException;
    public void addSubscriber(String subscriber) throws RemoteException;
    public void removeSubscriber(String subscriber) throws RemoteException;

}
