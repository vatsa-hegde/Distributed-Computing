import Mobile.*;
import java.rmi.Naming;
import java.util.*;
import java.io.*;

public class InvertedIndexAgentDeployer {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java InvertedIndexAgentDeployer <host> <keyword>");
            return;
        }

        String host = args[0];       
        String keyword = args[1];    
        int port = 5099;

        try {
            List<String> dummyList = new ArrayList<>(); 
            //Sending the umm dummy list to the agent becasue the focus is 
            //on developing mobile agents rather than the hop.
            //hopping will be impleted in the next version.
            IndexingAgent agent = new IndexingAgent(keyword, dummyList, port);
            agent.setId(new Random().nextInt(10000));
            agent.setPort(port);
            agent.setHost(host);
            agent.setFunction("init"); // Set the function to invoke upon deployment

            // RMI lookup
            String url = "rmi://" + host + ":" + port + "/place";
            PlaceInterface remote = (PlaceInterface) Naming.lookup(url);
            boolean success = remote.transfer(agent.getClass().getName(), agent.getByteCode(), serialize(agent));

            if (success) {
                System.out.println("Agent successfully deployed to " + host);
            } else {
                System.err.println("Agent deployment failed on " + host);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] serialize(Agent agent) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(agent);
        return out.toByteArray();
    }
}
