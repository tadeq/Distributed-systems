import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Admin extends Worker {
    private Scanner scanner;

    public Admin() throws IOException, TimeoutException {
        scanner = new Scanner(System.in);
    }

    public void run() throws IOException {
        String logQueue = channel.queueDeclare().getQueue();
        channel.queueBind(logQueue, hospitalExchangeName, "log");
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                String message = new String(body);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(message);
            }
        };
        channel.basicConsume(logQueue, true, consumer);
        while (true) {
            sendInfo();
        }
    }

    private void sendInfo() throws IOException {
        System.out.print("Enter info message: ");
        String message = scanner.nextLine();
        channel.basicPublish(logExchangeName, "", null, ("INFO: " + message).getBytes());
    }

    public static void main(String[] args) throws Exception {
        Admin admin = new Admin();
        admin.run();
    }
}
