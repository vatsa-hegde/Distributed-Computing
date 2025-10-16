package spouts;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;


public class FlightsDataReader extends BaseRichSpout {
    private SpoutOutputCollector collector;
    private FileReader fileReader;
    private boolean completed = false;

    public void ack(Object msgId) {
        System.out.println("OK: " + msgId);
    }

    public void fail(Object msgId) {
        System.out.println("FAIL: " + msgId);
    }

    public void close() {}

    public void nextTuple() {
        if (completed) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            return;
        }

        try {
            StringBuilder jsonBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(fileReader);
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONObject root = new JSONObject(jsonBuilder.toString());
            JSONArray states = root.getJSONArray("states");

            for (int i = 0; i < states.length(); i++) {
                JSONArray flight = states.getJSONArray(i);
                if (flight.length() == 17) {
                    Object[] fields = new Object[17];
                    for (int j = 0; j < 17; j++) {
                        fields[j] = (flight.isNull(j)) ? "" : flight.get(j).toString().trim();
                    }
                    // System.out.println("EMITTING: " + Arrays.toString(fields));
                    collector.emit(new Values(fields), fields[1]);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error parsing flights.json", e);
        }

        completed = true;
    }

    

    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
        try {
            String filePath = conf.get("FlightsFile").toString();
            System.out.println("Reading flights data from: " + filePath);
            this.fileReader = new FileReader(filePath);
        } catch (Exception e) {
            throw new RuntimeException("Error reading flight file", e);
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(
            "transponder_address", "call_sign", "origin_country", "last_contact1", "last_contact2",
            "longitude", "latitude", "altitude_barometric", "on_ground", "velocity", "heading",
            "vertical_rate", "sensors", "altitude_geometric", "transponder_code", "special_purpose", "origin"
        )); // Adjusted couple of names to make them more meaningful and easy to fetch
    }
}

