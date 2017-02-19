import javax.swing.*;
import java.awt.*;
import java.io.Serializable;


/**
 * Object that will be included in the ObjectMessage that carries
 * the information about the tweet
 *
 * @auth Alex Delbono
 */
public class Tweet implements Serializable {


    private final String publisherUsername;
    private final ImageIcon content;

    //When img is a full image this is invalid (-1)
    //When img is a thumbnail this is the id of the full image for the lookup in the database
    private final int fullImgID;

    public Tweet(String publisherUsername, String tweet ,Image img, int fullImgID) {
        this.publisherUsername = publisherUsername;
        this.content = new ImageIcon(img, tweet);
        this.fullImgID = fullImgID;
    }

    public Tweet(String publisherUsername, String tweet ,Image img) {
        this.publisherUsername = publisherUsername;
        this.content = new ImageIcon(img, tweet);
        this.fullImgID = -1;
    }

    public Tweet(Tweet t){
        publisherUsername = t.publisherUsername;
        content = new ImageIcon(t.content.getImage(), t.content.getDescription());
        fullImgID=t.fullImgID;

    }

    public String getPublisherUsername() {
        return publisherUsername;
    }

    public Image getImg() {
        return content.getImage();
    }

    public String getText() {
        return content.getDescription();
    }

    public int getFullImgID() {
        return fullImgID;
    }
}
