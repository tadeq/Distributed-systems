package server;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import requests.OrderRequest;
import requests.SearchRequest;
import requests.StreamRequest;

public class ServerActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequest.class, searchRequest -> {
                    log.info("Search book " + searchRequest.getTitle());
                    ActorRef actor = getContext().actorOf(Props.create(SearchActor.class));
                    actor.tell(searchRequest, self());
                })
                .match(OrderRequest.class, orderRequest -> {
                    log.info("Order book " + orderRequest.getTitle());
                    ActorRef actor = getContext().actorOf(Props.create(OrderActor.class));
                    actor.tell(orderRequest, self());
                })
                .match(StreamRequest.class, streamRequest -> {
                    log.info("Stream book " + streamRequest.getTitle());
                    ActorRef actor = getContext().actorOf(Props.create(StreamActor.class));
                    actor.tell(streamRequest, self());
                })
                .matchAny(o -> log.info("Unknown message"))
                .build();
    }
}
