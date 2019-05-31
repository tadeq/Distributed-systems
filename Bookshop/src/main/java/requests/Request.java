package requests;

import akka.actor.ActorRef;

import java.io.Serializable;

public class Request implements Serializable {
    String title;

    public ActorRef replyTo;

    public Request(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
