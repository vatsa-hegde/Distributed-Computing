package Mobile;
import java.rmi.*;

public interface AgentInterface extends Remote {
    void receiveMessage(String msg) throws RemoteException;
}