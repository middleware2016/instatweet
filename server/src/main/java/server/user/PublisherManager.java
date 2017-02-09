package server.user;

import javax.jms.*;
import java.util.logging.Logger;

/**
 * Class used to process and route the requests
 * from the user
 *
 * @author Alex Delbono
 */
public class PublisherManager extends Thread{


    private static final Logger logger = Logger.getLogger(PublisherManager.class.getName());
    private JMSConsumer inputRequest;
    private JMSProducer toSubscriber;
    private Destination toSubscriberDest;

    public PublisherManager(ConnectionFactory connectionFactory,
                            Destination inputRequestDest,
                            Destination toSubscriberDest) throws JMSRuntimeException {

        JMSContext context = connectionFactory.createContext();
        inputRequest = context.createConsumer(inputRequestDest);
        toSubscriber = context.createProducer();
        this.toSubscriberDest=toSubscriberDest;

    }

    @Override
    public void run(){

        //TODO analyze messages and route them

    }
}
