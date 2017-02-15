import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for the image handlers
 *
 * @author Alex Delbono
 */
public interface ImageHandlerInterface extends Remote {

    public void stop() throws RemoteException;
}
