package Mobile;
import com.hazelcast.core.*;
import com.hazelcast.map.IMap;

import java.util.*;
import java.rmi.*;
import java.io.*;

public class IndexingAgent extends Agent {
    private String keyword;
    private List<String> hopList; // hostnames to hop
    private int currentHop;
    private Map<String, Integer> localResults;

    public IndexingAgent(String keyword, List<String> hopList, int port) {
        this.keyword = keyword;
        this.hopList = hopList;
        this.currentHop = 0;
        this.localResults = new HashMap<>();
        this.setPort(port);
    }

    // First method to invoke
    public void init() {
        System.out.println("IndexingAgent[" + getId() + "] starting on host: " + getHost());
        performIndexing(); // Call for  Moble Agent additional feature

    }

    // Hazelcast local indexing
    private void performIndexing() {
        HazelcastInstance hz = getHazelcastInstance();

        Date startTimer = new Date( );

        IMap<String, String> map = hz.getMap("files");

        Set<String> localKeys = map.localKeySet();

        for (String filename : localKeys) {
            String content = map.get(filename);
            int count = countOccurrences(content, keyword);
            if (count > 0) {
                localResults.put(filename, localResults.getOrDefault(filename, 0) + count);
            }
        }

        Date endTimer = new Date( );

        System.out.println("Indexing done at: " + getHost());
        System.out.println("=== Final Inverted Indexing Results ===");
        for (Map.Entry<String, Integer> entry : localResults.entrySet()) {
            System.out.println("File[" + entry.getKey() + "] has " + entry.getValue());
        }
        System.out.println("Execution Time in " + getHost() +" : " + (endTimer.getTime() - startTimer.getTime()) + " ms");
        hz.shutdown();
    }

    private int countOccurrences(String text, String word) {
        int count = 0, idx = 0;
        while ((idx = text.indexOf(word, idx)) != -1) {
            count++;
            idx += word.length();
        }
        return count;
    }

}
