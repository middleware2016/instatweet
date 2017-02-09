package server.user;

import javax.jms.*;
import java.util.logging.Logger;

/**
 * Class that manages single subscription
 * to the topic of another user
 *
 * @author Alex Delbono
 */
public class UserSubscription {

    /**
     * MessageListener that send the message published
     * by the followed user to the subscriber timeline
     */
    private class TimelineListener implements MessageListener {

        @Override
        public void onMessage(Message message) {

            producer.send(subscriberTimeline, message);

        }
    }

    private static final Logger logger = Logger.getLogger(UserSubscription.class.getName());
    private JMSConsumer consumer;
    private JMSProducer producer;
    private Destination subscriberTimeline;

    public UserSubscription(ConnectionFactory connectionFactory,
                            Destination subscriberTimeline,
                            Destination subscriptionDest) throws JMSRuntimeException {

        JMSContext context = connectionFactory.createContext();
        consumer = context.createConsumer(subscriptionDest);
        producer = context.createProducer();
        this.subscriberTimeline=subscriberTimeline;

        consumer.setMessageListener(new TimelineListener());

    }
}
