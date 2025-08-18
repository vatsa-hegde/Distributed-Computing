package bolts;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.task.TopologyContext;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HubIdentifier extends BaseBasicBolt {

    private List<Airport> airports = new ArrayList<>();

    public void prepare(Map stormConf, TopologyContext context) { // Initialize the airports list
        String airportsFile = stormConf.get("AirportsData").toString();
        try (BufferedReader br = new BufferedReader(new FileReader(airportsFile))) {
            String line;
            while ((line = br.readLine()) != null) { 
                // There are only 40 entries in the airports.txt file, So there was no need to select top 40 entries.
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String city = parts[0].trim();
                    String code = parts[1].trim();
                    double lat = Double.parseDouble(parts[2].trim());
                    double lon = Double.parseDouble(parts[3].trim());
                    airports.add(new Airport(city, code, lat, lon));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading airports file", e);
        }
    }

    public void execute(Tuple input, BasicOutputCollector collector) {
        try {
            String callSign = input.getStringByField("call_sign").trim();
            double flightLat = Double.parseDouble(input.getStringByField("latitude"));
            double flightLon = Double.parseDouble(input.getStringByField("longitude"));
            double verticalRate = Double.parseDouble(input.getStringByField("vertical_rate"));
            double velocity = Double.parseDouble(input.getStringByField("velocity"));
            double altitude = Double.parseDouble(input.getStringByField("altitude_geometric"));
            boolean onGround = Boolean.parseBoolean(input.getStringByField("on_ground"));
    
            if (callSign == null || callSign.trim().isEmpty()) {
                return; // Skip flights with no call sign
            }

            // // Filter out flyovers
            // if (!onGround && verticalRate == 0) { 
            //     return; // Skip flights that are not on the ground and have no vertical rate 
            // }

            // if (velocity > 200) {
            //     return; // Skip high-speed flights
            // }

            // if (altitude > 1000) {
            //     return; // Skip high altitude flights
            // }
    
    
            // Airport nearest = null;
            // double minDistance = Double.MAX_VALUE;

            double latThreshold = 20.0 / 70.0;   //  0.2857 According to the given proximity rule
            double lonThreshold = 20.0 / 45.0;   // 0.4444 According to the given proximity rule

            //region implementation
            int regionLat = (int) Math.floor(flightLat);
            int regionLon = (int) Math.floor(flightLon);
            String regionKey = regionLat + "," + regionLon;
            String airlineCode = callSign.length() >= 3 ? callSign.substring(0, 3).trim() : callSign;
            collector.emit(new Values(regionKey,  airlineCode));

            // for (Airport airport : airports) {
            //     boolean nearLat = Math.abs(flightLat - airport.latitude) <= latThreshold;
            //     boolean nearLon = Math.abs(flightLon - airport.longitude) <= lonThreshold;

                

            //     if (nearLat || nearLon) { // Near either latitude or longitude
            //         double dist = Math.sqrt(Math.pow(flightLat - airport.latitude, 2) + Math.pow(flightLon - airport.longitude, 2)); //calculating the Euclidean distance
            //         if (nearest == null || dist < minDistance) {
            //             nearest = airport;
            //             minDistance = dist;
            //         }
            //     }
            // }
                
            // if (nearest != null) {
            //     System.out.println("NEAREST " + nearest.city +" (" + nearest.code + "): " + callSign);
            //     // Airline code is typically the first 3 letters of the call sign
            //     String airlineCode = callSign.length() >= 3 ? callSign.substring(0, 3).trim() : callSign;
            //     collector.emit(new Values(nearest.city, nearest.code, airlineCode));
            // }
        } catch (Exception ignored) {}
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // declarer.declare(new Fields("airport.city", "airport.code", "call_sign"));
        declarer.declare(new Fields("region", "call_sign"));
    }

    public void cleanup() {}


    private static class Airport implements Serializable {
        String city, code;
        double latitude, longitude;

        Airport(String city, String code, double latitude, double longitude) {
            this.city = city;
            this.code = code;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}

