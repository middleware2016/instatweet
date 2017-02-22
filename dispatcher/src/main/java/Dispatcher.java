import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.*;
import java.awt.*;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.System.exit;

/**
 * Class that consumes the queue of the tweets enqueued for processing by Timelines and updates the timelines
 * of the users that are interested in the tweets.
 *
 * @author Alex Delbono
 */
public class Dispatcher extends UnicastRemoteObject implements DispatcherInterface {

    private boolean stop;
    private Registry reg;
    private JMSConsumer consumer;
    private JMSContext context;
    private MessageListener listener;
    private DatabaseInterface db;

    private static Logger logger = Logger.getLogger(Dispatcher.class.getName());

    public Dispatcher(JMSContext context, Destination dest, Registry reg, DatabaseInterface db) throws RemoteException {
        this.stop = false;
        this.reg = reg;
        this.consumer = context.createConsumer(dest);
        this.listener = new DispatcherListener();
        this.consumer.setMessageListener(this.listener);
        this.db = db;
    }

    @Override
    public synchronized void stop() throws RemoteException{
        stop = true;
        notify();
    }

    public synchronized void start() {
        while(!stop) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized boolean isStopped() {
        return stop;
    }


    public static void main(String args[]){
        if(args.length<3) {
            System.out.println("Dispatcher arguments: connection-factory-name tweet-queue-name dispatcher-rmi-name database-rmi-name " +
                                "[rmi-registry-ip  rmi-registry-port]");
            return;
        }

        try {
            Registry registry;
            if(args.length < 5) {
                System.out.println("Using default rmi ip and port");
                registry = LocateRegistry.getRegistry();
            } else
                registry = LocateRegistry.getRegistry(args[4], Integer.parseInt(args[5]));

            DatabaseInterface db = (DatabaseInterface) registry.lookup(args[3]);

            Context jndiContext = new InitialContext();
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(args[0]);
            Destination dest = (Destination) jndiContext.lookup(args[1]);
            JMSContext context = connectionFactory.createContext();

            // Construction and binding
            Dispatcher disp = new Dispatcher(context, dest, registry, db);
            registry.bind(args[2], disp);

            disp.start();

            registry.unbind(args[2]);
            System.out.println("Dispatcher " + args[2] + " unbound");
            exit(0);


        } catch (RemoteException | AlreadyBoundException | NotBoundException | NamingException e){
            e.printStackTrace();
            System.out.println("Exiting the dispatcher " + args[2]);
            exit(-1);
        }

    }

    /*
        Class to listen asynchronously to tweets in the queue.
    */
    class DispatcherListener implements MessageListener {

        private static final int THUMB_HEIGHT = 100;
        private static final int THUMB_WIDTH = 100;

        @Override
        public void onMessage(Message message) {
            ObjectMessage msg = (ObjectMessage)message;
            try {
                Tweet tw = (Tweet)msg.getObject();
                handleTweet(tw);
            } catch(JMSException e) {
                e.printStackTrace();
            }

        }

        private void handleTweet(Tweet tw) {
            logger.warning(String.format("[Dispatcher] Received tweet: %s", tw.toString()));

            try {
                Tweet processedTw = processImage(tw);
                informFollowers(processedTw);
            } catch (RemoteException e) {
                logger.severe(e.toString());
            }
        }

        /**
         * Processes the tweet in order to generate the thumbnail, store the full-size image and add them to the tweet.
         * @param tw the tweet to process (will be modified)
         * @throws RemoteException
         */
        private Tweet processImage(Tweet tw) throws RemoteException {

            Image fullImg = tw.getImg();

            if(fullImg!=null) {
                String text = tw.getText();

                //Scaling
                Image thumb = fullImg.getScaledInstance(THUMB_WIDTH, THUMB_HEIGHT, Image.SCALE_DEFAULT);

                //Database insert
                int fullImgID = db.addImage(new ImageIcon(fullImg, text));

                //Create new tw
                Tweet newTw = new Tweet(tw.getPublisherUsername(),
                                        text,
                                        thumb,
                                        fullImgID);

                return newTw;
            }

            return tw;

        }

        /**
         * Posts the tweet on all the followers' timelines.
         * @param tw a tweet complete with thumbnail and link to full image
         * @throws RemoteException
         */
        private void informFollowers(Tweet tw) throws RemoteException {
            List<String> followers = db.getSubscribers(tw.getPublisherUsername());

            for(String f: followers) {
                TimelineUpdateInterface tui = (TimelineUpdateInterface) db.getTimeline(f);
                tui.addTweet(tw);
            }
        }
    }
}
