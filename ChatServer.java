import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class ChatServer implements ChatRMI {
  private int numberMensages;
  private ArrayList<String> nicknames;

  public ChatServer() {
    super();
    this.nicknames = new ArrayList<String>();
    this.numberMensages = 0;
  }

  public static void main(String[] args) {
    try {
      // Instancia o objeto servidor e a sua stub
      ChatServer server = new ChatServer();
      ChatRMI stub = (ChatRMI) UnicastRemoteObject.exportObject(server, 0);
      // Registra a stub no RMI Registry para que ela seja obtAida pelos clientes
      Registry registry = LocateRegistry.createRegistry(6600);
      // Registry registry = LocateRegistry.getRegistry(9999);
      registry.bind("Chat", stub);
      System.out.println("Servidor pronto");
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Metodo de teste que não faz nada
   */
  public String hello() throws RemoteException {
    System.out.println("Executando hello()");
    return "Hello!!!";
  }

  /**
   * Grava uma mensagem de um usuario registrado
   *
   * @param id      id do usuario no servidor
   * @param mensage mensagem a ser grava
   * @return retorna {@code null} se ocorer erro na leitura/escrita do arquivo
   */
  public Integer postMensage(Integer id, String mensage) throws RemoteException {

    File myObj = new File(this.nicknames.get(id) + '-' + this.numberMensages + ".serv");

    try {
      if (myObj.createNewFile()) {
        System.out.println("File created: " + myObj.getName());
      }

      FileWriter myWriter = new FileWriter(myObj);

      myWriter.write(mensage);
      myWriter.close();
      System.out.println("Successfully wrote to the file: " + myObj.getName());
    } catch (IOException e) {
      System.out.println("Erro durante a criação/escrita do arquivo: " + myObj.getName());
      return null;
    }

    return this.numberMensages++;
  }

  /**
   * Retorna a mensagem corespondente ao indice passado
   *
   * @param index indice da mensagem a ser buscada
   * @return retorna {@code null} se o arquivo não foi encontrado
   */
  public RespMensage getMensage(Integer index) throws RemoteException {

    // Creating a File object for actual directory
    File directoryPath = new File(".");

    FilenameFilter textFilefilter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        String lowercaseName = name.toLowerCase();
        if (lowercaseName.endsWith(".serv")) {
          return true;
        } else {
          return false;
        }
      }
    };

    // List of all the .serv files
    String filesList[] = directoryPath.list(textFilefilter);

    String nickname = null;
    Integer indexFile = 0;
    String pathname = null;

    for (String file : filesList) {
      String[] partsName = file.split("-");
      String tmp = partsName[1];

      String[] partsName2 = tmp.split(".serv");

      String tmp2 = partsName2[0];

      indexFile = Integer.valueOf(tmp2);

      if (indexFile.compareTo(index) == 0) {
        nickname = partsName[0];
        pathname = file;
        break;
      }
    }

    if (pathname == null || nickname == null) {
      System.out.println("Arquivo não encontrado!");

      return null;
    }

    File file = new File(pathname);

    try {
      StringBuilder contentFile = new StringBuilder();

      FileReader fileReader = new FileReader(file);

      int j;

      while ((j = fileReader.read()) != -1) {
        contentFile.append((char) j);
      }

      fileReader.close();

      RespMensage respMensage = new RespMensage(nickname, contentFile.toString(), index,
          this.nicknames.indexOf(nickname));

      return respMensage;

    } catch (IOException e) {
      System.out.println("Arquivo não encontrado!");
      return null;
    }
  }

  /**
   * Registra um usuario no servidor
   *
   * @param nickname nome do usuario
   * @return id do usuario no servidor
   */
  public Integer register(String nickname) throws RemoteException {

    this.nicknames.add(nickname);

    return this.nicknames.indexOf(nickname);
  }

  /**
   * Verifica se existem novas mensagens ainda não carregadas
   *
   * @param index indece da mensagem a se ser buscada
   * @return retorn {@code true} se exite novas mensagens ou {@code false} se não
   *         existe novas mensagens
   */
  public Boolean hasNewMensage(Integer index) throws RemoteException {
    return (this.numberMensages > index);
  }
}
