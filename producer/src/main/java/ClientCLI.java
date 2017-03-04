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
import java.util.Collections;
import java.util.List;

import static java.lang.System.exit;

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

    @Command(description="Post a new tweet, without any image")
    public String tweet(
            @Param(name="user", description="Username of the sender")
                String user,
            @Param(name="text", description="Text of the tweet")
                String text) {
        producer.tweet(user, text, null);
        return "Tweet sent!";
    }

    @Command(description="Post a new tweet, without any image")
    public String tweetimg(
            @Param(name="user", description="Username of the sender")
                    String user,
            @Param(name="text", description="Text of the tweet")
                    String text,
            @Param(name="path", description="Path of the image for the tweet")
                    String path){
        Image image = null;
        try {
            image = ImageIO.read(new File(path));
            producer.tweet(user, text, image);
        } catch (IOException e) {
            e.printStackTrace();
            return "Tweet not sent";
        }

        return "Tweet sent!";
    }

    @Command(description="Post a new tweet, without any image")
    public String spam(
            @Param(name="user", description="Username of the sender")
                    String user,
            @Param(name="text", description="Text of the tweet")
                    String text,
            @Param(name="quantity", description="Number of tweets that will be sent")
                    int quantity) {
        for(int i = 0; i<quantity; i++)
            producer.tweet(user, text, null);
        return "Spam done!";
    }

    @Command(description="Display a certain number of tweets for the specified user, from the most recent one")
    public String read(
            @Param(name="user", description="Username corresponding to the timeline to show")
                String user,
            @Param(name="quantity", description="Number of tweets to show")
                int quantity) {
        String out = "";
        List<Tweet> tweets = producer.getLastTweets(user, quantity);
        Collections.reverse(tweets); // Display tweets from the most recent one
        out += String.format("Showing %d new tweets for @%s:\n\n", tweets.size(), user);

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
            out += "\n";
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
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    @Command(description="Display 10 tweets for the specified user")
    public String read(
            @Param(name="user", description="Username corresponding to the timeline to show")
            String user) {
        return read(user, 10);
    }

    @Command(description = "Count the number of tweets in the timeline of a user")
    public int count(
            @Param(name="user", description="Username corresponding to the timeline to count")
                    String user) {
        return producer.getNumTweets(user);
    }

    @Command(description="Download the full image from the server")
    public String getfull(
            @Param(name="user", description="Username corresponding to the timeline to show")
                String user,
            @Param(name="imgID", description="ID of the image to download")
                int imgID) {
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
            System.out.println("Producer arguments: accesspoint_rmi_name");
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
