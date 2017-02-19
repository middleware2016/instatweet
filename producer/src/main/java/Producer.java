import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import static java.lang.System.exit;

public class Producer {
    private JMSContext context;
    private Destination inputDestination;
    private JMSProducer producer;

    public Producer(JMSContext context, Destination inputDest) {
        this.context = context;
        this.inputDestination = inputDest;

        this.producer = context.createProducer();
    }

    public void sendTextTweet(String txt) {
        Tweet t = new Tweet("pietrodn", 2, null, 0);
        this.producer.send(inputDestination, t);
        System.out.println("Sent a tweet: " + t.toString());
    }

    public static void main(String args[]){
        if(args.length<3) {
            System.out.println("Producer arguments: connection-factory-name input-queue-name " +
                    " timeline-jar-path");
            return;
        }

        try {
            Context jndiContext = new InitialContext();

            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(args[0]);
            Destination inputDest = (Destination) jndiContext.lookup(args[1]);
            JMSContext context = connectionFactory.createContext();

            // Construction of the object and binding
            Producer prod = new Producer(context, inputDest);

            prod.sendTextTweet("ciaone");
            prod.sendTextTweet("lol");

        } catch (NamingException e){
            e.printStackTrace();
            exit(-1);
        }

    }
}
