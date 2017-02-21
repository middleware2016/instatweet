import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

import static java.lang.System.exit;

/**
 * Class that processes the tweets with images and creates thumbnails
 *
 * @author Alex Delbono
 */
public class ImageHandler extends UnicastRemoteObject implements ImageHandlerInterface{

    private boolean stop;
    private Registry reg;
    private JMSConsumer consumer;
    private JMSProducer output;
    private Destination outputDest, inputDest;
    private DatabaseInterface db;
    private JMSContext context;
    private MessageListener listener;

    private static Logger logger = Logger.getLogger(ImageHandler.class.getName());

    protected ImageHandler(JMSContext context, Destination inputDest, Destination outputDest, Registry reg, DatabaseInterface db) throws RemoteException {
        this.stop = false;
        this.reg=reg;
        this.db=db;
        this.inputDest = inputDest;
        this.outputDest=outputDest;
        this.context = context;

        this.consumer = context.createConsumer(inputDest);
        this.output = context.createProducer();

        this.listener = new ImgListener();
        consumer.setMessageListener(this.listener);
    }

    @Override
    public synchronized void stop() throws RemoteException{
        stop = true;
        notify();
    }

    public synchronized boolean isStopped() {
        return stop;
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


    public static void main(String args[]){
        if(args.length<5) {
            System.out.println("ImageHandler arguments: connection-factory-name image-tweets-queue-name " +
                    "output-destination-name database-rmi-name imagehandler-rmi-name " +
                    "[rmi-registry-ip  rmi-registry-port]");
            return;
        }

        try {
            Registry registry;
            if(args.length < 7) {
                System.out.println("Using default rmi ip and port");
                registry = LocateRegistry.getRegistry();
            } else
                registry = LocateRegistry.getRegistry(args[5], Integer.parseInt(args[6]));

            Context jndiContext = new InitialContext();
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(args[0]);
            Destination inputDest = (Destination) jndiContext.lookup(args[1]);
            Destination outputDest = (Destination) jndiContext.lookup(args[2]);
            JMSContext context = connectionFactory.createContext();
            DatabaseInterface db = (DatabaseInterface) registry.lookup(args[3]);

            // Initialization and binding
            ImageHandler imgHandler = new ImageHandler(context, inputDest, outputDest, registry, db);
            registry.bind(args[4], imgHandler);

            imgHandler.start();

            registry.unbind(args[4]);
            System.out.println("ImageHandler " + args[4] + " unbound");
            exit(0);

        } catch (RemoteException | AlreadyBoundException | NotBoundException | NamingException e){
            e.printStackTrace();
            System.out.println("Exiting the imagehandler " + args[4]);
            exit(-1);
        }

    }

    /*
        Class to listen asynchronously to incoming messages.
        TODO: implement
    */
    class ImgListener implements MessageListener {

        @Override
        public void onMessage(Message message) {
            ObjectMessage msg = (ObjectMessage)message;
            try {
                Object obj = msg.getObject();
                logger.info("[ImageHandler] Received a message: " + obj.toString());
            } catch(JMSException e) {
                e.printStackTrace();
            }

        }
    }
}
