package requests;

import akka.actor.ActorRef;

import java.io.Serializable;

public class SearchResult implements Serializable {

    private double price;

    private String title;

    private boolean bookFound = false;

    public ActorRef replyTo;

    public void setReplyTo(ActorRef replyTo) {
        this.replyTo = replyTo;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isBookFound() {
        return bookFound;
    }

    public void setBookFound(boolean bookFound) {
        this.bookFound = bookFound;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
