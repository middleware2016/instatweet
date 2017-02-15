import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for the dispatchers
 *
 * @author Alex Delbono
 */
public interface DispatcherInterface extends Remote {

    public void stop() throws RemoteException;
}
