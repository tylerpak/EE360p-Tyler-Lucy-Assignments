import java.io.*;
import java.net.*;
import java.util.Scanner;

public class BookServer{
  public static Inventory inventory = new Inventory();
  public static void main (String[] args) {
    int tcpPort;
    int udpPort;

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
      
      byte[] inBuf = new byte[1024];
      byte[] outBuf = new byte[1024];
      DatagramPacket rPacket;
      DatagramPacket sPacket;
      
      while ((tcpClient = tcpServer.accept()) != null || udpServer.isConnected()){
        if (tcpClient != null){
          Thread t = new ServerThread(true, inventory, tcpClient, null, null); //for TCP connections
          t.start();
        }
        else { 
          rPacket = new DatagramPacket(inBuf, 1024);
          sPacket = new DatagramPacket(outBuf, 1024, rPacket.getAddress(), udpPort);
          udpServer.receive(rPacket);
          udpServer.send(sPacket);
          Thread t = new ServerThread(false, inventory, null, rPacket, sPacket);
          t.start();
        }
      }

      tcpServer.close();
      udpServer.close();
    } catch (IOException e) {}
  }
}
