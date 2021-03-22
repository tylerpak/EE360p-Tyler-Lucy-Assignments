import java.io.*;
import java.net.*;
import java.util.*;

public class ServerThread extends Thread{
    Socket client;
    DatagramPacket inBuf;
    DatagramPacket outBuf;
    static Inventory inventory;
    boolean tcpMode;
    int requestId;
    HashMap<Integer, String[]> requestLog;
    
    public ServerThread(boolean tcpConnection, Inventory bookInventory, Socket clientSocket, DatagramPacket in, DatagramPacket out){
      if (tcpConnection){
        client = clientSocket;
        tcpMode = true;
      }
      else {
        inBuf = in;
        outBuf = out;
        tcpMode = false;
      }
      
      inventory = bookInventory;
      requestId = 1;
      requestLog = new HashMap<>();
    }

    @Override
    public void run() { //should work for tcp, trying to figure out how to translate to udp
      try {
        Scanner sc;
        PrintWriter pout;

        // if (tcpMode){
          sc = new Scanner(client.getInputStream());
          pout = new PrintWriter(client.getOutputStream());
        // }
        /*
        else { //gonna figure this out later
          sc = new Scanner(inBuf.getData().toString());
          pout = new PrintWriter(System.out);
        }
        */

        while (true){
          String command = sc.nextLine();
          System.out.println("received:" + command);
          Scanner st = new Scanner(command);          
          String tag = st.next();
          if (tag.equals("setmode")){
            tcpMode = st.next().equals("T");
            pout.printf("The communication mode is set to %s\n", tcpMode ? "T" : "U");
            //not sure how to change connections
          } else if (tag.equals("borrow")) {
            String borrower = st.next();
            String title = st.next();
            int result = inventory.borrowBook(title, borrower);

            if (result == -1){
              pout.println("Request Failed - We do not have this book");
            } else if (result == 0){
              pout.println("Request Failed - Book not available");
            } else {
              pout.printf("Your request has been approved, %d %s %s", requestId, borrower, title);
              String[] requestDetails = {title, borrower};
              requestLog.put(requestId, requestDetails);
              requestId++;
            }
          } else if (tag.equals("return")) {
            int returnRequest = st.nextInt();
            String[] requestDetails = requestLog.get(returnRequest);

            if (requestDetails == null){
              pout.printf("%d not found, no such borrow record\n", returnRequest);
            }
            else {
              inventory.returnBook(requestDetails[0], requestDetails[1]);
              requestLog.remove(returnRequest);
              pout.printf("%d is returned\n", returnRequest);
            }          
          } else if (tag.equals("list")) {
            String borrower = st.next();
            List<String> borrowedTitles = inventory.borrowerList(borrower);
            List<String []> borrowerLog = reverseMapRequestLog(borrowedTitles, borrower);

            if (borrowerLog.size() > 0){
              for (int i = 0; i < borrowerLog.size(); i++){
                pout.printf("%s %s\n", borrowerLog.get(i)[0], borrowerLog.get(i)[1]);
              }
            }
            else {
              pout.printf("No record found for %s\n", borrower);
            }
          } else if (tag.equals("inventory")) {
            String inventoryString = inventory.printInventory();
            pout.println(inventoryString);
          } else if (tag.equals("exit")){
            String exitMessage = inventory.printInventory();
            client.close();
            PrintWriter fileOut = new PrintWriter(new File("inventory.txt"));
            fileOut.println(exitMessage);
            fileOut.flush();
            fileOut.close();
            st.close();
            pout.close();
            sc.close();
            break;
          }
          st.close();
          pout.flush();
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
  }