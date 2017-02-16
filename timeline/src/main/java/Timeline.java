import javax.swing.*;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

/**
 * Class that manages the timeline of the user
 *
 * @author Alex Delbono
 */
public class Timeline extends UnicastRemoteObject implements TimelineInterface{

    private Registry registry;
    private String username;
    private DatabaseInterface db;

    private List<Tweet> timeline;

    public Timeline(Registry registry, String username, DatabaseInterface db) throws RemoteException {
        this.registry=registry;
        this.username=username;
        this.db=db;

        timeline = new ArrayList<>();

    }

    public synchronized void runTimeline() throws AlreadyBoundException, RemoteException, NotBoundException, InterruptedException {

        registry.bind("user_"+username, this);

        wait();

        registry.unbind("user_"+username);
    }

    @Override
    public synchronized void deleteTimeline() throws RemoteException {
        notify();
    }

    @Override
    public synchronized int getIndexLast() throws RemoteException {
        return timeline.size()-1;
    }

    @Override
    public synchronized List<Tweet> getFrom(int ind, int quantity) throws RemoteException {

        List<Tweet> list = new ArrayList<>();

        for(int i = ind-quantity; i<=ind && i< timeline.size(); i++){
            list.add(i, new Tweet(timeline.get(i)));
        }

        return list;
    }

    @Override
    public ImageIcon getFullImage(int imgID) throws RemoteException {
        ImageIcon temp = db.getImage(imgID);
        ImageIcon img = new ImageIcon(temp.getImage(),temp.getDescription());

        return img;

    }

    @Override
    public synchronized void addTweet(Tweet t) throws RemoteException {
        timeline.add(new Tweet(t));
    }

    public static void main(String args[]){
        if(args.length<2) {
            System.out.println("Timeline arguments: username database-rmi-name " +
                    "[rmi-registry-ip  rmi-registry-port]");
            return;
        }

        try {
            Registry registry;
            if(args.length < 4) {
                System.out.println("Using default rmi ip and port");
                registry = LocateRegistry.getRegistry();
            } else
                registry = LocateRegistry.getRegistry(args[2], Integer.parseInt(args[3]));

            DatabaseInterface db = (DatabaseInterface) registry.lookup(args[1]);

            Timeline timeline = new Timeline(registry, args[0], db);

            timeline.runTimeline();

            System.out.println("Timeline " + args[1] + " unbound");


        } catch (RemoteException | AlreadyBoundException | NotBoundException | InterruptedException e){
            e.printStackTrace();
            System.out.println("Exiting the timeline " + args[1]);
            exit(-1);
        }

    }
}
