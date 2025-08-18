import Mobile.*;
import java.rmi.*;
import java.net.*;

public class DirectAgent extends Agent implements AgentInterface {
    private String[] destination;

    public DirectAgent(String[] args) {
        destination = args; // args[0] = cssmpiX destination, args[1] = "send" or "receive"
    }

    public void init() {
        System.out.println("DirectAgent (" + getId() + ") invoked init, mode: " + destination[1]);
        try {
            if ("receive".equals(destination[1])) {
                // Register this agent to the registry
                AgentRegistry registry = (AgentRegistry) Naming.lookup("rmi://cssmpi5:50999/registry");
                registry.registerAgent(getId(), this);
                System.out.println("DirectAgent registered for receiving, ID: " + getId());
            } else if ("send".equals(destination[1])) {
                // Look up the receiver and send message
                AgentRegistry registry = (AgentRegistry) Naming.lookup("rmi://cssmpi5:50999/registry");
                int receiverId = Integer.parseInt(destination[2]);
                AgentInterface receiver = registry.getAgent(receiverId);
                // Check if the receiver is registered, if registered, send a message
                if (receiver != null) {
                    receiver.receiveMessage("Direct message from Agent " + getId());
                    System.out.println("Message sent directly to Agent " + receiverId);
                } else {
                    System.out.println("Receiver agent not found.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveMessage(String msg) throws RemoteException {
        System.out.println("DirectAgent (" + getId() + ") received on " + getHost() + ": \"" + msg + "\"");
    }
}
