package server;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import requests.SearchRequest;
import requests.SearchResult;

import java.io.BufferedReader;
import java.io.FileReader;

public class DatabaseSearchActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private String fileName;

    public DatabaseSearchActor(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequest.class, this::searchInDatabase)
                .matchAny(o -> System.out.println(o.toString()))
                .build();
    }

    private void searchInDatabase(SearchRequest request) {

        SearchResult result = new SearchResult();
        result.setBookFound(true);
        try {
            result.setPrice(findBookPrice(this.fileName, request.getTitle()));
        } catch (Exception e) {
            result.setBookFound(false);
        }
        result.setTitle(request.getTitle());
        result.setReplyTo(request.replyTo);
        getSender().tell(result, self());
    }

    private double findBookPrice(String dbFileName, String title) throws Exception {

        BufferedReader reader = new BufferedReader(new FileReader(dbFileName));
        String line = reader.readLine();

        while (line != null) {
            String[] result = line.split("-");
            String dbBookTitle = result[0];
            double dbBookPrice = Double.parseDouble(result[1]);

            if (dbBookTitle.equals(title)) return dbBookPrice;

            line = reader.readLine();
        }
        reader.close();

        throw new Exception("Book not found in database " + this.fileName);
    }
}
