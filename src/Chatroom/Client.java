package Chatroom;

import View.Clientview;
import User.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Scanner;

/**
 *  This Class is the Client for this chat system.
 *  It extends clientView to use a GUI window to control this system.
 *  This Class contains all the commands about client and its connection to both chat system server or another P2P client.
 */
public class Client extends Clientview {

    //Here is the basic components for client
    private byte[] inBuff = new byte[4096]; //an array to contain bytes
    private DatagramPacket inPacket = new DatagramPacket(inBuff, inBuff.length);    //packets for receive message from another client
    private DatagramPacket outPacket = null;    //packets for send message to another
    private DatagramSocket datagramSocket = null;   //A datagram socket for P2P connection
    private User user = null;   //initialize a user
    private Socket socket;  //a Socket for client - server connection
    private PrintWriter writer;    //Output stream from server
    private BufferedReader reader; //Input stream to server
    private boolean isConnected = false;    //decide whether client has connected to server
    private HashMap<String, User> users = new HashMap<>();  //Hashmap to contain information for online users

    /**
     * constructor for this Class
     */
    public Client() {
        /**
         *  a listener of frame to set action after frame being closed
         */
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isConnected) {
                    disconnect();
                }
                System.exit(0);
            }
        });

        /**
         *  a listener of connect button to set a connection between client and server
         */
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isConnected) {
                    connect();
                }
            }
        });

        /**
         *  a listener of disconnect button to disconnect the connection to server
         */
        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isConnected) {
                    disconnect();
                }
            }
        });

        /**
         *  a listener of online userlist to change some GUI when selecting certain option
         */
        userList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int index = userList.getSelectedIndex();
                if (index < 0) {
                    return;
                }

                if (index == 0) {
                    messageLabel.setText("TO: ALL PEOPLE");
                    messageField.setEnabled(true);
                    sendButton.setEnabled(true);
                    kickButton.setEnabled(false);
                    statButton.setEnabled(false);
                } else {
                    String name = listModel.getElementAt(index);
                    String temp[] = name.split(":");
                    String id = temp[1];
                    String onlineuser = temp[0];
                    if (users.containsKey(id)) {
                        messageLabel.setText("TO: " + onlineuser);
                        messageField.setEnabled(true);
                        sendButton.setEnabled(true);
                        kickButton.setEnabled(true);
                        statButton.setEnabled(true);
                        if (Integer.parseInt(id) == user.getId()){
                            messageLabel.setText("TO: " + onlineuser + "(myself)");
                            messageField.setEnabled(false);
                            sendButton.setEnabled(false);
                            kickButton.setEnabled(false);
                            statButton.setEnabled(true);
                        }
                    } else {
                        messageLabel.setText("TO: ALL PEOPLE");
                        messageField.setEnabled(true);
                        sendButton.setEnabled(true);
                        kickButton.setEnabled(false);
                        statButton.setEnabled(false);
                    }
                }
            }
        });

        /**
         *  a listener of send button to send messages
         */
        sendButton.addActionListener(new MyActionListener());

        /**
         *  a listener of send text area to send messages
         */
        messageField.addActionListener(new MyActionListener());

        /**
         *  a listener of stats button to list commands
         */
        statButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = userList.getSelectedIndex();
                if (index < 0 || index == 0) {
                    showError("Please Select a user !!!");
                    return;
                }else {
                    String name = listModel.getElementAt(index);
                    String temp[] = name.split(":");
                    String id = temp[1];
                    send("{STATS - " + id + "}");
                    addMessage("<-- User's Commands are below -->");
                }
            }
        });

        /**
         *  a listener of kick button to kick off one user
         */
        kickButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = userList.getSelectedIndex();
                if (index < 0 || index == 0) {
                    showError("Please Select a user !!!");
                    return;
                }else {
                    String name = listModel.getElementAt(index);
                    String temp[] = name.split(":");
                    String id = temp[1];
                    send("{KICK - " + id + "}");
                }
            }
        });

        /**
         *  a listener of refresh button to refresh the online userlist
         */
        listButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listModel.removeAllElements();
                addMessage("<-- online users are below -->");
                send("{LIST}");
                listModel.addElement("ALL PEOPLE");
            }
        });

    }

    /**
     *  a method to build a connection between client and srver
     */
    public void connect() {

        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        }catch (Exception e) {
            showError("The port number must be an Integer!!!");
            return;
        }

        if (port < 1024 || port > 65535) {
            showError("Port number must inside the range (1024 ~ 65535) !!!");
            return;
        }

        String name = nameField.getText().trim();
        if (name == null || name.equals("")) {
            showError("Name cannot be empty!!!");
            return;
        }

        String id = IDField.getText().trim();
        if (id == null || id.equals("")) {
            showError("ID cannot be empty!!!");
            return;
        }else {
            for (String uid : users.keySet()) {
                if (uid.equals(id)) {
                    showError("This ID is already existed, please change!!!");
                    return;
                }
            }
        }

        String ip = IPField.getText().trim();
        if (ip == null || ip.equals("")) {
            showError("IP cannot be empty!!!");
            return;
        }

        //here create the connection between user and server
        try {
            listModel.addElement("ALL PEOPLE");

            InetAddress address = InetAddress.getLocalHost();
            user = new User(Integer.parseInt(IDField.getText().trim()), address.getHostAddress(), nameField.getText().trim());
            socket = new Socket(IPField.getText().trim(), Integer.parseInt(portField.getText().trim()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());
            writer.println(user.getId() + " " + user.getIp() + " " + user.getName());
            writer.flush();

            listModel.addElement(nameField.getText().trim()+ ":" +IDField.getText().trim());
            users.put(IDField.getText().trim(), user);
            addMessage("Connect Successfully!!!");
            ClientUI(true);
            isConnected = true;
        }catch(Exception e) {
            isConnected = false;
            addMessage("Connect Failed!!!");
            listModel.removeAllElements();
            e.printStackTrace();
            return;
        }

        new ReceiveFromClient().start();
        new ReceiveFromServer().start();
    }

    /**
     *  a method to disconnect connection between user and server
     */
    public void disconnect() {

        listModel.removeAllElements();
        writer.println("{STOP}");
        writer.flush();
        users.clear();
        ClientUI(false);
        isConnected = false;
        datagramSocket.close();
        messageLabel.setText("TO: ALL PEOPLE");

    }

    /**
     *  a method to send messages
     * @param command   messages to be send
     */
    public void send(String command) {

        if (!isConnected) {
            showError("Not connect with Server!!!");
            return;
        }

        //Judge the kinds of different message
        //if the message is private message to another user, it should user P2P connection to send message
        if(command.matches("\\{BROADCAST - \\{.+\\}\\}") || command.equals("{STOP}") || command.equals("{LIST}") || command.matches("\\{KICK - \\d+\\}") || command.matches("\\{STATS - \\d+\\}"))  {
            writer.println(command);
            writer.flush();
        }else if (command.matches("\\{MESSAGE - \\d+ - \\{.+\\}\\}")) {
            int index = userList.getSelectedIndex();
            String name = listModel.getElementAt(index);
            String tem[] = name.split(":");
            String id = tem[1];
            writer.println(command);
            writer.flush();

            try {
                DatagramSocket datagramSocket = new DatagramSocket();
                outPacket = new DatagramPacket(new byte[0], 0, InetAddress.getByName(users.get(id).getIp()), Integer.parseInt(portField.getText().trim()));
                String temp[] = command.split("-");
                String content = temp[2].substring(2, temp[2].length()-2);
                String message = user.getId() + "(PRIVATE): " + content;
                byte[] buff = message.getBytes();
                outPacket.setData(buff);
                datagramSocket.send(outPacket);
                datagramSocket.close();
                addMessage("TO: " + name + ": " + content);
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *  an action listener for send button and send text area
     */
    private class MyActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int index = userList.getSelectedIndex();

            //Here transform the message into certain form to be able to be send out
            if (index < 0) {
                showError("Please Select one option to send message!!!");
            } else if (index == 0 ) {
                String message = messageField.getText();
                send("{BROADCAST - {" + message + "}}");
            } else if (index > 0) {
                String id = listModel.getElementAt(index);
                String temp[] = id.split(":");
                String touser = temp[1];
                String message = messageField.getText();
                if (message.equals("") || message.equals(null)) {
                    showError("Cannot send empty Message!!!");
                }else{
                    if (touser.equals("ALL PEOPLE")) {
                        send("{BROADCAST - {" + message + "}}");
                    }else {
                        if (!touser.equals(String.valueOf(user.getId()))) {
                            send("{MESSAGE - " + touser + " - {" + message + "}}");
                        }else {
                            showError("Cannot send message to myself!!!");
                        }
                    }
                }
            }

            //reset the message area
            messageField.setText("");
        }
    }

    /**
     *  This Class is to create the a thread to receive message from server
     */
    private class ReceiveFromServer extends Thread {

        /**
         *  reset the run method to receive messages from server
         */
        @Override
        public void run() {
            while (isConnected) {
                try {
                    receive();
                }catch (IOException e) {
                    isConnected = false;
                    e.printStackTrace();
                }
            }
        }

        /**
         *  This method is to receive messages from server and act according to different commands
         * @throws IOException  for socket connection exception
         */
        public void receive() throws IOException {
            String message = reader.readLine();
            if (message == null || message.equals("")){
                addMessage("");
            }
            if (message.matches("\\d+\\|\\d+\\.\\d+\\.\\d+\\.\\d+\\|+\\w*")) {
                addMessage(message);
                String[] temp = message.split("\\|");
                String id = temp[0];
                String ip = temp[1].substring(0, temp[1].length());
                String name = temp[2];
                User u = new User(Integer.parseInt(id), ip, name);
                users.put(id, u);
                listModel.addElement(name + ":" + id);
            } else if (message.matches("\\d* has been kicked off!!!")) {
                addMessage(message);
                String[] temp = message.split("\\s");
                String id = temp[0];
                if (Integer.parseInt(id) == user.getId()) {
                    disconnect();
                }
            } else {
                addMessage(message);
            }
        }
    }

    /**
     *  This Class is to create the a thread to receive message from another user through P2P connection
     */
    private class ReceiveFromClient extends Thread {

        /**
         *  constructor for this thread to initialize the P2P receiver
         */
        public ReceiveFromClient() {
            try {
                datagramSocket = new DatagramSocket(Integer.parseInt(portField.getText().trim()));
            }catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         *  reset the run method to receive messages from another user through P2P connection
         */
        @Override
        public void run() {
            while (isConnected) {
                try {
                    receive();
                }catch (IOException e) {
                    isConnected = false;
                    e.printStackTrace();
                }
            }
        }

        /**
         *  receiver messages from another P2P client
         * @throws IOException  for datagramsocket connection exception
         */
        public void receive() throws IOException{
            datagramSocket.receive(inPacket);
            addMessage(new String(inBuff, 0, inPacket.getLength()));
        }

    }

    /**
     *  Display error messages inside a JOptionpanel
     * @param msg   error messages
     */
    private void showError(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     *  Add the message into the message text area
     * @param msg   messages to be displayed
     */
    private void addMessage(String msg) {
        messageTextArea.append(msg + "\r\n");
    }

    public static void main(String[] args) throws IOException {
        new Client();
    }

}
