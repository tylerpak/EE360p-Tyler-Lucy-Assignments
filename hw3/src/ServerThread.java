import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ServerThread extends Thread{
    byte buf1[] = new byte[1024];
    byte buf2[] = new byte[1024];
    Socket client;
    static DatagramSocket udpServer; //DatagramSocket is thread-safe in Java
    static ServerSocket tcpServer;
    DatagramPacket inBuf;
    DatagramPacket outBuf;
    static Inventory inventory;
    boolean tcpMode;
    static AtomicInteger requestId;
    static HashMap<Integer, String[]> requestLog;
    static ReentrantLock requestLogLock = new ReentrantLock();
    
    public ServerThread(Inventory bookInventory, HashMap<Integer, String[]> reqLog, AtomicInteger requestNum, ServerSocket serverSocket, Socket clientSocket){
      client = clientSocket;
      tcpServer = serverSocket;
      tcpMode = true;
      inventory = bookInventory;
      requestId = requestNum;
      requestLog = reqLog;
    }

    public ServerThread(Inventory bookInventory, HashMap<Integer, String[]> reqLog, AtomicInteger requestNum, DatagramSocket serverSocket, DatagramPacket receivePacket){
      udpServer = serverSocket;
      inBuf = receivePacket;
      tcpMode = false;
      inventory = bookInventory;
      requestId = requestNum;
      requestLog = reqLog;
    }

    @Override
    public void run() {
      try {
        Scanner sc = new Scanner(System.in);
        PrintWriter pout = new PrintWriter(System.out);

        if (tcpMode){ //sets input/output for TCP connection
          sc.close();
          sc = new Scanner(client.getInputStream());
          pout = new PrintWriter(client.getOutputStream());
        }

        while (true){
          if (!tcpMode){ //reading UDP message
            sc.close();
            udpServer.receive(inBuf);
            outBuf = new DatagramPacket(new byte[1024], 1024, inBuf.getAddress(), inBuf.getPort());
            sc = new Scanner(new String(inBuf.getData(), inBuf.getOffset(), inBuf.getLength()));
          }

          String message = "";
          String command = sc.nextLine();
          Scanner st = new Scanner(command);
          String tag = st.next();

          if (tag.equals("setmode")){ //let client side handle setmode
            tcpMode = st.next().equals("T");
            message = String.format("The communication mode is set to %s", tcpMode ? "TCP" : "UDP");
          } else if (tag.equals("borrow")) {
            String borrower = st.next();
            String title = st.nextLine().trim();
            int result = inventory.borrowBook(title, borrower);

            if (result == -1){
              message = "Request Failed - We do not have this book";
            } else if (result == 0){
              message = "Request Failed - Book not available";
            } else {
              message = String.format("Your request has been approved, %d %s %s", requestId.get(), borrower, title);
              String[] requestDetails = {title, borrower};
              requestLogLock.lock();
              requestLog.put(requestId.get(), requestDetails);
              requestLogLock.unlock();
              requestId.incrementAndGet();
            }
          } else if (tag.equals("return")) {
            int returnRequest = st.nextInt();
            
            if (!requestLog.containsKey(returnRequest)){
              message = String.format("%d not found, no such borrow record", returnRequest);
            }
            else {
              String[] requestDetails = requestLog.get(returnRequest);
              inventory.returnBook(requestDetails[0], requestDetails[1]);
              requestLogLock.lock();
              requestLog.remove(returnRequest);
              requestLogLock.unlock();
              message = String.format("%d is returned", returnRequest);
            }          
          } else if (tag.equals("list")) {
            String borrower = st.next();
            List<String> borrowedTitles = inventory.borrowerList(borrower);
            List<String []> borrowerLog = reverseMapRequestLog(borrowedTitles, borrower);

            if (borrowerLog.size() > 0){
              for (int i = 0; i < borrowerLog.size(); i++){
                message += String.format("%s %s___", borrowerLog.get(i)[0], borrowerLog.get(i)[1]);
              }
            }
            else {
              message = String.format("No record found for %s", borrower);
            }
          } else if (tag.equals("inventory")) {
            message = inventory.printInventory();
          } else if (tag.equals("exit")){
            writeInventory();
            if (tcpMode) {
              st.close();
              sc.close();
              pout.close();
              client.close();
              break;
            }
          }

          message = message.trim();
          if (message.length() > 0){
            if (tcpMode){
              pout.println(message);
              pout.flush();
            }
            else {
              byte packet[] = message.getBytes();
              outBuf.setData(packet, 0, packet.length);
              udpServer.send(outBuf);
            }
          }
          st.close();
        } 
      } catch (Exception e) {}
    }

    /**
     * Returns all unreturned requests associated with borrower
     * @param borrowedTitles titles that have been borrowed and not returned
     * @param borrower borrower's name
     * @return List of all unreturned requests associated with borrower
     */
    public List<String []> reverseMapRequestLog(List<String> borrowedTitles, String borrower){
      List<String []> borrowerLog = new LinkedList<>();
      Set<Integer> addedRequests = new HashSet<>();

      for (int i = 0; i < borrowedTitles.size(); i++){
        String title = borrowedTitles.get(i);
        for (int key : requestLog.keySet()){
          if (requestLog.get(key)[0].equals(title) && requestLog.get(key)[1].equals(borrower) && !addedRequests.contains(key)){
            String[] borrowerEntry = {Integer.toString(key), title};
            borrowerLog.add(borrowerEntry);
            addedRequests.add(key);
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
      String exitMessage = inventory.printInventory().replace("___", "\n").trim();
      PrintWriter fileOut = new PrintWriter(new File("inventory.txt"));
      fileOut.print(exitMessage);
      fileOut.flush();
      fileOut.close();
    }
  }