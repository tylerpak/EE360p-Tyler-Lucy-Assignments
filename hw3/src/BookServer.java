import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class BookServer{
  public static Inventory inventory = new Inventory();
  public static HashMap<Integer, String[]> requestLog = new HashMap<>();
  static int requestId = 1;
  static int tcpPort;
  static int udpPort;
  static ExecutorService threadPool = Executors.newCachedThreadPool();

  public static void main (String[] args) {
    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];
    tcpPort = 7000;
    udpPort = 8000;

    // parse the inventory file
    try {
      Scanner scan = new Scanner(new File(fileName));

      while (scan.hasNextLine()){
        String line = scan.nextLine();
        Scanner strScan = new Scanner(line);
        String bookTitle = "";
        int bookQuantity = 0;
        while (strScan.hasNext()){
          if (strScan.hasNextInt()){
            bookQuantity = strScan.nextInt();
          }
          else {
            bookTitle += " " + strScan.next(); //removes spaces from title
          }
        }
        inventory.addBook(bookTitle.trim(), bookQuantity);
        strScan.close();
      }
      scan.close();
    } catch (FileNotFoundException e) {}
    
    // handling client connections
    try {
      ServerSocket tcpServer = new ServerSocket(tcpPort);
      DatagramSocket udpServer = new DatagramSocket(udpPort);
      Socket tcpClient;
      
      while ((tcpClient = tcpServer.accept()) != null || udpServer.isConnected()){
        if (tcpClient != null){ // handling TCP requests
          Thread t = new ServerThread(true, inventory, requestLog, requestId, tcpClient, null); //for TCP connections
          t.start();
          threadPool.submit(t);
        }
        else { // handling UDP requests
          Thread t = new ServerThread(false, inventory, requestLog, requestId, null, udpServer); //for UDP connections
          t.start();
          threadPool.submit(t);
        }
      }

      threadPool.shutdown();
      threadPool.awaitTermination(3, TimeUnit.SECONDS);
      tcpServer.close();
      udpServer.close();
    } catch (Exception e) {}
  }

  public static void switchConnection(boolean toTCP, Socket client) throws IOException{
    if (toTCP){
      client.close();
    }
  }
}
