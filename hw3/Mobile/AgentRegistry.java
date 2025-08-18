
package Mobile;
import java.rmi.*;

public interface AgentRegistry extends Remote {
    void registerAgent(int id, AgentInterface agent) throws RemoteException;
    AgentInterface getAgent(int id) throws RemoteException;
}