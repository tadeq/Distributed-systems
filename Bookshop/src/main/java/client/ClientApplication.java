package client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import requests.OrderRequest;
import requests.SearchRequest;
import requests.StreamRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientApplication {

    public static void main(String[] args) throws IOException {

        final File configFile = new File("client_app.conf");

        final Config config = ConfigFactory.parseFile(configFile);

        final ActorSystem system = ActorSystem.create("client_system", config);

        ActorRef clientActor = system.actorOf(Props.create(ClientActor.class), "client");

        System.out.println("Available options: search, order, stream (all followed by book title)");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = br.readLine();
            if (line.startsWith("search")) {
                String title = line.substring(7);
                clientActor.tell(new SearchRequest(title), null);
            } else if (line.startsWith("order")) {
                String title = line.substring(6);
                clientActor.tell(new OrderRequest(title), null);
            } else if (line.startsWith("stream")) {
                String title = line.substring(7);
                clientActor.tell(new StreamRequest(title), null);
            } else {
                System.out.println("Unknown command");
            }
        }
    }
}
