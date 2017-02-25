import configurator.Configurator;
import interfaces.DatabaseInterface;
import interfaces.DispatcherInterface;
import interfaces.TimelineInterface;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import static java.lang.System.exit;
import static java.lang.Thread.sleep;

/**
 * Class that initializes the system and manages the scaling
 *
 * @author Alex Delbono
 */
public class LoadManager {
    //JMS names
    private static String connection_factory_name;
    private static String dispatch_destination_name;
    private static String accesspoint_rmi_name;

    //Jar paths
    private static String timeline_jar_path;
    private static String database_jar_path;
    private static String dispatcher_jar_path;

    //RMI ip and port
    private static String rmi_ip_port;
    private static Registry registry;

    //Bases of the class names
    private static String database_rmi_name;
    private static String dispatcher_rmi_name;

    //Containers for classes
    private static List<String> dispatcher_list = new ArrayList<>();
    private static int dispatcher_counter = 0;

    //JMS queue browsers
    private static QueueBrowser dispatch_destination_browser;

    //Scaling parameters
    private static int maxMessPerElem;
    private static int minMessPerElem;

    //Database object
    private static DatabaseInterface db;

    private static Logger logger = Logger.getLogger(LoadManager.class.getName());


    public static void main(String args[]) {

        Configurator config = Configurator.getInstance();

        connection_factory_name = config.get("connection_factory_name");
        dispatch_destination_name = config.get("dispatch_queue_name");

        timeline_jar_path = config.get("timeline_jar_path");
        database_jar_path = config.get("database_jar_path");
        dispatcher_jar_path = config.get("dispatcher_jar_path");

        database_rmi_name = config.get("database_rmi_name");
        dispatcher_rmi_name = config.get("dispatcher_rmi_name");
        accesspoint_rmi_name = config.get("accesspoint_rmi_name");

        maxMessPerElem = Integer.parseInt(config.get("min_messages_per_dispatcher"));
        minMessPerElem = Integer.parseInt(config.get("max_messages_per_dispatcher"));


        try {
            //Initialize RMI
            if (config.get("rmi_ip") == null) {
                logger.fine("Using default rmi ip and port");
                registry = LocateRegistry.getRegistry();
                rmi_ip_port = "";
            } else {
                registry = LocateRegistry.getRegistry(config.get("rmi_ip"), Integer.parseInt(config.get("rmi_port")));
                rmi_ip_port = config.get("rmi_ip") + " " + config.get("rmi_port");
            }

            //JMS initialization
            Context jndiContext = new InitialContext();
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(connection_factory_name);
            JMSContext context = connectionFactory.createContext();

            Queue dispatch_queue = (Queue) jndiContext.lookup(dispatch_destination_name);

            dispatch_destination_browser = context.createBrowser(dispatch_queue);

            //Database creation
            logger.info("Initialize database");
            createDatabase();

            //Create first dispatcher
            logger.info("Initialize first dispatcher");
            createDispatcher();

            // Create AccessPoint
            logger.info("Initialize AccessPoint");
            createAccessPoint();
            waitForRMIObject(accesspoint_rmi_name);

            logger.info("Everything initialized");

            //Loop until administrator wants to quit
            loadManagerLoop();

            closeAll();

            logger.info("Everything closed");

            exit(0);


        } catch (IOException | NamingException | JMSException | AlreadyBoundException | NotBoundException | InterruptedException e) {
            logger.severe("Exiting LoadManager: " + e.toString());
            exit(-1);
        }


    }

    private static void loadManagerLoop() throws JMSException, IOException {

        Thread t = new Thread() {

            public void run() {
                Scanner scan = new Scanner(System.in);
                String s;

                while (true) {
                    System.out.println("Type \"q\" to exit o \"l\" for RMI list");
                    s = scan.nextLine();
                    if (s.compareTo("q") == 0) {
                        break;
                    }
                    if (s.compareTo("l") == 0){
                        try {
                            String[] list = registry.list();
                            for(String elem : list)
                                System.out.println(elem);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        };

        t.start();

        while (t.isAlive()) {
            //Scaling analysis
            LoadManager.scalingAnalysis();
        }

        logger.fine("Ending LoadManager loop.");
    }

    ////////////////////////////////////////////////////////////////////////


    private static void scalingAnalysis() throws JMSException, IOException {
        dispatchQueueScaling();
    }

    private static void dispatchQueueScaling() throws JMSException, IOException {
        System.out.println("Starting dispatch loop.");
        int i = 0;
        Enumeration e = dispatch_destination_browser.getEnumeration();

        System.out.println("Starting counter.");
        i = 0;
        while (e.hasMoreElements()) {
            e.nextElement();
            i++;
        }

        System.out.println("Elements in queue: " + i);

        if (((float) i) / dispatcher_list.size() > maxMessPerElem) {
            createDispatcher();
        } else if (((float) i) / dispatcher_list.size() < minMessPerElem && dispatcher_list.size() > 1) {
            deleteDispatcher();
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }

    }

    ////////////////////////////////////////////////////////////////////////////////

    private static Object waitForRMIObject(String name) {
        Object obj = null;
        while(obj == null) {
            try {
                obj = registry.lookup(name);
            } catch (NotBoundException | RemoteException e) {
                try {
                    sleep(100);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return obj;
    }

    private static void createDatabase() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", database_jar_path);
        pb/*.inheritIO()*/.start();
        db = (DatabaseInterface)waitForRMIObject(database_rmi_name);

    }

    private static void createAccessPoint() throws AlreadyBoundException, RemoteException, NotBoundException, InterruptedException {
        AccessPoint ap = new AccessPoint(db);
        registry.bind(accesspoint_rmi_name, ap);
    }

    private static void createDispatcher() throws IOException {
        String name = dispatcher_rmi_name + "_" + dispatcher_counter;

        logger.info(String.format("Starting %s", name));
        ProcessBuilder pb = new ProcessBuilder("appclient", "-client", dispatcher_jar_path, name);
        pb.inheritIO().start();

        dispatcher_counter++;
        waitForRMIObject(name); // wait for the new dispatcher to come up online
        dispatcher_list.add(name);
    }

    static void createTimeline(String user) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("appclient", "-client", timeline_jar_path, user);
        pb.inheritIO().start();
    }

    //////////////////////////////////////////////////////////////////////////////////////////


    private static void deleteDispatcher() {
        if (dispatcher_list.size() > 0) {
            String name = dispatcher_list.get(0);
            logger.info(String.format("Stopping %s", name));
            try {
                DispatcherInterface d = (DispatcherInterface) registry.lookup(name);
                d.stop();
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
            dispatcher_list.remove(name);
        }
    }

    private static void deleteAccessPoint() {
        try {
            registry.unbind(accesspoint_rmi_name);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    private static void closeAll(){
        deleteAccessPoint();

        while(!dispatcher_list.isEmpty()){
            deleteDispatcher();
        }

        try {
            List<Object> list = db.getTimelinesAsList();

            for(Object o : list)
                ((TimelineInterface) o).deleteTimeline();

            db.stop();

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}