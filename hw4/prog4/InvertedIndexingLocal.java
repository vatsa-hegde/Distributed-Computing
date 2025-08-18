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
import java.util.Hashtable;
import java.util.Map.Entry;


import com.hazelcast.core.HazelcastInstanceAware;
import java.util.concurrent.*;
import java.util.Date;
import java.io.Serializable;

public class InvertedIndexingLocal {
    public static void main( String[] args ) {
		// validate arguments
        if ( args.length != 1 ) {
            System.out.println( "usage: java InvertedIndexingLocal keyword " );
            return;
        }
        String keyword = args[0];

		// prepare a local map
		Hashtable<String, Integer> local = new Hashtable<String, Integer>( );

		// start hazelcast and retrieve a cached map
		HazelcastInstance hz = Hazelcast.newHazelcastInstance( );

		// start a timer
		Date startTimer = new Date( );
	
        IMap<String, String> map = hz.getMap( "files" );
		// examine each file 
        Iterator<Entry<String, String>> iterator = map.entrySet().iterator();

        // Count occurrences of the keyword in each file
		while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            String filename = entry.getKey();
            String contents = entry.getValue();

            int count = countOccurrences(contents, keyword);
            if (count > 0) {
                local.put(filename, count);
            }
        }
		Date endTimer = new Date( ); // before showing the result, stop the timer for the perrformance measurement.

		// show the result  
		for (Map.Entry<String, Integer> entry : local.entrySet()) {
            System.out.println("File[" + entry.getKey() + "] has " + entry.getValue());
        }

        System.out.println("Execution Time: " + (endTimer.getTime() - startTimer.getTime()) + " ms");

        hz.shutdown();
    }
    // Count occurrences of a word in a given text
	private static int countOccurrences(String text, String word) {
        int count = 0;
        int index = 0;

        while ((index = text.indexOf(word, index)) != -1) {
            count++;
            index += word.length();
        }

        return count;
    }
}
