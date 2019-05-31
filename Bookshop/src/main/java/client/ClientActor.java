package client;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import requests.OrderResult;
import requests.Request;
import requests.SearchResult;
import requests.StreamResult;

public class ClientActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    final String serverPath = "akka.tcp://server_system@127.0.0.1:10001/user/server";

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, request -> {
                    request.replyTo = getSelf();
                    getContext().actorSelection(serverPath).tell(request, getSelf());
                })
                .match(SearchResult.class, searchResult -> {
                    if (searchResult.isBookFound()) {
                        System.out.println(searchResult.getTitle() + " price: " + searchResult.getPrice());
                    } else {
                        System.out.println(searchResult.getTitle() + " not found");
                    }
                })
                .match(OrderResult.class, orderResult -> System.out.println(orderResult.getTitle() + " ordered. Confirmation status: " + orderResult.isConfirmed()))
                .match(StreamResult.class, result -> System.out.println(result.getLine()))
                .matchAny(o -> {
                    if (o instanceof String) {
                        if (o.equals("Streaming completed"))
                            System.out.println(o);
                    } else
                        log.info("Unknown message");
                })
                .build();
    }
}
