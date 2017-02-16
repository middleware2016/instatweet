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
 * Class that processes the tweets in input and routes them
 *
 * @author Alex Delbono
 */
public class InputServer extends UnicastRemoteObject implements InputServerInterface{

    private boolean stop;
    private Registry reg;
    private JMSConsumer stream;
    private JMSProducer dispatch;
    private Destination dispatchDest;
    private JMSProducer imagehandle;
    private Destination imagehandleDest;
    private DatabaseInterface db;
    private String timeline_jar_path;

    protected InputServer(Registry reg, JMSConsumer stream,
                          JMSProducer dispatch, Destination dispatchDest,
                          JMSProducer imagehandle, Destination imagehandleDest,
                          DatabaseInterface db, String timeline_jar_path) throws RemoteException {
        this.reg=reg;
        this.stream=stream;
        this.dispatch=dispatch;
        this.dispatchDest=dispatchDest;
        this.imagehandle=imagehandle;
        this.imagehandleDest=imagehandleDest;
        this.db=db;
        this.timeline_jar_path=timeline_jar_path;
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
        //if there is new user request process it
        //if there is following request process it
        //if there is an image forward to imagehandler
        //otherwise send it to dispatcher
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
                System.out.println("Using default rmi ip and port");
                registry = LocateRegistry.getRegistry();
            } else
                registry = LocateRegistry.getRegistry(args[7], Integer.parseInt(args[8]));

            Context jndiContext = new InitialContext();
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(args[0]);
            Destination inputDest = (Destination) jndiContext.lookup(args[1]);
            Destination dispatchDest = (Destination) jndiContext.lookup(args[2]);
            Destination imagehandleDest = (Destination) jndiContext.lookup(args[3]);
            JMSContext context = connectionFactory.createContext();
            JMSConsumer consumer = context.createConsumer(inputDest);
            JMSProducer dispatch = context.createProducer();
            JMSProducer imagehandle = context.createProducer();

            DatabaseInterface db = (DatabaseInterface) registry.lookup(args[5]);

            InputServer inputServer = new InputServer(registry, consumer, dispatch, dispatchDest,
                                                    imagehandle, imagehandleDest, db, args[4]);

            registry.bind(args[6], inputServer);

            while(!inputServer.isStopped()){
                inputServer.processNext();
            }

            registry.unbind(args[6]);
            System.out.println("InputServer " + args[6] + " unbound");


        } catch (RemoteException | AlreadyBoundException | NotBoundException | NamingException e){
            e.printStackTrace();
            System.out.println("Exiting the inputserver " + args[6]);
            exit(-1);
        }

    }
}
