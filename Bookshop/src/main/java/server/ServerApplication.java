package server;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class ServerApplication {

    public static void main(String[] args) {

        File configFile = new File("server_app.conf");

        Config config = ConfigFactory.parseFile(configFile);

        final ActorSystem system = ActorSystem.create("server_system", config);

        ActorRef serverActor = system.actorOf(Props.create(ServerActor.class), "server");
    }
}
