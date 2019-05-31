package server;

import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.ThrottleMode;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import requests.StreamRequest;
import requests.StreamResult;
import scala.concurrent.duration.Duration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StreamActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StreamRequest.class, streamRequest -> {

                    final ActorSystem system = ActorSystem.create("stream_system");

                    final Materializer materializer = ActorMaterializer.create(system);

                    String filename = "books/" + streamRequest.getTitle() + ".txt";

                    List<StreamResult> result = new ArrayList<>();

                    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                        while (br.ready()) {
                            result.add(new StreamResult(br.readLine()));
                        }
                    } catch (FileNotFoundException e) {
                        System.out.println("Book not found");
                    }
                    final Source<StreamResult, NotUsed> source = Source.from(result);

                    source.throttle(1, Duration.create(1, TimeUnit.SECONDS), 1, ThrottleMode.shaping())
                            .runWith(Sink.actorRef(streamRequest.replyTo, "Streaming completed"), materializer);

                    getContext().stop(getSelf());
                })
                .matchAny(o -> System.out.println(o.toString()))
                .build();
    }
}
