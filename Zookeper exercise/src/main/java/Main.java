import org.apache.zookeeper.KeeperException;

import java.util.Scanner;

public class Main {
    private static final String CLIENT_PORTS = "localhost:2181,localhost:2182,localhost:2183";

    public static void main(String[] argv) throws Exception {
        ZooKeeperClient client = new ZooKeeperClient(CLIENT_PORTS);
        client.runOrWaitForNode();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            if (line.equals("tree")) {
                try {
                    client.showTree(ZooKeeperClient.ZNODE);
                } catch (KeeperException | InterruptedException e) {
                    System.out.println("No child found");
                }
            } else if (line.equals("exit")) {
                client.close();
                System.exit(0);
            }
        }
    }

}
