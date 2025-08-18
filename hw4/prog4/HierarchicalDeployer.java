import Mobile.*;
import java.util.*;

public class HierarchicalDeployer {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java HierarchicalDeployer <host> <keyword>");
            return;
        }

        String host = args[0];
        String keyword = args[1];
        int port = 5099;

        List<String> children = Arrays.asList("cssmpi1", "cssmpi2", "cssmpi3");  

        try {
            HierarchicalIndexingAgent parent = new HierarchicalIndexingAgent(keyword, children, port);
            parent.setId(new Random().nextInt(10000));
            parent.setPort(port);
            parent.setHost(host);

            String url = "rmi://" + host + ":" + port + "/place";
            PlaceInterface remote = (PlaceInterface) java.rmi.Naming.lookup(url);
            boolean success = remote.transfer(parent.getClass().getName(), parent.getByteCode(), serialize(parent));

            if (success) {
                System.out.println("Parent agent dispatched to " + host);
            } else {
                System.err.println("Parent dispatch failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] serialize(Agent agent) throws Exception {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream os = new java.io.ObjectOutputStream(out);
        os.writeObject(agent);
        return out.toByteArray();
    }
}
