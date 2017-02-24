package payloads;

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
    private final String text;
    private final ImageIcon image;

    //When img is a full image this is invalid (-1)
    //When img is a thumbnail this is the id of the full image for the lookup in the database
    private final int fullImgID;


    public Tweet(String publisherUsername, String text, Image img) {
        this.publisherUsername = publisherUsername;
        this.image = (img==null ? null : new ImageIcon(img));
        this.text = text;
        this.fullImgID = -1;
    }

    public Tweet(String publisherUsername, String text, Image img, int fullImgID) {
        this.publisherUsername = publisherUsername;
        this.image = (img==null ? null : new ImageIcon(img));
        this.text = text;
        this.fullImgID = fullImgID;
    }

    public String getPublisherUsername() {
        return this.publisherUsername;
    }

    public Image getImg() {
        return ( this.image==null ? null : this.image.getImage());
    }

    public String getText() {
        return this.text;
    }

    public int getFullImgID() {
        return this.fullImgID;
    }

    @Override
    public String toString() {
        return String.format("@%s: %s (img: %d)", this.getPublisherUsername(), this.getText(), this.getFullImgID());
    }
}
