package bolts;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AirlineSorter extends BaseBasicBolt {

    // This is used to store the number of flights for each airline at each airport
    private Map<String, Map<String, Integer>> stats;

    // Map from airport code to city name
    private Map<String, String> codeToCity;

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        stats = new HashMap<>();
        codeToCity = new HashMap<>();
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        // String airportCity = input.getStringByField("airport.city");
        // String airportCode = input.getStringByField("airport.code");
        String airlineCode = input.getStringByField("call_sign");

        String regionKey = input.getStringByField("region");  //changed to region

        // Store code -> city mapping
        // codeToCity.putIfAbsent(airportCode, airportCity);

        // Keyed by airport code
        // stats.putIfAbsent(airportCode, new HashMap<String,Integer>());
        // Map<String, Integer> airlineMap = stats.get(airportCode);

        // added as part of region key
        stats.putIfAbsent(regionKey, new HashMap<String,Integer>());
        Map<String, Integer> airlineMap = stats.get(regionKey);

        airlineMap.put(airlineCode, airlineMap.getOrDefault(airlineCode, 0) + 1);
    }

    @Override
    public void cleanup() {
        System.out.println("-- Flight Counter --");

        for (Map.Entry<String, Map<String, Integer>> airportEntry : stats.entrySet()) {
            String airport = airportEntry.getKey();
            Map<String, Integer> airlineMap = airportEntry.getValue();
            // String cityName = codeToCity.getOrDefault(airport, "Unknown");

            // System.out.println("At Airport: " + airport + " (" + cityName + ")");
            System.out.println("In Area: " + airport); //changed to region
            int total = 0;

            List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(airlineMap.entrySet());
            sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            for (Map.Entry<String, Integer> entry : sortedEntries) {
                System.out.println("\t" + entry.getKey() + ": " + entry.getValue());
                total += entry.getValue();
            }

            System.out.println("\ttotal #flights = " + total);
            System.out.println();
        }
    }



    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // No output fields from final bolt
    }
}
