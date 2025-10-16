import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

import spouts.FlightsDataReader;
import bolts.HubIdentifier;
import bolts.AirlineSorter;

public class TopologyMain {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: TopologyMain <flights.txt path> <airports.txt path>");
            System.exit(1);
        }

        String flightsPath = args[0];
        String airportsPath = args[1];

        // Create topology
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("flights-reader", new FlightsDataReader());

        builder.setBolt("hub-identifier", new HubIdentifier())
                .shuffleGrouping("flights-reader");

        builder.setBolt("airline-sorter", new AirlineSorter(), 1)
                .fieldsGrouping("hub-identifier", new Fields("region")); //changed to region

        // Configuration
        Config conf = new Config();
        conf.put("FlightsFile", flightsPath);
        conf.put("AirportsData", airportsPath);
        conf.setDebug(false);
        conf.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);

        // Run locally
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("Flight-Data-Analysis", conf, builder.createTopology());

        Thread.sleep(10000);
        try{
        cluster.shutdown();
        }catch (Exception e){
        }  
    
    }
}
