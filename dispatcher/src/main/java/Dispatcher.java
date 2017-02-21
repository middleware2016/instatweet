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
 * Class that consumes the queue of the tweets already processed and updates the timeline
 * of the users that are interested in the tweets
 *
 * @author Alex Delbono
 */
public class Dispatcher extends UnicastRemoteObject implements DispatcherInterface {

    private boolean stop;
    private Registry reg;
    private JMSConsumer consumer;
    private JMSContext context;
    private MessageListener listener;
    private static Logger logger = Logger.getLogger(Dispatcher.class.getName());

    public Dispatcher(JMSContext context, Destination dest, Registry reg) throws RemoteException {
        this.stop = false;
        this.reg = reg;
        this.consumer = context.createConsumer(dest);
        this.listener = new DispatcherListener();
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
        if(args.length<3) {
            System.out.println("Dispatcher arguments: connection-factory-name tweet-queue-name dispatcher-rmi-name " +
                                "[rmi-registry-ip  rmi-registry-port]");
            return;
        }

        try {
            Registry registry;
            if(args.length < 5) {
                System.out.println("Using default rmi ip and port");
                registry = LocateRegistry.getRegistry();
            } else
                registry = LocateRegistry.getRegistry(args[3], Integer.parseInt(args[4]));

            Context jndiContext = new InitialContext();
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(args[0]);
            Destination dest = (Destination) jndiContext.lookup(args[1]);
            JMSContext context = connectionFactory.createContext();

            // Construction and binding
            Dispatcher disp = new Dispatcher(context, dest, registry);
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
        Class to listen asynchronously to incoming messages.
        TODO: implement
    */
    class DispatcherListener implements MessageListener {

        @Override
        public void onMessage(Message message) {
            ObjectMessage msg = (ObjectMessage)message;
            try {
                Object obj = msg.getObject();
                logger.info("[Dispatcher] Received a message: " + obj.toString());
            } catch(JMSException e) {
                e.printStackTrace();
            }

        }
    }
}
