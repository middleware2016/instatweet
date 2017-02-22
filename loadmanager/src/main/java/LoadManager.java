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

    public static final String accesspoint_rmi_name = "instatweet_accesspoint";
    //JMS names
    private static String connection_factory_name;
    private static String dispatch_destination_name;

    //Jar paths
    private static String timeline_jar_path;
    private static String database_jar_path;
    private static String dispatcher_jar_path;

    //RMI ip and port
    private static String rmi_ip_port;
    private static Registry registry;

    //Bases of the class names
    private static String database_rmi_name = "instatweet_database";
    private static String dispatcher_rmi_name = "instatweet_dispatcher";

    //Containers for classes
    private static List<String> dispatcher_list = new ArrayList<>();
    private static int dispatcher_counter = 0;

    //JMS queue browsers
    private static QueueBrowser dispatch_destination_browser;

    //Scaling parameters
    private static int maxMessPerElem = 50;
    private static int minMessPerElem = 5;

    //Database object
    private static DatabaseInterface db;

    private static Logger logger = Logger.getLogger(LoadManager.class.getName());


    public static void main(String args[]) {
        if (args.length < 5) {
            System.out.println("LoadManager arguments: connection-factory-name  " +
                    "dispatch-destination-name timeline-jar-path" +
                    "database-jar-path dispatcher-jar-path " +
                    "[rmi-registry-ip  rmi-registry-port]");
            return;
        }

        connection_factory_name = args[0];
        dispatch_destination_name = args[1];

        timeline_jar_path = args[2];
        database_jar_path = args[3];
        dispatcher_jar_path = args[4];


        try {
            //Initialize RMI
            if (args.length < 6) {
                System.out.println("Using default rmi ip and port");
                registry = LocateRegistry.getRegistry();
                rmi_ip_port = "";
            } else {
                registry = LocateRegistry.getRegistry(args[5], Integer.parseInt(args[6]));
                rmi_ip_port = args[5] + " " + args[6];
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
            e.printStackTrace();
            logger.info("Exiting LoadManager");
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

        t.run();

        while (t.isAlive()) {
            //Scaling analysis
            LoadManager.scalingAnalysis();
        }

        logger.info("Closing everything...");
    }

    ////////////////////////////////////////////////////////////////////////


    private static void scalingAnalysis() throws JMSException, IOException {
        dispatchQueueScaling();
    }

    private static void dispatchQueueScaling() throws JMSException, IOException {

        int i=0;
        Enumeration e = dispatch_destination_browser.getEnumeration();

        while(e.hasMoreElements())
            i++;

        if(((float) i)/dispatcher_list.size() > maxMessPerElem)
            createDispatcher();
        else if(((float) i)/dispatcher_list.size() < minMessPerElem && dispatcher_list.size()>1)
            deleteDispatcher();
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
        ProcessBuilder pb = new ProcessBuilder(
                "java", "-jar", database_jar_path, database_rmi_name, rmi_ip_port);
        pb.inheritIO().start();
        db = (DatabaseInterface)waitForRMIObject(database_rmi_name);

    }

    private static void createAccessPoint() throws AlreadyBoundException, RemoteException, NotBoundException, InterruptedException {
        AccessPoint ap = new AccessPoint(db);
        registry.bind(accesspoint_rmi_name, ap);
    }

    private static void createDispatcher() throws IOException {
        String name = dispatcher_rmi_name + "_" + dispatcher_counter;

        ProcessBuilder pb =
                new ProcessBuilder("appclient", "-client",
                        dispatcher_jar_path, connection_factory_name, dispatch_destination_name,
                        name, database_rmi_name, rmi_ip_port);
        pb.inheritIO().start();

        dispatcher_list.add(name);

        dispatcher_counter ++;
    }

    static void createTimeline(String user) throws IOException {
        ProcessBuilder pb =
                new ProcessBuilder("appclient", "-client",
                        timeline_jar_path, user, database_rmi_name, connection_factory_name,
                        dispatch_destination_name, rmi_ip_port);
        pb.inheritIO().start();
    }

    //////////////////////////////////////////////////////////////////////////////////////////


    private static void deleteDispatcher() {
        if(dispatcher_list.size()>0){
            String name = dispatcher_list.get(0);
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