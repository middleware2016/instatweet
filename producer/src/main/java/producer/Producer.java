package producer;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;


/**
 * Created by alex on 16/01/2017.
 */
public class Producer {
    @Resource(mappedName = "jms/DurableConnectionFactory")
    private static ConnectionFactory connectionFactory;
    @Resource(mappedName = "jms/MyQueue")
    private static Queue queue;

    public static void main(String [] args) throws JMSException {

        final int NUM_MSGS = 1;


        Destination dest = (Destination) queue;




        Connection connection = connectionFactory.createConnection();
        Session session = connection.createSession(
                false,
                Session.AUTO_ACKNOWLEDGE);

        MessageProducer producer = session.createProducer(dest);
        TextMessage message = session.createTextMessage();

        for (int i = 0; i < NUM_MSGS; i++) {
            message.setText("This is message " + (i + 1) + " from producer");
            System.out.println("Sending message: " + message.getText());
            producer.send(message);
        }

        //Sends an empty control message to indicate the end of the message stream
        //Sending an empty message of no specified type is a convenient way
        //to indicate to the consumer that the final message has arrived.
        producer.send(session.createMessage());

        if (connection != null) {
            connection.close();
        }
    }
}
