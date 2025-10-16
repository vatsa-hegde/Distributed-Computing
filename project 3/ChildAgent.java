import Mobile.*;
import java.rmi.*;
import java.net.*;

public class ChildAgent extends Agent {
    public ChildAgent(String[] args) {
        // Just for demonstration, args[0] is the destination
    }
    
    public void run(String[] args) {
        System.out.println(" Child agent (" + getId() + ") created and running.");
    }
}
