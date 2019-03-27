import org.jgroups.*;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

public class Receiver extends ReceiverAdapter {
    private final DistributedMap distributedMap;
    private JChannel channel;

    public Receiver(DistributedMap distributedMap, JChannel channel) {
        this.distributedMap = distributedMap;
        this.channel = channel;
    }

    @Override
    public void receive(Message msg) {
        String message = (String) msg.getObject();
        System.out.println("SOURCE: " + msg.getSrc() + "\nTEXT: " + message);
        String[] parts = message.split(" ");
        if (parts[0].equals("put")) {
            distributedMap.getMap().put(parts[1], Integer.parseInt(parts[2]));
        }
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (this.distributedMap) {
            Util.objectToStream(distributedMap.getMap(), new DataOutputStream(output));
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        HashMap<String, Integer> newMap;
        newMap = (HashMap<String, Integer>) Util.objectFromStream(new DataInputStream(input));
        synchronized (this.distributedMap) {
            this.distributedMap.getMap().clear();
            this.distributedMap.getMap().putAll(newMap);
        }
    }

    @Override
    public void viewAccepted(View view) {
        if (view instanceof MergeView) {
            ViewHandler handler = new ViewHandler(channel, (MergeView) view);
            handler.start();
        }
    }

    private static class ViewHandler extends Thread {
        JChannel ch;
        MergeView view;

        private ViewHandler(JChannel ch, MergeView view) {
            this.ch = ch;
            this.view = view;
        }

        public void run() {
            List<View> subgroups = view.getSubgroups();
            View tmp_view = subgroups.get(0); // picks the first
            Address local_addr = ch.getAddress();
            if (!tmp_view.getMembers().contains(local_addr)) {
                System.out.println("Not member of the new primary partition ("
                        + tmp_view + "), will re-acquire the state");
                try {
                    ch.getState(null, 30000);
                } catch (Exception ex) {
                }
            } else {
                System.out.println("Not member of the new primary partition ("
                        + tmp_view + "), will do nothing");
            }
        }
    }
}
