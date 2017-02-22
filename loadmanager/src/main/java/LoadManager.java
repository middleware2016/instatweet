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

/**
 * Class that initializes the system and manages the scaling
 *
 * @author Alex Delbono
 */
public class LoadManager {

    //JMS names
    private static String connection_factory_name;
    private static String input_queue_name;
    private static String dispatch_destination_name;
    private static String imagehandle_destination_name;

    //Jar paths
    private static String timeline_jar_path;
    private static String input_server_jar_path;
    private static String database_jar_path;
    private static String dispatcher_jar_path;
    private static String image_handler_jar_path;

    //RMI ip and port
    private static String rmi_ip_port;
    private static Registry registry;

    //Bases of the class names
    private static String database_rmi_name = "instatweet_database";
    private static String input_server_rmi_name = "instatweet_inputserver";
    private static String image_handler_rmi_name = "instatweet_imagehandler";
    private static String dispatcher_rmi_name = "instatweet_dispatcher";

    //Containers for classes
    private static List<String> input_server_list = new ArrayList<>();
    private static int input_server_counter = 0;
    private static List<String> image_handler_list = new ArrayList<>();
    private static int image_handler_counter = 0;
    private static List<String> dispatcher_list = new ArrayList<>();
    private static int dispatcher_counter = 0;

    //JMS queue browsers
    private static QueueBrowser input_queue_browser;
    private static QueueBrowser dispatch_destination_browser;
    private static QueueBrowser imagehandle_destination_browser;

    //Scaling parameters
    private static int maxMessPerElem = 50;
    private static int minMessPerElem = 5;

    private static Logger logger = Logger.getLogger(LoadManager.class.getName());


