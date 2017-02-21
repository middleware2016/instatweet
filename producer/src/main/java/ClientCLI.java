import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.ShellFactory;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.management.openmbean.KeyAlreadyExistsException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.System.exit;

/**
 * Created by pietro on 2017-02-19.
 */
public class ClientCLI {

    private ClientInterface producer;

    public ClientCLI(ClientInterface producer) {
        this.producer = producer;
    }

    @Command(description="Subscribe to an user")
    public String subscribe(
        @Param(name="user", description="Username of the subscriber")
                String subscriber,
        @Param(name="text", description="Username of the user to subscribe to")
                String user) {
        producer.subscribe(subscriber, user);
        return subscriber + " subscribed to " + user;
    }

    @Command(description="Unsubscribe from an user")
    public String unsubscribe(
            @Param(name="user", description="Username of the subscriber")
                    String subscriber,
            @Param(name="text", description="Username of the user to unsubscribe from")
                    String user) {
        producer.unsubscribe(subscriber, user);
        return subscriber + " unsubscribed from " + user;
    }

    @Command(description="Post a new tweet")
    public String tweet(
            @Param(name="user", description="Username of the sender")
            String user,
            @Param(name="text", description="Text of the tweet")
            String text) {
        producer.tweet(user, text, new BufferedImage(100,100, TYPE_INT_RGB));
        return "Tweet sent!";
    }

    @Command(description="Display the list of tweets")
    public String read() {
        String out = "";
        List<Tweet> tweets = producer.getTimeline();
        out += String.format("%d new tweets:\n", tweets.size());
        for(Tweet t: tweets) {
            out += t.toString() + "\n";
        }
        return out;
    }

    @Command(description="Create a new user with the given name", name="new_user", abbrev="n")
    public String newUser(String name) {
        try {
            producer.newUser(name);
            return "User created.";
        } catch(KeyAlreadyExistsException e) {
            return String.format("User @%s already exists!", name);
        }
    }

    public void runShell() throws IOException {
        ShellFactory.createConsoleShell("TweetCLI", "Instatweet", this)
                .commandLoop();
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
            ClientInterface producer = new Producer(context, inputDest);
            ClientCLI cli = new ClientCLI(producer);
            cli.runShell();


        } catch (NamingException | IOException e){
            e.printStackTrace();
            exit(-1);
        }

    }
}
