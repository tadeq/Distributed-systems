import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.ProtocolStack;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class DistributedMap implements SimpleStringMap {
    private HashMap<String, Integer> map;
    private JChannel channel;

    DistributedMap() {
        map = new HashMap<>();
        channel = new JChannel(false);
        ProtocolStack stack = new ProtocolStack();
        channel.setProtocolStack(stack);
        try {
            stack.addProtocol(new UDP().setValue("mcast_group_addr", InetAddress.getByName("230.100.200.111")))
                    .addProtocol(new PING())
                    .addProtocol(new MERGE3())
                    .addProtocol(new FD_SOCK())
                    .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
                    .addProtocol(new VERIFY_SUSPECT())
                    .addProtocol(new BARRIER())
                    .addProtocol(new NAKACK2())
                    .addProtocol(new UNICAST3())
                    .addProtocol(new STABLE())
                    .addProtocol(new GMS())
                    .addProtocol(new UFC())
                    .addProtocol(new MFC())
                    .addProtocol(new FRAG2())
                    .addProtocol(new STATE())
                    .addProtocol(new SEQUENCER())
                    .addProtocol(new FLUSH());
            stack.init();
            channel.setReceiver(new Receiver(this, channel));
            channel.connect("cluster");
            channel.getState(null, 0);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public Map<String, Integer> getMap() {
        return this.map;
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public Integer get(String key) {
        return map.get(key);
    }

    @Override
    public void put(String key, Integer value) {
        try {
            channel.send(new Message(null, null, "put " + key + " " + value));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        map.put(key, value);
    }

    @Override
    public Integer remove(String key) {
        try {
            channel.send(new Message(null, null, "remove " + key));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        return map.remove(key);
    }
}
