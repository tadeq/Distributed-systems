import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class Technician extends Worker {
    private String[] examinedInjuries;
    private String name;

    private Technician(String name, String[] injuries) throws Exception {
        this.examinedInjuries = injuries;
        this.name = name;
        channel.basicQos(1);
    }

    private void run() throws Exception {
        setConsume(logQueueName);
        String[] injuryQueues = new String[2];
        for (int i = 0; i < 2; i++) {
            injuryQueues[i] = channel.queueDeclare(examinedInjuries[i], false, false, false, null).getQueue();
            channel.queueBind(injuryQueues[i], hospitalExchangeName, examinedInjuries[i]);
        }
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body);
                Random random = new Random();
                int examinationTime = random.nextInt(4) + 1;
                try {
                    Thread.sleep(examinationTime * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String[] parts = message.split(", ");
                channel.basicPublish("", properties.getReplyTo(), null, (parts[0] + " done by " + name).getBytes());
                channel.basicPublish(hospitalExchangeName, "log", null, (parts[0] + " done by " + name).getBytes());
                System.out.println("Received: " + parts[0] + " from " + parts[1]);
            }
        };
        for (int i = 0; i < 2; i++) {
            channel.basicConsume(injuryQueues[i], true, consumer);
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Technician name: ");
        String technicianName = scanner.nextLine();
        System.out.print("Examined injuries: ");
        String injuries = scanner.nextLine();
        String[] examinedInjuries = injuries.split(" ");
        Technician technician = new Technician(technicianName, examinedInjuries);
        technician.run();
    }
}