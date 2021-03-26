import java.util.Scanner;
import java.io.*;
import java.util.*;
import java.net.*;
public class BookClient {
  public static void main (String[] args) throws FileNotFoundException {
    String hostAddress;
    int tcpPort;
    int udpPort;
    int clientId;
    DatagramSocket udpSocket;
    InetAddress address = null;
    try {
      address = InetAddress.getByName("localhost");
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    Socket tcpSocket = new Socket();
    PrintWriter out = null;
    BufferedReader in = null;
    try {
      out = new PrintWriter(tcpSocket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
    } catch (IOException e) {

    }
    udpSocket = null;
    try {
      udpSocket = new DatagramSocket();
    } catch (SocketException e) {
      e.printStackTrace();
    }
    boolean tcp = false; //True for TCP, False for UDP
    boolean setmode = false;
    byte[] buf;

    if (args.length != 2) {
      System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
      System.out.println("\t(1) <command-file>: file with commands to the server");
      System.out.println("\t(2) client id: an integer between 1..9");
      System.exit(-1);
    }

    String commandFile = args[0];
    clientId = Integer.parseInt(args[1]);
    hostAddress = "localhost";
    tcpPort = 7000;// hardcoded -- must match the server's tcp port
    udpPort = 8000;// hardcoded -- must match the server's udp port
    PrintWriter fileOut = new PrintWriter(new File("out_"+ clientId + ".txt"));

    try {
        Scanner sc = new Scanner(new FileReader(commandFile));

        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");

          if (tokens[0].equals("setmode")) {
            String message = tokens[0] + " " + tokens[1];
            setmode = true;
            if(tokens[1].equals("U")) {
              tcp = false;
              buf = message.getBytes();
              DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort);
              udpSocket.send(packet);
              buf = new byte[1024];
              packet = new DatagramPacket(buf, buf.length);
              udpSocket.receive(packet);
              String response = new String(packet.getData(), 0, packet.getLength());
              System.out.println(response);
              fileOut.print(response);
            }
            else if(tokens[1].equals("T")) {
              tcpSocket = new Socket(hostAddress, tcpPort);
              out = new PrintWriter(tcpSocket.getOutputStream(), true);
              in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
              tcp = true;
              out.println(message);
              String response = in.readLine();
              System.out.println(response);
              fileOut.print(response);
            }
          }
          else if (tokens[0].equals("borrow")) {
            String message = cmd.trim();
            if(tcp == true) {
              out.println(message);
              String response = in.readLine();
              System.out.println(response);
              fileOut.print("\n" + response);
            }
            else {
              if(setmode == false) {
                String setDefault = "setmode U";
                buf = setDefault.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort);
                udpSocket.send(packet);
                buf = new byte[1024];
                packet = new DatagramPacket(buf, buf.length);
                udpSocket.receive(packet);
                String response = new String(packet.getData(), 0, packet.getLength());
                System.out.println(response);
                fileOut.print(response);
                setmode = true;
              }
              buf = message.getBytes();
              DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort);
              udpSocket.send(packet);
              buf = new byte[1024];
              packet = new DatagramPacket(buf, buf.length);
              udpSocket.receive(packet);
              String response = new String(packet.getData(), 0, packet.getLength());
              System.out.println(response);
              fileOut.print("\n" + response);
            }
          }
          else if (tokens[0].equals("return")) {
            String message = tokens[0] + " " + tokens[1];
            if(tcp == true) {
              out.println(message);
              String response = in.readLine();
              System.out.println(response);
              fileOut.print("\n" + response);
            }
            else {
              if(setmode == false) {
                String setDefault = "setmode U";
                buf = setDefault.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort);
                udpSocket.send(packet);
                buf = new byte[1024];
                packet = new DatagramPacket(buf, buf.length);
                udpSocket.receive(packet);
                String response = new String(packet.getData(), 0, packet.getLength());
                System.out.println(response);
                fileOut.print(response);
                setmode = true;
              }
              buf = message.getBytes();
              DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort);
              udpSocket.send(packet);
              buf = new byte[1024];
              packet = new DatagramPacket(buf, buf.length);
              udpSocket.receive(packet);
              String response = new String(packet.getData(), 0, packet.getLength());
              System.out.println(response);
              fileOut.print("\n" + response);
            }
          }
          else if (tokens[0].equals("inventory")) {
            String message = tokens[0];
            if(tcp == true) {
              out.println(message);
              String response = in.readLine();
              response = response.replace("___", "\n").trim();
              System.out.println(response);
              fileOut.print("\n" + response);
            }
            else {
              if(setmode == false) {
                String setDefault = "setmode U";
                buf = setDefault.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort);
                udpSocket.send(packet);
                buf = new byte[1024];
                packet = new DatagramPacket(buf, buf.length);
                udpSocket.receive(packet);
                String response = new String(packet.getData(), 0, packet.getLength());
                System.out.println(response);
                setmode = true;
                fileOut.print(response);
              }
              buf = message.getBytes();
              DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort);
              udpSocket.send(packet);
              buf = new byte[1024];
              packet = new DatagramPacket(buf, buf.length);
              udpSocket.receive(packet);
              String response = new String(packet.getData(), 0, packet.getLength());
              response = response.replace("___", "\n").trim();
              System.out.println(response);
              fileOut.print("\n" + response);
            }
          }
          else if (tokens[0].equals("list")) {
            String message = tokens[0] + " " + tokens[1];
            if(tcp == true) {
              out.println(message);
              String response = in.readLine();
              response = response.replace("___", "\n").trim();
              System.out.println(response);
              fileOut.print("\n" + response);
            }
            else {
              if(setmode == false) {
                String setDefault = "setmode U";
                buf = setDefault.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort);
                udpSocket.send(packet);
                buf = new byte[1024];
                packet = new DatagramPacket(buf, buf.length);
                udpSocket.receive(packet);
                String response = new String(packet.getData(), 0, packet.getLength());
                System.out.println(response);
                setmode = true;
                fileOut.print(response);
              }
              buf = message.getBytes();
              DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort);
              udpSocket.send(packet);
              buf = new byte[1024];
              packet = new DatagramPacket(buf, buf.length);
              udpSocket.receive(packet);
              String response = new String(packet.getData(), 0, packet.getLength());
              response = response.replace("___", "\n").trim();
              System.out.println(response);
              fileOut.print("\n" + response);
            }
          }
          else if (tokens[0].equals("exit")) {
            String message = tokens[0];
            if(tcp == true) {
              out.println(message);
            }
            else {
              if(setmode == false) {
                String setDefault = "setmode U";
                buf = setDefault.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort);
                udpSocket.send(packet);
                buf = new byte[1024];
                packet = new DatagramPacket(buf, buf.length);
                udpSocket.receive(packet);
                String response = new String(packet.getData(), 0, packet.getLength());
                System.out.println(response);
                setmode = true;
                fileOut.print(response);
              }
              buf = message.getBytes();
              DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort);
              udpSocket.send(packet);
              packet = new DatagramPacket(buf, buf.length);
            }
            fileOut.flush();
            fileOut.close();
          }
          else {
            System.out.println("ERROR: No such command");
          }
        }
    } catch (FileNotFoundException | SocketException | UnknownHostException e) {
	e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

