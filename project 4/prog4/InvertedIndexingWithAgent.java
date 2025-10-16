import java.util.*;
import Mobile.*;
import java.io.*;

public class InvertedIndexingWithAgent {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java InvertedIndexingWithAgent <keyword> <host1> <host2> ...");
            return;
        }

        String keyword = args[0];
        List<String> hopList = new ArrayList<>();

        for (int i = 1; i < args.length; i++) {
            hopList.add(args[i]);
        }

        int port = 5099; // fixing since this is not the primary focus of the task
        int agentId = new Random().nextInt(10000); 

        IndexingAgent agent = new IndexingAgent(keyword, hopList, port);
        agent.setId(agentId);

        System.out.println("Launching IndexingAgent with ID: " + agentId);
        agent.init();
    }
}
