package Chatroom;

import User.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  This Class is the server for this chat system
 *  There is no GUI and interaction between users and server
 *  When this chat system is running, this server will not be changed any more
 */
public class Server {

    //basic components for server
    private ServerSocket serverSocket;//serversocket to create server
    private ConcurrentHashMap<User, ClientThread> clientThreads;//Hashmap to store different threads defined by user
    private Socket socket;
    private final int PORT = 20000;//set port to send and receive messages

    /**
     *  constructor to initialize the server
     */
    public Server() {
        new ServerThread().start();
    }

    /**
     * server thread to achieve server functions
     */
    private class ServerThread extends Thread{

        private BufferedReader reader;//input stream to receive message
        private PrintWriter writer;//output stream to send message

        public ServerThread() {
            try {
                clientThreads = new ConcurrentHashMap<User, ClientThread>();
                serverSocket = new ServerSocket(PORT);
            }catch(BindException e) {
                e.printStackTrace();
            }catch(IOException e) {
                e.printStackTrace();
            }
        }

        /**
         *  reset the run method
         *  when a new user has been created, it will broadcast to any other clients
         */
        public void run() {
            while(true) {
                try {
                    socket = serverSocket.accept();
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new PrintWriter(socket.getOutputStream());

                    ClientThread clientThread = new ClientThread(socket);
                    User user = clientThread.getUser();

                    clientThreads.put(user, clientThread);
                    clientThread.start();

                    for (Map.Entry<User, ClientThread> entry : clientThreads.entrySet()) {
                        String message = user.toString() + " is connected! ";
                        entry.getValue().sendMessage(message);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     *  Client thread to receive and send messages to client
     *  It also store all the commands made by certain client
     */
    private class ClientThread extends Thread{
        private Socket socket;
        private User user;
        private BufferedReader reader;
        private PrintWriter writer;
        private volatile boolean isRunning = true;
        private LinkedList<String> commands = new LinkedList<String>();//Store the commands made by certain client

        public ClientThread(Socket socket) {
            this.socket = socket;

            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());
                String line = reader.readLine();
                String temp[] = line.split("\\s+");
                user = new User(Integer.parseInt(temp[0]), temp[1], temp[2]);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        /**
         *  reset the run method
         *  According to different kinds of messages, this method transform them into their suitable commands and return them to clients
         */
        public void run() {
            while(isRunning) {
                try {
                    String command = reader.readLine();
                    if(command.matches("\\{BROADCAST - \\{.+\\}\\}")) {//BROADCAST - {CONTENT}
                        commands.add(command);
                        for (Map.Entry<User, ClientThread> entry : clientThreads.entrySet()) {
                            String content = command.substring(14, command.length()-2);

                            entry.getValue().sendMessage(user.getId() + "(BROADCAST): " + content);
                        }
                    } else if(command.equals("{STOP}")) {//STOP     if a client stop, close its thread and connection
                        for (Map.Entry<User, ClientThread> entry : clientThreads.entrySet()) {
                            System.out.println("STOP " + user.getId() + " is disconnected! ");
                            String message = user.getId() + "|" + user.getName() + " is disconnected! ";
                            entry.getValue().sendMessage(message);
                        }
                        clientThreads.remove(user);
                        close();
                    } else if(command.equals("{LIST}")) {//LIST     list out clients online currently
                        commands.add(command);
                        for (Map.Entry<User, ClientThread> entry : clientThreads.entrySet()) {
                            String message = entry.getKey().toString();
                            sendMessage(message);
                        }
                    } else if (command.matches("\\{KICK - \\d+\\}")) {//KICK - ID   kick one client off line through its id
                        commands.add(command);
                        String message[] = command.split("-");
                        String id = message[1].substring(1, message[1].length()-1);
                        String kick = id + " has been kicked off!!!";

                        for (Map.Entry<User, ClientThread> entry : clientThreads.entrySet()) {
                            entry.getValue().sendMessage(kick);

                            if (entry.getKey().getId() == Integer.parseInt(id)) {
                                clientThreads.remove(entry.getKey());
                            }
                        }
                    } else if(command.matches("\\{STATS - \\d+\\}")) {//STATS - ID  return all the commands made by certain client
                        commands.add(command);
                        String message[] = command.split("-");
                        String id = message[1].substring(1, message[1].length()-1);
                        for (Map.Entry<User, ClientThread> entry : clientThreads.entrySet()) {
                            if (entry.getKey().getId() == Integer.parseInt(id)) {
                                LinkedList<String> list = entry.getValue().getCommands();
                                for (String s : list) {
                                    sendMessage(s);
                                }
                            }
                        }
                    } else if (command.matches("\\{MESSAGE - \\d+ - \\{.+\\}\\}")) {//store the MESSAGE - ID - {CONTENT} command. Do not attend the process of send messages to another client (P2P)
                        String message[] = command.split("-");
                        String id = message[1].substring(1, message[1].length()-1);
                        String secret = "{MESSAGE - " + id + "{SECRET}}";
                        commands.add(secret);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * CLose the connection
         * @throws IOException  connection exception
         */
        public void close() throws IOException {
            isRunning = false;
            this.reader.close();
            this.writer.close();
            this.socket.close();
        }

        /**
         * Send messages out
         * @param message messages to be sent
         */
        public void sendMessage(String message) {
            writer.println(message);
            writer.flush();
        }

        /**
         *
         * @return user of this thread
         */
        public User getUser() {
            return user;
        }

        /**
         *
         * @return commands made be certain client
         */
        public LinkedList<String> getCommands() {
            return commands;
        }
    }
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        new Server();
    }


}
