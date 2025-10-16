import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.IndexType;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.Predicate;
import java.util.Collection;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

import com.hazelcast.core.HazelcastInstanceAware;
import java.util.concurrent.*;
import java.util.Date;
import java.io.Serializable;

public class InvertedIndexingEach 
    implements Callable<String>, HazelcastInstanceAware, Serializable {
        // This class implements Callable to perform inverted indexing on each member of the Hazelcast cluster
        String keyword;
        private transient HazelcastInstance hz;

        // default constructor
        public InvertedIndexingEach( ) {

        }

        // constructor used to receive a keyword for inverted indexing
        public InvertedIndexingEach( String keyword ) {
            this.keyword = keyword;
        }


        @Override
        public void setHazelcastInstance( HazelcastInstance hz ) {
            this.hz = hz;
        }

    @Override
    public String call( ) throws Exception {
        System.out.println( "started" );

        Map<String, Integer> local = new HashMap<>();
        // search for a collection of files
        IMap<String, String> map = hz.getMap( "files" );

        
        Set<String> localKeys = map.localKeySet();
        //Count occurrences of the keyword in each file
        for (String filename : localKeys) {
            String content = map.get(filename);
            int count = countOccurrences(content, keyword);
            if (count > 0) {
                local.put(filename, count);
            }
        }

        // Prepare the result as a string
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Integer> entry : local.entrySet()) {
            result.append(entry.getKey()).append(" ").append(entry.getValue()).append(" ");
        }

        return result.toString().trim();

    }
    // Method to count occurrences of a word in a text
    private int countOccurrences(String text, String word) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(word, index)) != -1) {
            count++;
            index += word.length();
        }
        return count;
    }
}
