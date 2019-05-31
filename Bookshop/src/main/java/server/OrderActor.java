package server;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import requests.OrderRequest;
import requests.OrderResult;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class OrderActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrderRequest.class, this::handleOrder)
                .matchAny(o -> System.out.println(o.toString()))
                .build();
    }

    private void handleOrder(OrderRequest request) {

        OrderResult result = new OrderResult();
        result.setConfirmed(true);
        result.setTitle(request.getTitle());

        try {
            writeToFile("orders.txt", request.getTitle());
        } catch (IOException e) {
            System.out.println("Writing to file failed");
            result.setConfirmed(false);
        }

        request.replyTo.tell(result, self());
        getContext().stop(getSelf());
    }

    private static synchronized void writeToFile(String filename, String text) throws IOException {
        Writer output = new BufferedWriter(new FileWriter(filename, true));
        output.append(text).append("\n");
        output.close();
    }

}
