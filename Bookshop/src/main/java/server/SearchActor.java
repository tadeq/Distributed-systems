package server;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import requests.SearchRequest;
import requests.SearchResult;

public class SearchActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequest.class, searchRequest -> {
                    ActorRef firstDataBaseSearchActor = getContext().actorOf(Props.create(DatabaseSearchActor.class, "firstDB.txt"));
                    ActorRef secondDataBaseSearchActor = getContext().actorOf(Props.create(DatabaseSearchActor.class, "secondDB.txt"));
                    firstDataBaseSearchActor.tell(searchRequest, getSelf());
                    secondDataBaseSearchActor.tell(searchRequest, getSelf());
                })
                .match(SearchResult.class, searchResult -> {
                    if (searchResult.isBookFound()) {
                        searchResult.replyTo.tell(searchResult, null);
                        getContext().stop(self());
                    }
                })
                .matchAny(o -> System.out.println(o.toString()))
                .build();
    }

}
