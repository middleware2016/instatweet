import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for the input server
 *
 * @author Alex Delbono
 */
public interface InputServerInterface extends Remote {

    public void stop() throws RemoteException;
}
