import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerThread extends Thread{
    byte buf1[] = new byte[1024];
    byte buf2[] = new byte[1024];
    Socket client;
    static DatagramSocket udpServer;
    static ServerSocket tcpServer;
    DatagramPacket inBuf;
    DatagramPacket outBuf;
    static Inventory inventory;
    boolean tcpMode;
    static AtomicInteger requestId = new AtomicInteger();
    static HashMap<Integer, String[]> requestLog;
    
    public ServerThread(Inventory bookInventory, HashMap<Integer, String[]> reqLog, int requestNum, ServerSocket serverSocket, Socket clientSocket){
      client = clientSocket;
      tcpServer = serverSocket;
      tcpMode = true;
      inventory = bookInventory;
      requestId.set(requestNum);
      requestLog = reqLog;
    }

    public ServerThread(Inventory bookInventory, HashMap<Integer, String[]> reqLog, int requestNum, DatagramSocket serverSocket, DatagramPacket receivePacket){
      udpServer = serverSocket;
      inBuf = receivePacket;
      outBuf = new DatagramPacket(buf2, 1024, inBuf.getAddress(), inBuf.getPort());
      tcpMode = false;
      inventory = bookInventory;
      requestId.set(requestNum);
      requestLog = reqLog;
    }

    @Override
    public void run() {
      try {
        Scanner sc;
        PrintWriter pout;

        if (tcpMode){ //sets input/output for TCP connection
          sc = new Scanner(client.getInputStream());
          pout = new PrintWriter(client.getOutputStream());
        }

        while (true){
          if (!tcpMode){ //blocks for next udp message and reads command
            udpServer.receive(inBuf);
            sc = new Scanner(new String(inBuf.getData()));
          }

          String message = "";
          String command = sc.nextLine();
          // System.out.println("received:" + command);
          Scanner st = new Scanner(command);          
          String tag = st.next();

          if (tag.equals("setmode")){ //let client side handle setmode?
            tcpMode = st.next().equals("T");
            message = String.format("The communication mode is set to %s\n", tcpMode ? "T" : "U");
          } else if (tag.equals("borrow")) {
            String borrower = st.next();
            String title = st.next();
            int result = inventory.borrowBook(title, borrower);

            if (result == -1){
              message = "Request Failed - We do not have this book";
            } else if (result == 0){
              message = "Request Failed - Book not available";
            } else {
              message = String.format("Your request has been approved, %d %s %s", requestId, borrower, title);
              String[] requestDetails = {title, borrower};
              requestLog.put(requestId.get(), requestDetails);
              requestId.incrementAndGet();
            }
          } else if (tag.equals("return")) {
            int returnRequest = st.nextInt();
            String[] requestDetails = requestLog.get(returnRequest);

            if (requestDetails == null){
              message = String.format("%d not found, no such borrow record\n", returnRequest);
            }
            else {
              inventory.returnBook(requestDetails[0], requestDetails[1]);
              requestLog.remove(returnRequest);
              message = String.format("%d is returned\n", returnRequest);
            }          
          } else if (tag.equals("list")) {
            String borrower = st.next();
            List<String> borrowedTitles = inventory.borrowerList(borrower);
            List<String []> borrowerLog = reverseMapRequestLog(borrowedTitles, borrower);

            if (borrowerLog.size() > 0){
              for (int i = 0; i < borrowerLog.size(); i++){
                message += String.format("%s %s\n", borrowerLog.get(i)[0], borrowerLog.get(i)[1]);
              }
            }
            else {
              message = String.format("No record found for %s\n", borrower);
            }
          } else if (tag.equals("inventory")) {
            message = inventory.printInventory();
          } else if (tag.equals("exit")){
            writeInventory();
            st.close();
            if (tcpMode) {
              pout.close();
              client.close();
            }
            sc.close();
            break;
          }

          if (message.length() > 0){
            if (tcpMode){
              pout.println(message);
              pout.flush();
            }
            else {
              outBuf.setData(message.getBytes());
              udpServer.send(outBuf);
            }
          }
          st.close();
        } 
      } catch (IOException e) {
        System.err.println(e);
      }
    }

    /**
     * Returns all unreturned requests associated with borrower
     * @param borrowedTitles titles that have been borrowed and not returned
     * @param borrower borrower's name
     * @return List of all unreturned requests associated with borrower
     */
    public List<String []> reverseMapRequestLog(List<String> borrowedTitles, String borrower){
      List<String []> borrowerLog = new LinkedList<>();

      for (int i = 0; i < borrowedTitles.size(); i++){
        String title = borrowedTitles.get(i);
        String[] requestEntry = {title, borrower};

        if (requestLog.containsValue(requestEntry)){
          for (int j = 0; j < requestLog.size(); j++){
            if (requestLog.get(j)[0].equals(title) && requestLog.get(j)[1].equals(borrower)){
              String[] borrowerEntry = {"" + j, title};
              borrowerLog.add(borrowerEntry);
            }
          }
        }
      }
      return borrowerLog;
    }

    /**
     * Creates and writes the current inventory in inventory.txt
     * @throws IOException inventory.txt is not found and cannot be created
     */
    public synchronized void writeInventory() throws IOException{
      String exitMessage = inventory.printInventory();
      PrintWriter fileOut = new PrintWriter(new File("inventory.txt"));
      fileOut.println(exitMessage);
      fileOut.flush();
      fileOut.close();
    }
  }