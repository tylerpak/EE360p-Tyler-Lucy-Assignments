import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BookServer{

  public static void main (String[] args) {
    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];
    int tcpPort = 7000;
    int udpPort = 8000;

    // server variables (all threads share the same inventory, requestLog, requestId)
    Inventory inventory = new Inventory();
    HashMap<Integer, String[]> requestLog = new HashMap<>();
    AtomicInteger requestId = new AtomicInteger(1);
    ExecutorService threadPool = Executors.newCachedThreadPool();

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
    
    // handle client connections
    try {
      ServerSocket tcpServer = new ServerSocket(tcpPort);
      DatagramSocket udpServer = new DatagramSocket(udpPort);
      tcpServer.setSoTimeout(1000);
      udpServer.setSoTimeout(1000);
      Socket tcpClient;
      DatagramPacket receiveConnection;
      boolean checkTCP = true;
      while (true){
        try {
          while (true){
            if (checkTCP && (tcpClient = tcpServer.accept()) != null){ // handling TCP requests
              Thread t = new ServerThread(inventory, requestLog, requestId, tcpServer, tcpClient); //for TCP connections
              t.start();
              threadPool.submit(t);
            }
            else { // handling UDP requests
              receiveConnection = new DatagramPacket(new byte[1024], 1024);
              udpServer.receive(receiveConnection);
              Thread t = new ServerThread(inventory, requestLog, requestId, udpServer, receiveConnection); //for UDP connections
              t.start();
              threadPool.submit(t);
            }
          }   
        } catch (SocketTimeoutException e) { //this prevents blocking on accept() or receive (will timeout after 1 sec) 
          checkTCP = !checkTCP;
        } 
      }
    } catch (Exception e) {
      threadPool.shutdown();
      try {
        threadPool.awaitTermination(3, TimeUnit.SECONDS);
      } catch (InterruptedException e1) {}
    }
  }
}
