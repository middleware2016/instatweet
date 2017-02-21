import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.management.openmbean.KeyAlreadyExistsException;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Producer implements ClientInterface {
    private JMSContext context;
    private Destination inputDestination;
    private JMSProducer producer;

    private static Logger logger = Logger.getLogger(Producer.class.getName());

    public Producer(JMSContext context, Destination inputDest) {
        this.context = context;
        this.inputDestination = inputDest;

        this.producer = context.createProducer();
    }

    @Override
    public void tweet(String user, String text, Image image) {
        Tweet t = new Tweet(user, text, image);
        this.producer.send(inputDestination, t);
        logger.info("Sent a tweet: " + t.toString());
    }

    @Override
    public void subscribe(String subscriber, String user) {
        NewFollower nf = new NewFollower(subscriber, user, false);
        this.producer.send(inputDestination, nf);
        logger.info("A subscription was sent.");
    }

    @Override
    public void unsubscribe(String subscriber, String user) {
        NewFollower nf = new NewFollower(subscriber, user, true);
        this.producer.send(inputDestination, nf);
        logger.info("A subscription removal was sent.");
    }

    @Override
    public void newUser(String name) throws KeyAlreadyExistsException {
        // TODO: implement, throw exception if the user already exists.
        NewUser nu = new NewUser(name);
        this.producer.send(inputDestination, nu);
        logger.info(String.format("(Not implemented) User @%s should be created.", name));
    }

    @Override
    public List<Tweet> getTimeline() {
        return new ArrayList<>();
    }
}
