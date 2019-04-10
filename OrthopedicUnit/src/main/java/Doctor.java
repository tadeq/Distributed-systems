import com.rabbitmq.client.AMQP;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Doctor extends Worker {
    private String name;
    private Scanner scanner;

    private Doctor(String name) throws IOException, TimeoutException {
        this.name = name;
        scanner = new Scanner(System.in);
    }

    private void run() throws Exception {
        setConsume(hospitalQueueName);
        setConsume(logQueueName);
        while (true) {
            sendOrder();
        }
    }

    private void sendOrder() throws IOException {
        System.out.print("Examination type: ");
        String examinationType = scanner.nextLine();
        System.out.print("Patient: ");
        String patient = scanner.nextLine();
        channel.basicPublish(hospitalExchangeName, examinationType, new AMQP.BasicProperties().builder().replyTo(hospitalQueueName).build(), (patient + ", " + name).getBytes());
        channel.basicPublish(hospitalExchangeName, "log", null, ("Doctor: " + name + ", Patient: " + patient + ", Examination: " + examinationType).getBytes());
        System.out.println("Order sent");
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Doctor name: ");
        String doctorName = scanner.nextLine();
        Doctor doctor = new Doctor(doctorName);
        doctor.run();
    }
}
