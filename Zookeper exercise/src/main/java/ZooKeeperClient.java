import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ZooKeeperClient {
    private final ZooKeeper zooKeeper;
    private Process process;
    static final String ZNODE = "/z";

    public ZooKeeperClient(String hosts) throws IOException {
        zooKeeper = new ZooKeeper(hosts, 5000, event -> {
        });
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
    }

    public void runOrWaitForNode() throws KeeperException, InterruptedException {
        Optional<Stat> stat = Optional.ofNullable(handleZNodeEvent());
        stat.ifPresent(s -> runProcessAndWatchChildren());
    }

    private Stat handleZNodeEvent() throws KeeperException, InterruptedException {
        return zooKeeper.exists(ZNODE, event -> {
            if (event.getType() == Watcher.Event.EventType.NodeCreated)
                runProcessAndWatchChildren();
            else if (event.getType() == Watcher.Event.EventType.NodeDeleted)
                destroyProcess();
            try {
                handleZNodeEvent();
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void runProcessAndWatchChildren() {
        runProcess();
        try {
            watchForChildrenChanges(ZNODE);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void runProcess() {
        Runtime runTime = Runtime.getRuntime();
        String command = "C:\\Windows\\notepad.exe";
        try {
            process = runTime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void watchForChildrenChanges(String node) throws KeeperException, InterruptedException {
        List<String> children = zooKeeper.getChildren(node, false);
        for (String child : children) {
            watchForChildrenChanges(node + "/" + child);
        }
        zooKeeper.getChildren(node, event -> {
            if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                try {
                    System.out.println("Znode " + ZNODE + " has " + countChildren(ZNODE) + " children now");
                    watchForChildrenChanges(node);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private int countChildren(String currentNode) throws KeeperException, InterruptedException {
        List<String> children = zooKeeper.getChildren(currentNode, false);
        if (children.isEmpty())
            return 0;
        int count = children.size();
        for (String child : children) {
            count += countChildren(currentNode + "/" + child);
        }
        return count;
    }

    public void showTree(String node) throws KeeperException, InterruptedException {
        List<String> children = zooKeeper.getChildren(node, false);
        if (!children.isEmpty()) {
            System.out.print(node + " children: ");
            children.forEach(child -> System.out.print("[" + node + "/" + child + "]  "));
            System.out.println();
            for (String childNode : children) {
                showTree(node + "/" + childNode);
            }
        }
    }

    private void destroyProcess() {
        process.destroy();
        System.out.println("Stopped external app");
    }
}
