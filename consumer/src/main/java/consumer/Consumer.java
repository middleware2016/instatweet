package consumer;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

/**
 * Created by alex on 16/01/2017.
 */
public class Consumer {
    @Resource(mappedName = "jms/DurableConnectionFactory")
    private static ConnectionFactory connectionFactory;
    @Resource(mappedName = "jms/MyQueue")
    private static Queue queue;

    public static void main (String [] args) throws JMSException {

        final int NUM_MSGS = 1;

        Destination dest = (Destination) queue;


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
