package example.producer;

import javax.annotation.Resource;
import javax.jms.*;


/**
 * Created by alex on 16/01/2017.
 */
public class Producer {
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
