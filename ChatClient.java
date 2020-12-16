import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ChatClient {

  private Integer numberMensagesRecivers;

  public ChatClient() {
    super();
    this.numberMensagesRecivers = 0;
  }

  public static void main(String[] args) {
    String host = (args.length < 1) ? null : args[0];
    System.out.print("Digite o nickname: ");

    Scanner input = new Scanner(System.in);
    String nickname = input.nextLine();

    try {
      // Obtém uma referência para o registro do RMI
      Registry registry = LocateRegistry.getRegistry(host, 6600);

      // Obtém a stub do servidor
      ChatRMI stub = (ChatRMI) registry.lookup("Chat");

      Integer id = stub.register(nickname);

      menu(input, stub, id);

      input.close();
    } catch (Exception ex) {
      System.out.println("Erro ao se conectar com o servidor");
      ex.printStackTrace();
    }
  }

  /**
   * menu de opções do cliente
   *
   * @param input entrada do teclado
   * @param stub  conecção com o servidor
   * @param id    id do cliente no servidor
   */
  private static void menu(Scanner input, ChatRMI stub, Integer id) {
    ChatClient clientChat = new ChatClient();

    int op = 1;
    while (op != 0) {
      System.out.println("Operações possiveis:");
      System.out.println("0 - Sair");
      System.out.println("1 - Enviar o arquivo");
      System.out.println("2 - Receber o arquivo");
      op = input.nextInt();

      switch (op) {
        case 1:
          System.out.println("Nome do arquivo a ser enviado (sem o '.chat'):");
          if (input.hasNext()) {
            input.nextLine();
          }
          String fileName = input.nextLine();

          clientChat.sendMensage(fileName, id, stub);
          break;
        case 2:
          clientChat.reciveMensage(id, stub);
          break;
        default:
          break;
      }

      try {
        Runtime.getRuntime().exec("cls");
      } catch (IOException e) {
      }
    }
  }

  /**
   * Envia uma mensagem para o servidor
   *
   * @param fileName nome do arquivo que a mensagem será lida
   * @param id       id do usuario no servidor
   * @param stub     conecção com o servidor
   */
  private void sendMensage(String fileName, Integer id, ChatRMI stub) {
    File file = new File(fileName.trim() + ".chat");

    try {
      StringBuilder lines = new StringBuilder();

      Scanner scanFile = new Scanner(file);

      // int i = 0;
      while (scanFile.hasNextLine()) {
        lines.append(scanFile.nextLine());
        if (scanFile.hasNextLine()) {
          lines.append("\n");
        }
      }

      scanFile.close();

      try {
        Integer resp = stub.postMensage(id, lines.toString());
        if (resp == null) {
          System.out.println("Erro no servidor durante a escrita no arquivo.");
        }
      } catch (RemoteException e) {
        System.out.println("Erro no envio da mensagem.");
      }
    } catch (FileNotFoundException e) {
      System.out.println("Arquivo " + file.getName() + " não encontrado!");
      System.out.println("Verifique o nome e tente novamente!");
    }
  }

  /**
   * Busca mensagens novas do servidor
   *
   * @param stub conecção com o servidor
   */
  private void reciveMensage(Integer id, ChatRMI stub) {
    try {
      System.out.println(stub.hasNewMensage(this.numberMensagesRecivers));
      while (stub.hasNewMensage(this.numberMensagesRecivers)) {
        RespMensage resp = stub.getMensage(this.numberMensagesRecivers);
        this.numberMensagesRecivers++;

        if (id.compareTo(resp.getClient()) != 0) {
          File myObj = new File(resp.getNickname() + '-' + resp.getIndex() + ".client" + resp.getClient());

          try {
            if (myObj.createNewFile()) {
              System.out.println("File created: " + myObj.getName());
            }

            FileWriter myWriter = new FileWriter(myObj);

            myWriter.write(resp.getMensage());
            myWriter.close();

            System.out.println("Sucesso em escrever no arquivo: " + myObj.getName());
          } catch (IOException e) {
            System.out.println("Erro ao salvar o arquivo: " + myObj.getName());
          }
        } else {
          System.out.println("Sua mensagem pulando...");
        }
      }
    } catch (RemoteException e) {
      System.out.println("Erro durante a conecção com o servidor.");
    }
  }
}
