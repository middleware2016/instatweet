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


    private String publisherUsername;
    private int publisherID;
    private ImageIcon content;

    //When img is a full image this is invalid (-1)
    //When img is a thumbnail this is the id of the full image for the lookup in the database
    private int fullImgID;

    public Tweet(){}

    public Tweet(Tweet t){
        publisherUsername = t.publisherUsername;
        publisherID=t.publisherID;
        content = new ImageIcon(t.content.getImage(), t.content.getDescription());

    }

    public String getPublisherUsername() {
        return publisherUsername;
    }

    public void setPublisherUsername(String publisherUsername) {
        this.publisherUsername = publisherUsername;
    }

    public int getPublisherID() {
        return publisherID;
    }

    public void setPublisherID(int publisherID) {
        this.publisherID = publisherID;
    }

    public Image getImg() {
        return content.getImage();
    }

    public void setImg(Image img) {
        content.setImage(img);
    }

    public String getText() {
        return content.getDescription();
    }

    public void setText(String text) {
        content.setDescription(text);
    }

    public int getFullImgID() {
        return fullImgID;
    }

    public void setFullImgID(int fullImgID) {
        this.fullImgID = fullImgID;
    }
}
