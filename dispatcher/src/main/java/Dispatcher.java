import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
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
 * Class that consumes the queue of the tweets already processed and updates the timeline
 * of the users that are interested in the tweets
 *
 * @author Alex Delbono
 */
public class Dispatcher extends UnicastRemoteObject implements DispatcherInterface {

    private boolean stop;
    private Registry reg;
    private JMSConsumer stream;

    public Dispatcher(Registry reg, JMSConsumer stream) throws RemoteException {
        this.reg = reg;
        this.stream = stream;
        stop = false;
    }

    @Override
    public synchronized void stop() throws RemoteException{
        stop = true;
    }

    public synchronized boolean isStopped() {
        return stop;
    }

    public void processNext(){

        stream.receive(500);
        //TODO do stuff
        //wait new message
        //take userID from message
        //lookup the followers of that user
        //for every follower using RMI find the timeline and update it
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
            JMSConsumer consumer = context.createConsumer(dest);

            Dispatcher disp = new Dispatcher(registry, consumer);

            registry.bind(args[2], disp);

            while(!disp.isStopped()){
                disp.processNext();
            }

            registry.unbind(args[2]);
            System.out.println("Dispatcher " + args[2] + " unbound");
            exit(0);


        } catch (RemoteException | AlreadyBoundException | NotBoundException | NamingException e){
            e.printStackTrace();
            System.out.println("Exiting the dispatcher " + args[2]);
            exit(-1);
        }

    }
}
