import Mobile.*;
import java.rmi.*;

public class TestAgentReceiver extends Agent {
    private String[] destination;

    public TestAgentReceiver(String[] args) {
        destination = args;
    }

    public void init() {
        System.out.println("Receiver agent (" + getId() + ") invoked init");

        try {
            // central node
            PlaceInterface remote = (PlaceInterface) Naming.lookup("rmi://path/place");  
            // Register itself with the local Place under a known name
            remote.registerAgent("receiverAgent", getId());
            System.out.println(getId() +" Registered as 'receiverAgent'");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Migrate to the destination to receive messages
        hop(destination[0], "receive", new String[0]);

    }

    /**
     * receive( ) is invoked when the agent has completed its task.
     *
     * @param args The arguments passed to the receive method.
     */
    public void receive(String[] args) {
        System.out.println( getId() + " invoked receive");
        try {
            // Register itself with the local Place under a known name
            PlaceInterface remote = (PlaceInterface) Naming.lookup("rmi://localhost:" + getPort() + "/place");
            for (int i = 0; i < 5; i++) {
                // retrieve message
                String msg = remote.retrieveMessage(getId());
                if (msg != null) {
                    System.out.println("Received: \"" + msg + "\"");
                    return;
                } else {
                    System.out.println("No message yet, retrying...");
                    Thread.sleep(2000); // wait 2 second before retry
                }
            }

            System.out.println("No message received after retries.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
