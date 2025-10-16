import Mobile.*;
import java.rmi.*;
import java.lang.reflect.*;
import java.net.*;

public class ParentAgent extends Agent {
    private String[] destination;

    public ParentAgent(String[] args) {
        destination = args; // args[0] = child destination
    }

    public void init() {
        System.out.println("Parent agent (" + getId() + ") invoked init");

        try {
            // Load the bytecode for ChildAgent
            String className = "ChildAgent";
            byte[] bytecode = Agent.getByteCode("ChildAgent");
            if (bytecode == null) {
                System.err.println("ERROR: Could not load ChildAgent bytecode.");
                return;
            }
            System.out.println("Bytecode loaded for ChildAgent, size: " + bytecode.length);

            // Create an instance of ChildAgent using reflection
            AgentLoader loader = new AgentLoader();
            Class<?> cls = loader.loadClass(className, bytecode);
            Constructor<?> ctor = cls.getConstructor(String[].class);
            // Pass the destination as an argument to the constructor
            String[] childArgs = new String[] { destination[0] };
            Agent child = (Agent) ctor.newInstance((Object) childArgs);
            child.setPort(getPort());

            System.out.println("Calling child.hop(...) to " + destination[0]);
            child.hop(destination[0], "run", new String[0]);  // 'run' method in child
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}