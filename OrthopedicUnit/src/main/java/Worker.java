import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public abstract class Worker {
    Channel channel;
    static final String hospitalExchangeName = "hospitalExchange";
    static final String logExchangeName = "logExchange";
    String hospitalQueueName;
    String logQueueName;

    public Worker() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare(hospitalExchangeName, BuiltinExchangeType.TOPIC);
        channel.exchangeDeclare(logExchangeName, BuiltinExchangeType.FANOUT);
        logQueueName = channel.queueDeclare().getQueue();
        hospitalQueueName = channel.queueDeclare().getQueue();
        channel.queueBind(logQueueName, logExchangeName, "");
    }

    public void setConsume(String queueName) throws Exception {
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                String message = new String(body, StandardCharsets.UTF_8);
                System.out.println("Received: " + message);
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }
}
