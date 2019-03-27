import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        DistributedMap distributedMap = new DistributedMap();
        Scanner scanner = new Scanner(System.in);
        String command = "";
        while (!command.equals("exit")) {
            command = scanner.nextLine();
            String[] parts = command.split(" ");
            switch (parts[0]) {
                case "containsKey":
                    System.out.println(distributedMap.containsKey(parts[1]));
                    break;
                case "get":
                    System.out.println(distributedMap.get(parts[1]));
                    break;
                case "put":
                    distributedMap.put(parts[1], Integer.parseInt(parts[2]));
                    break;
                case "remove":
                    System.out.println(distributedMap.remove(parts[1]));
                    break;
            }
        }
    }
}
