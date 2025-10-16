import com.hazelcast.core.*;
import com.hazelcast.map.IMap;
import com.hazelcast.executor.*;
import java.util.concurrent.*;
import java.util.*;
import com.hazelcast.cluster.Member;


public class InvertedIndexingRemote {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java InvertedIndexingRemote <keyword>");
            return;
        }

        // Get the keyword from command line arguments
        String keyword = args[0];
        // Create a local map to store results
        Map<String, Integer> local = new HashMap<>();

        System.out.println("---------Joining Hazelcast cluster----------");
        // Create a Hazelcast instance to join the cluster
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        Date start = new Date();
        System.out.println("Start time recorded: " + start);

        System.out.println("Retrieving executor service");
        // Get the executor service from Hazelcast instance
        IExecutorService exec = hz.getExecutorService("exec");
        // Create a task for inverted indexing
        InvertedIndexingEach task = new InvertedIndexingEach(keyword);
        // Set the Hazelcast instance in the task
        Set<Member> members = hz.getCluster().getMembers();
        System.out.println("Dispatching task to " + members.size() + " cluster members");

        // Submit the task to all members of the cluster
        exec.submitToMembers(task, members, new MultiExecutionCallback() {
            // This callback will be invoked when the task is executed on each member
            @Override
            public void onResponse(Member member, Object value) {
                System.out.println("Received response from: " + member.getAddress());
            }
            
            // This callback will be invoked when all responses are received
            @Override
            public void onComplete(Map<Member, Object> values) {
                System.out.println("All responses received. Processing results");
                Date end = new Date();

                for (Object resultObj : values.values()) {
                    String result = (String) resultObj;
                    String[] tokens = result.split(" ");
                    for (int i = 0; i < tokens.length - 1; i += 2) {
                        String filename = tokens[i];
                        int count = Integer.parseInt(tokens[i + 1]);
                        local.put(filename, count);
                    }
                }

                System.out.println("Inverted Indexing Results:");
                for (Map.Entry<String, Integer> entry : local.entrySet()) {
                    System.out.println("File[" + entry.getKey() + "] has " + entry.getValue());
                }

                System.out.println("Remote Execution Time: " + (end.getTime() - start.getTime()) + " ms");

                System.out.println("----------Shutting down Hazelcast instance----------");
                hz.shutdown();
            }
        });
    }
}
