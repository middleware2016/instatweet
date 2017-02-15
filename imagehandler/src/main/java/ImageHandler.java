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

import static java.lang.System.exit;

/**
 * Class that processes the tweets with images and creates thumbnails
 *
 * @author Alex Delbono
 */
public class ImageHandler extends UnicastRemoteObject implements ImageHandlerInterface{

    private boolean stop;
    private Registry reg;
    private JMSConsumer stream;
    private JMSProducer output;
    private Destination outputDest;
    private DatabaseInterface db;

    protected ImageHandler(Registry reg, JMSConsumer stream, JMSProducer output, Destination outputDest, DatabaseInterface db) throws RemoteException {
        this.reg=reg;
        this.stream=stream;
        this.output=output;
        this.outputDest=outputDest;
        this.db=db;
        stop=false;
    }

    @Override
    public synchronized void stop() throws RemoteException{
        stop = true;
    }

    public synchronized boolean isStopped() {
        return stop;
    }

    public void processNext(){

        System.out.println(stream.receive());
        //TODO do stuff
        //wait new message
        //take image from tweet
        //create thumbnail
        //add full image to database
        //create new version of the tweet
        //send new tweet to publishing queue
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
            JMSConsumer consumer = context.createConsumer(inputDest);
            JMSProducer producer = context.createProducer();

            DatabaseInterface db = (DatabaseInterface) registry.lookup(args[3]);

            ImageHandler imgHandler = new ImageHandler(registry, consumer, producer, outputDest, db);

            registry.bind(args[4], imgHandler);

            while(!imgHandler.isStopped()){
                imgHandler.processNext();
            }

            registry.unbind(args[4]);
            System.out.println("ImageHandler " + args[4] + " unbound");


        } catch (RemoteException | AlreadyBoundException | NotBoundException | NamingException e){
            e.printStackTrace();
            System.out.println("Exiting the imagehandler " + args[4]);
            exit(-1);
        }

    }
}
