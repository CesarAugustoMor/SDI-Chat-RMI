import java.rmi.*;

public interface ChatRMI extends Remote {
  public String hello() throws RemoteException;

  public Integer register(String nickname) throws RemoteException;

  public Integer postMensage(Integer id, String mensage) throws RemoteException;

  public RespMensage getMensage(Integer index) throws RemoteException;

  public Boolean hasNewMensage(Integer index) throws RemoteException;
}
