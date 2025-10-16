package Mobile;

import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.rmi.registry.*;

/**
 * Mobile.Place is the our mobile-agent execution platform that accepts an
 * agent transferred by Mobile.Agent.hop( ), deserializes it, and resumes it
 * as an independent thread.
 *
 */
public class Place extends UnicastRemoteObject implements PlaceInterface {
    private AgentLoader loader = null;  // a loader to define a new agent class
    private int agentSequencer = 0;     // a sequencer to give a unique agentId
    // Indirect communication
    private ConcurrentHashMap<Integer, Queue<String>> messageBox = new ConcurrentHashMap<>(); 
    // Agent directory to keep track of agent IDs
    private ConcurrentHashMap<String, Integer> agentDirectory = new ConcurrentHashMap<>(); 

    /**
     * This constructor instantiates a Mobiel.AgentLoader object that
     * is used to define a new agen class coming from remotely.
     */
    public Place( ) throws RemoteException {
	super( );
	loader = new AgentLoader( );
    }

    /**
     * deserialize( ) deserializes a given byte array into a new agent.
     *
     * @param buf a byte array to be deserialized into a new Agent object.
     * @return a deserialized Agent object
     */
    private Agent deserialize( byte[] buf ) 
	throws IOException, ClassNotFoundException {
	// converts buf into an input stream
        ByteArrayInputStream in = new ByteArrayInputStream( buf );

	// AgentInputStream identify a new agent class and deserialize
	// a ByteArrayInputStream into a new object
        AgentInputStream input = new AgentInputStream( in, loader );
        return ( Agent )input.readObject();
    }

    /**
     * transfer( ) accepts an incoming agent and launches it as an independent
     * thread.
     *
     * @param classname The class name of an agent to be transferred.
     * @param bytecode  The byte code of  an agent to be transferred.
     * @param entity    The serialized object of an agent to be transferred.
     * @return true if an agent was accepted in success, otherwise false.
     */
    public boolean transfer( String classname, byte[] bytecode, byte[] entity )
	throws RemoteException {
        try {
            //  Load agent class
            loader.loadClass(classname, bytecode);

            // Deserialize agent
            Agent agent = deserialize(entity);

            //  Assign unique ID if not yet set
            if (agent.getId() == -1) {
                int id = InetAddress.getLocalHost().hashCode() + agentSequencer++;
                agent.setId(id);
            }

            //  Start agent thread
            Thread thread = new Thread(agent);
            thread.start();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * main( ) starts an RMI registry in local, instantiates a Mobile.Place
     * agent execution platform, and registers it into the registry.
     *
     * @param args receives a port, (i.e., 5001-65535).
     */
    public static void main( String args[] ) {
        if (args.length != 1) {
        System.err.println("Usage: java Mobile.Place <port>");
        System.exit(1);
        }

        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
            if (port < 5001 || port > 65535) {
                throw new IllegalArgumentException("Port must be between 5001 and 65535");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            startRegistry(port);
            Place place = new Place();
            Naming.rebind("rmi://localhost:" + port + "/place", place);
            System.out.println("Place is running at port " + port);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    /**
     * startRegistry( ) starts an RMI registry process in local to this Place.
     * 
     * @param port the port to which this RMI should listen.
     */
    private static void startRegistry( int port ) throws RemoteException {
        try {
            Registry registry =
                LocateRegistry.getRegistry( port );
            registry.list( );
        }
        catch ( RemoteException e ) {
            Registry registry =
                LocateRegistry.createRegistry( port );
        }
    }

    /**
     * deliverMessage( ) delivers a message to the specified agent.
     *
     * @param receiverId The ID of the agent to receive the message.
     * @param message The message to be delivered.
     */
    @Override
    public void deliverMessage(int receiverId, String message) throws RemoteException {
        messageBox.computeIfAbsent(receiverId, k -> new ConcurrentLinkedQueue<>()).add(message);
        System.out.println("Delivered message to agent " + receiverId + ": " + message);
    }

    /**
     * retrieveMessage( ) retrieves a message for the specified agent.
     *
     * @param receiverId The ID of the agent to retrieve the message for.
     * @return The message for the agent, or null if no message is available.
     */

    @Override
    public String retrieveMessage(int receiverId) throws RemoteException {
        Queue<String> queue = messageBox.get(receiverId);
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        return queue.poll();
    }
    /**
     * registerAgent( ) registers an agent with a name and ID.
     *
     * @param name The name of the agent.
     * @param agentId The ID of the agent.
     */
    @Override
    public void registerAgent(String name, int agentId) {
        agentDirectory.put(name, agentId);
    }

    /**
     * lookupAgentId( ) looks up the ID of an agent by its name.
     * @param name
     * @return The ID of the agent, or -1 if not found.
     */

    @Override
    public int lookupAgentId(String name) {
        return agentDirectory.getOrDefault(name, -1);
    }
}