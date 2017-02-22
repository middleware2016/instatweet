import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.ShellFactory;
import payloads.Tweet;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Timestamp;
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
                String follower,
        @Param(name="toFollow", description="Username of the user to subscribe to")
                String toFollow) {
        producer.subscribe(follower, toFollow);
        return follower + " subscribed to " + toFollow;
    }

    @Command(description="Unsubscribe from an user")
    public String unsubscribe(
            @Param(name="user", description="Username of the subscriber")
                    String subscriber,
            @Param(name="toUnfollow", description="Username of the user to unsubscribe from")
                    String toUnfollow) {
        producer.unsubscribe(subscriber, toUnfollow);
        return subscriber + " unsubscribed from " + toUnfollow;
    }

    @Command(description="Post a new tweet")
    public String tweet(
            @Param(name="user", description="Username of the sender")
            String user,
            @Param(name="text", description="Text of the tweet")
            String text) {
        producer.tweet(user, text, new BufferedImage(200,200, TYPE_INT_RGB));
        return "Tweet sent!";
    }

    @Command(description="Display a certain number of tweets for the specified user")
    public String read(String user, int quantity) {
        String out = "";
        List<Tweet> tweets = producer.getLastTweets(user, quantity);
        out += String.format("%d new tweets for @%s:\n", tweets.size(), user);

        for(Tweet t: tweets) {
            out += String.format("@%s:\t%s", t.getPublisherUsername(), t.getText());
            if(t.getImg()!= null){

                String localPath = String.format("tweets_data/%s_%s.jpg", t.getPublisherUsername(), new Timestamp(System.currentTimeMillis()));
                File outputfile = new File(localPath);
                try {
                    outputfile.getParentFile().mkdir();
                    outputfile.createNewFile();
                    ImageIO.write(toBufferedImage(t.getImg()), "jpg", outputfile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                out += String.format("\n\tLocal path to image: %s", localPath);
                if(t.getFullImgID() >= 0)
                    out += String.format("\n\tId of full img: %s", t.getFullImgID());
            }
            out += String.format("\n");
        }
        return out;
    }

    private static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    @Command(description="Display 10 tweets for the specified user")
    public String read(String user) {
        return read(user, 10);
    }

    @Command(description="Download the full image from the server")
    public String getfull(String user, int imgID) {
        String out = "";
        ImageIcon imgIc = producer.getFullImage(user,imgID);
        String localPath = String.format("tweets_data/Full_%s.jpg", new Timestamp(System.currentTimeMillis()));
        File outputfile = new File(localPath);
        if(imgIc.getImage()!= null) {
            out += String.format("Retreived image with id: %s", imgID);
            try {
                outputfile.createNewFile();
                outputfile.getParentFile().mkdir();
                ImageIO.write(toBufferedImage(imgIc.getImage()), "jpg", outputfile);

                out += String.format("\n\tLocal path to image: %s\n", localPath);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            out += String.format("Invalid image ID: %s\n", imgID);

        return out;
    }

    public void runShell() throws IOException {
        ShellFactory.createConsoleShell("TweetCLI", "Instatweet", this)
                .commandLoop();
    }

    public static void main(String args[]){
        if(args.length<1) {
            System.out.println("Producer arguments: api_rmi_name");
            return;
        }

        try {
            // Construction of the object and binding
            Registry reg = LocateRegistry.getRegistry();
            ClientInterface producer = new Producer(reg, args[0]);
            ClientCLI cli = new ClientCLI(producer);
            cli.runShell();

        } catch (IOException e){
            e.printStackTrace();
            exit(-1);
        }

    }
}