    public static void main(String args[]) {
        if (args.length < 9) {
            System.out.println("LoadManager arguments: connection-factory-name input-queue-name " +
                    "dispatch-destination-name imagehandle-destination-name timeline-jar-path" +
                    "input-server-jar-path database-jar-path dispatcher-jar-path image-handler-jar-path " +
                    "[rmi-registry-ip  rmi-registry-port]");
            return;
        }

        connection_factory_name = args[0];
        input_queue_name = args[1];
        dispatch_destination_name = args[2];
        imagehandle_destination_name = args[3];

        timeline_jar_path = args[4];
        input_server_jar_path = args[5];
        database_jar_path = args[6];
        dispatcher_jar_path = args[7];
        image_handler_jar_path = args[8];


        try {
            //Initialize RMI
            if (args.length < 11) {
                System.out.println("Using default rmi ip and port");
                registry = LocateRegistry.getRegistry();
                rmi_ip_port = "";
            } else {
                registry = LocateRegistry.getRegistry(args[9], Integer.parseInt(args[10]));
                rmi_ip_port = args[9] + " " + args[10];
            }

            //JMS initialization
            Context jndiContext = new InitialContext();
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(connection_factory_name);
            JMSContext context = connectionFactory.createContext();

            Queue input_queue = (Queue) jndiContext.lookup(input_queue_name);
            Queue dispatch_queue = (Queue) jndiContext.lookup(dispatch_destination_name);
            Queue imagehandle_queue = (Queue) jndiContext.lookup(imagehandle_destination_name);

            input_queue_browser = context.createBrowser(input_queue);
            dispatch_destination_browser = context.createBrowser(dispatch_queue);
            imagehandle_destination_browser = context.createBrowser(imagehandle_queue);


            //Database creation
            logger.info("Initialize database");
            Runtime.getRuntime().exec("java -jar " + database_jar_path + " " + database_rmi_name + " " + rmi_ip_port);

            //Create first input server
            logger.info("Initialize first input server");
            createInputServer();

            //Create first image handler
            logger.info("Initialize first image handler");
            createImageHandler();

            //Create first dispatcher
            logger.info("Initialize first dispatcher");
            createDispatcher();

            // Create AccessPoint
            logger.info("Initialize AccessPoint");
            createAccessPoint();

            logger.info("Everything initialized");

            //Loop until administrator wants to quit
            loadManagerLoop();

            closeAll();

            logger.info("Everything closed");

            exit(0);


        } catch (IOException | NamingException | JMSException | AlreadyBoundException | NotBoundException e) {
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
        inputQueueScaling();
        dispatchQueueScaling();
        imagehandleQueueScaling();
    }

    private static void inputQueueScaling() throws JMSException, IOException {

        int i=0;
        Enumeration e = input_queue_browser.getEnumeration();

        while(e.hasMoreElements())
            i++;

        if(((float) i)/input_server_list.size() > maxMessPerElem)
            createInputServer();
        else if (((float) i)/input_server_list.size() < minMessPerElem && input_server_list.size()>1)
            deleteInputServer();

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

    private static void imagehandleQueueScaling() throws JMSException, IOException {

        int i=0;
        Enumeration e = imagehandle_destination_browser.getEnumeration();

        while(e.hasMoreElements())
            i++;

        if(((float) i)/image_handler_list.size() > maxMessPerElem)
            createImageHandler();
        else if (((float) i)/image_handler_list.size() < minMessPerElem && image_handler_list.size()>1)
            deleteImageHandler();
    }

    ////////////////////////////////////////////////////////////////////////////////

    private static void createAccessPoint() throws AlreadyBoundException, RemoteException, NotBoundException {
        DatabaseInterface db = (DatabaseInterface) registry.lookup(database_rmi_name);
        AccessPoint ap = new AccessPoint(db);
        registry.bind("instatweet_accesspoint", ap);
    }

    private static void createInputServer() throws IOException {

        String name = input_server_rmi_name + "_" + input_server_counter;

        ProcessBuilder pb =
                new ProcessBuilder("appclient", "-client", input_server_jar_path,
                connection_factory_name, input_queue_name, dispatch_destination_name,
                imagehandle_destination_name, timeline_jar_path, database_rmi_name, name, rmi_ip_port);
        pb.inheritIO().start();

        input_server_list.add(name);

        input_server_counter++;
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

    private static void createImageHandler() throws IOException {
        String name = image_handler_rmi_name + "_" + image_handler_counter;

        ProcessBuilder pb =
                new ProcessBuilder("appclient", "-client",
                        image_handler_jar_path, connection_factory_name, imagehandle_destination_name,
                        dispatch_destination_name, database_rmi_name, name, rmi_ip_port);
        pb.inheritIO().start();

        image_handler_list.add(name);

        image_handler_counter++;
    }

    static void createTimeline(String user) throws IOException {
        ProcessBuilder pb =
                new ProcessBuilder("appclient", "-client",
                        timeline_jar_path, user, database_rmi_name, connection_factory_name,
                        dispatch_destination_name, rmi_ip_port);
        pb.inheritIO().start();
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    private static void deleteInputServer() {
        if(input_server_list.size()>0){
            String name = input_server_list.get(0);
            try {
                InputServerInterface is = (InputServerInterface) registry.lookup(name);
                is.stop();
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
            input_server_list.remove(name);
        }
    }

    private static void deleteImageHandler() {
        if(image_handler_list.size()>0){
            String name = image_handler_list.get(0);
            try {
                ImageHandlerInterface ih = (ImageHandlerInterface) registry.lookup(name);
                ih.stop();
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
            image_handler_list.remove(name);
        }
    }

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

    private static void closeAll(){
        while(!input_server_list.isEmpty())
            deleteInputServer();

        while(!image_handler_list.isEmpty())
            deleteImageHandler();

        while(!dispatcher_list.isEmpty()){
            deleteDispatcher();
        }

        try {
            DatabaseInterface db = (DatabaseInterface) registry.lookup(database_rmi_name);
            List<Object> list = db.getTimelinesAsList();

            for(Object o : list)
                ((TimelineInterface) o).deleteTimeline();

            db.stopDatabase();

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

}