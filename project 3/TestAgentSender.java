import Mobile.*;
import java.rmi.*;

public class TestAgentSender extends Agent {
    private String[] destination;

    public TestAgentSender(String[] args) {
        destination = args;
    }

    public void init() {
        System.out.println("Sender agent (" + getId() + ") invoked init");

        try {
            // Register itself
            // hardcoding the central node
            PlaceInterface remote = (PlaceInterface) Naming.lookup("rmi://path/place");  

            // Register itself with the local Place under a known name
            remote.registerAgent("senderAgent", getId());
            System.out.println("Registered as 'senderAgent'");

            // Try to look up the receiver agent by name
            int receiverId = -1;
            for (int i = 0; i < 5; i++) {
                receiverId = remote.lookupAgentId("receiverAgent");
                if (receiverId != -1) break;

                System.out.println("receiverAgent not found, retrying...");
                Thread.sleep(1000);
            }
            // If receiver agent is found, send a message
            if (receiverId != -1) {
                String msg = "Hello from senderAgent("+ getHost() +": "+ getId() + ")";
                System.out.println("Sending: \"" + msg + "\" to receiverAgent (ID: " + receiverId + ")");
                remote.deliverMessage(receiverId, msg);
                System.out.println("Message sent successfully.");
            } else {
                System.out.println("receiverAgent not found after retries. Message not sent.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Hop to next destination to complete lifecycle
        hop(destination[0], "done", new String[0]);

    }
    /**
     * done( ) is invoked when the agent has completed its task.
     *
     * @param args The arguments passed to the done method.
     */
    public void done(String[] args) {
        System.out.println("Sender agent (" + getId() + ") done.");
    }
}

