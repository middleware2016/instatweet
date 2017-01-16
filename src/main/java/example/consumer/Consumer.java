package example.consumer;

import javax.annotation.Resource;
import javax.jms.*;

/**
 * Created by alex on 16/01/2017.
 */
public class Consumer {
    @Resource(lookup = "jms/ConnectionFactory")
    private static ConnectionFactory connectionFactory;
    @Resource(lookup = "jms/Queue")private static Queue queue;
    @Resource(lookup = "jms/Topic")private static Topic topic;

    public static void main (String [] args) throws JMSException {

        final int NUM_MSGS = 1;

        //Must be "queue" or "topic"
        String destType = "queue";

        Destination dest = null;

        if (destType.equals("queue")) {
            dest = (Destination) queue;
        } else {
            dest = (Destination) topic;
        }


        Connection connection = connectionFactory.createConnection();
        Session session = connection.createSession(
                false,
                Session.AUTO_ACKNOWLEDGE);

        MessageConsumer consumer = session.createConsumer(dest);

        connection.start();

        while (true) {
            Message m = consumer.receive(1);
            if (m != null) {
                if (m instanceof TextMessage) {
                    TextMessage message = (TextMessage) m;
                    System.out.println("Reading message: " + message.getText());
                } else {
                    break;
                }
            }
        }

        if (connection != null) {
            connection.close();
        }

    }
}
