package Mobile;
import java.rmi.server.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.*;

public class AgentRegistryImpl extends UnicastRemoteObject implements AgentRegistry {
    private ConcurrentHashMap<Integer, AgentInterface> agents = new ConcurrentHashMap<>();

    public AgentRegistryImpl() throws RemoteException {
        super();
    }

    public void registerAgent(int id, AgentInterface agent) throws RemoteException {
        agents.put(id, agent);
        System.out.println("Agent " + id + " registered.");
    }

    public AgentInterface getAgent(int id) throws RemoteException {
        return agents.get(id);
    }

    public static void main(String[] args) throws Exception {
        int port = 50999;
        AgentRegistryImpl registry = new AgentRegistryImpl();
        LocateRegistry.createRegistry(port);
        Naming.rebind("rmi://localhost:" + port + "/registry", registry);
        System.out.println("AgentRegistry is running on port " + port);
    }
}