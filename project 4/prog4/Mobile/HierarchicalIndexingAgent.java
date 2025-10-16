package Mobile;

import com.hazelcast.core.*;
import com.hazelcast.map.IMap;

import java.io.Serializable;
import java.util.*;

public class HierarchicalIndexingAgent extends Agent implements Serializable {
    private String keyword;
    private List<String> childHosts;
    private int port;
    // Name of the shared map in Hazelcast where child results will be stored
    private String sharedMapName = "child-results";

    // Constructor to initialize the agent with keyword, child hosts, and port
    public HierarchicalIndexingAgent(String keyword, List<String> childHosts, int port) {
        this.keyword = keyword;
        this.childHosts = childHosts;
        this.port = port;
        this.setFunction("init");
    }

    public void init() {
        System.out.println("Hierarchical agent starting at: " + getHost());

        long start = System.currentTimeMillis();

        // Dispatch children
        for (String host : childHosts) {
            ChildIndexingAgent child = new ChildIndexingAgent(keyword,port);
            child.setId(new Random().nextInt(10000));
            child.setPort(port);
            child.setHost(host);
            child.setFunction("init");

            try {
                String url = "rmi://" + host + ":" + port + "/place";
                PlaceInterface remote = (PlaceInterface) java.rmi.Naming.lookup(url);
                boolean success = remote.transfer(child.getClass().getName(), child.getByteCode(), serialize(child));
                if (success) {
                    System.out.println("Dispatched child to " + host);
                } else {
                    System.err.println("Failed to dispatch to " + host);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Wait for children to index
        try {
            Thread.sleep(5000); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Aggregate results from Hazelcast shared map
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        collectResults(hz);
        hz.shutdown();
    }

    private byte[] serialize(Agent agent) throws Exception {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream os = new java.io.ObjectOutputStream(out);
        os.writeObject(agent);
        return out.toByteArray();
    }

    // Collect results from all child agents stored in Hazelcast
    private void collectResults(HazelcastInstance hz) {
        IMap<String, Map<String, Integer>> resultMap = hz.getMap("results");
        System.out.println("=== Aggregated Results from All Children in Parent: "+ getHost() + " ===");

        for (Map.Entry<String, Map<String, Integer>> entry : resultMap.entrySet()) {
            String childId = entry.getKey();
            Map<String, Integer> childResults = entry.getValue();

            

            if (childResults != null && !childResults.isEmpty()) {
                System.out.println("Results from: " + childId );
                for (Map.Entry<String, Integer> fileEntry : childResults.entrySet()) {
                    System.out.println("  File[" + fileEntry.getKey() + "] has " + fileEntry.getValue());
                }
            } else {
                System.out.println("No files indexed or no matches found in "+ childId);
            }
        }
    }


}
