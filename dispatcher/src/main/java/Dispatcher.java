import configurator.Configurator;
import interfaces.DatabaseInterface;
import interfaces.DispatcherInterface;
import interfaces.TimelineUpdateInterface;
import payloads.Tweet;

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
        Configurator config = Configurator.getInstance();
        if(args.length < 1) {
            System.out.println("Dispatcher arguments: dispatcher-rmi-name");
            return;
        }
        // Each dispatcher should have an unique RMI name.
        String dispatcher_rmi_name = args[0];
        try {

            Registry registry;
            //Initialize RMI
            if (config.get("rmi_ip") == null) {
                logger.fine("Using default rmi ip and port");
                registry = LocateRegistry.getRegistry();
            } else {
                registry = LocateRegistry.getRegistry(config.get("rmi_ip"), Integer.parseInt(config.get("rmi_port")));
            }

            DatabaseInterface db = (DatabaseInterface) registry.lookup(config.get("database_rmi_name"));

            Context jndiContext = new InitialContext();
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(config.get("connection_factory_name"));
            Destination dest = (Destination) jndiContext.lookup(config.get("dispatch_queue_name"));
            JMSContext context = connectionFactory.createContext();

            // Construction and binding
            Dispatcher disp = new Dispatcher(context, dest, registry, db);
            registry.bind(dispatcher_rmi_name, disp);

            disp.start();

            registry.unbind(dispatcher_rmi_name);
            logger.fine("Dispatcher " + dispatcher_rmi_name + " unbound");
            exit(0);

        } catch (RemoteException | AlreadyBoundException | NotBoundException | NamingException e){
            logger.severe("Exiting the dispatcher " + dispatcher_rmi_name + " : " + e.toString());
            exit(-1);
        }

    }

    /*
        Class to listen asynchronously to tweets in the queue.
    */
    class DispatcherListener implements MessageListener {

        private static final int THUMB_HEIGHT = 100;
        private static final int THUMB_WIDTH = 100;
        private int counter = 0;

        @Override
        public void onMessage(Message message) {
            ObjectMessage msg = (ObjectMessage)message;
            try {
                counter++;
                logger.fine(String.format("[DispatcherListener] Tweet %d: begin processing", counter));
                Tweet tw = (Tweet)msg.getObject();
                handleTweet(tw);
                logger.fine(String.format("[DispatcherListener] Tweet %d: end processing", counter));
            } catch(JMSException e) {
                e.printStackTrace();
            }

        }

        private void handleTweet(Tweet tw) {
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
                int fullImgID = db.addImage(new ImageIcon(fullImg));

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

            for (String f : followers) {
                TimelineUpdateInterface tui = (TimelineUpdateInterface) db.getTimeline(f);
                tui.addTweet(tw);
            }
        }
    }
}
