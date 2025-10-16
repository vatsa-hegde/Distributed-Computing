package Mobile;

import com.hazelcast.core.*;
import com.hazelcast.map.IMap;
import java.io.InputStream;
import java.io.FileInputStream;


import java.util.*;

public class ChildIndexingAgent extends Agent {
    private String keyword;
    // Map to store local results with filename as key and count of keyword occurrences as value
    private Map<String, Integer> localResults;

    public ChildIndexingAgent(String keyword, int port) {
        this.keyword = keyword;
        this.localResults = new HashMap<>();
        this.setPort(port);
    }

    // Initializes the agent and starts the indexing process
    public void init() {
        System.out.println("ChildIndexingAgent[" + getId() + "] started on host: " + getHost());
        performIndexing();
    }
    // Performs the indexing operation
    private void performIndexing() {
        HazelcastInstance hz;
        try {
            // Load Hazelcast configuration from the specified path or use default configuration
            String configPath = System.getProperty("hazelcast.config");
            if (configPath != null) {
                InputStream configInput = new java.io.FileInputStream(configPath);
                com.hazelcast.config.Config config = new com.hazelcast.config.XmlConfigBuilder(configInput).build();
                // Create a Hazelcast instance with the loaded configuration
                hz = Hazelcast.newHazelcastInstance(config);
            } else {
                hz = Hazelcast.newHazelcastInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // Fetch the map containing files and their contents
        IMap<String, String> map = hz.getMap("files");
        Set<String> localKeys = map.localKeySet();
        long start = System.currentTimeMillis();
        for (String filename : localKeys) {
            String content = map.get(filename);
            int count = countOccurrences(content, keyword);
            if (count > 0) {
                localResults.put(filename, localResults.getOrDefault(filename, 0) + count);
            }
        }

        System.out.println("Indexing done at: " + getHost());
        for (Map.Entry<String, Integer> entry : localResults.entrySet()) {
            System.out.println("File[" + entry.getKey() + "] has " + entry.getValue());
        }

        long end = System.currentTimeMillis();
        System.out.println("Execution Time in " + getHost() + " : " + (end - start) + " ms");
        IMap<String, Map<String, Integer>> resultMap = hz.getMap("results");
        resultMap.put(getHost() + "-" + getId(), localResults);
        try{
            Thread.sleep(500);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        hz.shutdown();
    }

    // Counts occurrences of the keyword in the given text
    private int countOccurrences(String text, String word) {
        int count = 0, idx = 0;
        while ((idx = text.indexOf(word, idx)) != -1) {
            count++;
            idx += word.length();
        }
        return count;
    }

}
