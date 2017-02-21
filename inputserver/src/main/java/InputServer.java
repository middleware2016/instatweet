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
 * Class that processes the tweets in input and routes them
 *
 * @author Alex Delbono
 */
public class InputServer extends UnicastRemoteObject implements InputServerInterface{

    private boolean stop;
    private Registry reg;
    private JMSConsumer consumer;
    private JMSProducer dispatch;
    private Destination inputDest;
    private Destination dispatchDest;
    private JMSProducer imagehandle;
    private Destination imagehandleDest;
    private DatabaseInterface db;
    private String timeline_jar_path;
    private JMSContext context;

    private MessageListener listener;

    private static Logger logger = Logger.getLogger(InputServer.class.getName());

    /**
     * @param context the JMS Context used to create consumer and producers
     * @param reg the RMI registry
     * @param inputDest input destination for the incoming message queue from the clients (input)
     * @param dispatchDest the destination for the dispatcher queue (output)
     * @param imagehandleDest the destination for the image handler (output)
     * @param db the object implementing the DatabaseInterface
     * @param timeline_jar_path path of the JAR of the Timeline class
     * @throws RemoteException
     */
    protected InputServer(JMSContext context, Registry reg, Destination inputDest,
                          Destination dispatchDest, Destination imagehandleDest,
                          DatabaseInterface db, String timeline_jar_path) throws RemoteException {
        this.context = context;
        this.reg=reg;
        this.inputDest=inputDest;
        this.dispatchDest=dispatchDest;
        this.imagehandleDest=imagehandleDest;
        this.db=db;
        this.timeline_jar_path=timeline_jar_path;
        stop=false;

        this.consumer = context.createConsumer(inputDest);
        this.dispatch = context.createProducer();
        this.imagehandle = context.createProducer();

        this.listener = new InputListener();
        this.consumer.setMessageListener(this.listener);
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
        if(args.length<7) {
            System.out.println("InputServer arguments: connection-factory-name input-queue-name " +
                    "dispatch-destination-name imagehandle-destination-name timeline-jar-path" +
                    "database-rmi-name inputserver-rmi-name " +
                    "[rmi-registry-ip  rmi-registry-port]");
            return;
        }

        try {
            Registry registry;
            if(args.length < 9) {
                logger.info("Using default rmi ip and port");
                registry = LocateRegistry.getRegistry();
            } else
                registry = LocateRegistry.getRegistry(args[7], Integer.parseInt(args[8]));

            Context jndiContext = new InitialContext();

            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(args[0]);
            Destination inputDest = (Destination) jndiContext.lookup(args[1]);
            Destination dispatchDest = (Destination) jndiContext.lookup(args[2]);
            Destination imagehandleDest = (Destination) jndiContext.lookup(args[3]);
            JMSContext context = connectionFactory.createContext();
            DatabaseInterface db = (DatabaseInterface) registry.lookup(args[5]);

            // Construction of the object and binding
            InputServer inputServer = new InputServer(context, registry, inputDest, dispatchDest, imagehandleDest, db, args[4]);
            registry.bind(args[6], inputServer);
            inputServer.start(); // main loop

            // Termination
            registry.unbind(args[6]);
            logger.info("InputServer " + args[6] + " unbound");
            exit(0);

        } catch (RemoteException | AlreadyBoundException | NotBoundException | NamingException e){
            e.printStackTrace();
            logger.info("Exiting the inputserver " + args[6]);
            exit(-1);
        }

    }

    /*
        Class to listen asynchronously to incoming messages.
        TODO: implement
     */
    class InputListener implements MessageListener {

        @Override
        public void onMessage(Message message) {
            ObjectMessage msg = (ObjectMessage)message;
            try {
                Object obj = msg.getObject();
                logger.info("[InputServer] Received a message: " + obj.toString());
            } catch(JMSException e) {
                e.printStackTrace();
            }

        }
    }
}
