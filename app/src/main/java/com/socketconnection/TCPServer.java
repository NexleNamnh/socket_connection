package com.socketconnection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;


public class TCPServer extends Thread {
    private static final int SERVER_PORT = 5657;
    private ServerSocket serverSocket;
    private Socket client = null;
    private boolean running = false;
    private PrintWriter mOut;
    private OnMessageReceived messageListener;


    /**
     * Constructor of the class
     *
     * @param messageListener listens for the messages
     * @author Prashant Adesara
     */
    public TCPServer(OnMessageReceived messageListener) {
        this.messageListener = messageListener;
    }

    /**
     * Method to send the messages from server to client
     *
     * @param message the message sent by the server
     * @author Prashant Adesara
     */
    public void sendMessage(String message) {
        try {
            if (mOut != null && !mOut.checkError()) {
                System.out.println(message);
                // Here you can connect with database or else you can do what you want with static message
                mOut.println(message);
                mOut.flush();
            }
        } catch (Exception e) {
        }
    }

    /**
     * @author Prashant Adesara
     */
    @Override
    public void run() {
        super.run();
        running = true;
        try {
            System.out.println("PA: Connecting...");

            // create a server socket. A server socket waits for requests to
            // come in over the network.
            serverSocket = new ServerSocket(SERVER_PORT);

            // create client socket... the method accept() listens for a
            // connection to be made to this socket and accepts it.
            try {
                client = serverSocket.accept();
                System.out.println("S: Receiving...");
                // sends the message to the client
                mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                System.out.println("PA: Sent");
                System.out.println("PA: Connecting Done.");
                if (messageListener != null) {
                    messageListener.onServerStart(true);
                }
                // read the message received from client
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                sendMessage("Server connected with Android Client now you can chat with socket server.");

                // in this while we wait to receive messages from client (it's an infinite loop)
                // this while it's like a listener for messages
                while (running) {
                    String message = in.readLine();
                    if (message != null && messageListener != null) {
                        // call the method messageReceived from ServerBoard class
                        messageListener.messageReceived(message);
                    }
                }
            } catch (Exception e) {
                System.out.println("PA: Error: " + e.getMessage());
                e.printStackTrace();
                if (messageListener != null) {
                    messageListener.onServerStart(false);
                }
            } finally {
                client.close();
                System.out.println("PA: Done.");
            }
        } catch (Exception e) {
            System.out.println("PA: Error");
            e.printStackTrace();
            if (messageListener != null) {
                messageListener.onServerStart(false);
            }
        }

    }

    /**
     * Declare the interface. The method messageReceived(String message) will
     *
     * @author Prashant Adesara
     *         must be implemented in the ServerBoard
     *         class at on startServer button click
     */
    public interface OnMessageReceived {
        public void messageReceived(String message);
        void onServerStart(boolean success);
    }


    public static String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

    public static int getPort() {
        return SERVER_PORT;
    }

}
