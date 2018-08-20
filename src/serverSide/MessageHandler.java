package serverSide;

import serverSide.message.GridStatusMessage;
import serverSide.message.Message;
import serverSide.message.MessageFactory;
import serverSide.message.SystemMessage;
import serverSide.jsonConverters.*;

import java.io.*;
import java.net.Socket;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class MessageHandler implements Runnable, Comparable<MessageHandler> {

    private static Set<MessageHandler> connections = new ConcurrentSkipListSet<>();
    private String username;
    private Socket socket;
    private BufferedReader br;
    private BufferedWriter bw;

    private MessageHandler(Socket socket) {
        this.socket = socket;

        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(this).start();
    }

    static MessageHandler handleConnection(Socket socket) {
        System.out.println("Connection from: " + socket.getRemoteSocketAddress());

        return new MessageHandler(socket);
    }

    static void shutdown() {
        GameHandler.shutdown();

        Iterator<MessageHandler> iterator = connections.iterator();
        while (iterator.hasNext()) {
            iterator.next().shutdownConnection();
            iterator.remove();
        }
    }

    private void shutdownConnection() {
        if (br != null) {
            try{
                br.close();
            }catch(IOException e){
                e.printStackTrace();
            }

        }

        if (bw != null) {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Socket already closed");
        }

        Iterator<MessageHandler> iterator = connections.iterator();
        while (iterator.hasNext()) {
            MessageHandler connection = iterator.next();
            if (this.equals(connection)) {
                iterator.remove();
            }
        }
    }

    private String jsonToString() {
        String line = null;

        try {
            line = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return line;
    }

    private boolean processLogin() {
        boolean loginSuccess = false;
        if (br == null) {
            shutdownConnection();
            return false;
        }

        Message message = JsonConverter.readJson(jsonToString());

        if (message != null) {
            if (message.getMessageType().equals(Message.MessageType.LOGIN)) {
                setUsername(message.getUsername());
            }
        }

        if (getUsername() != null) {
            System.out.println("Adding " + getUsername() + " to clients");
            int connectionCount = connections.size();
            connections.add(this);

            // connections.contains(this)...??
            if (connections.size() <= connectionCount) {
                username = null;
            }
        }

        System.out.println(getUsername());

        if (getUsername() != null) {
            this.sendMessage(MessageFactory.getAckMessage());
            loginSuccess = true;

            broadcast(message, getUsername());
        } else {
            this.sendMessage(MessageFactory.getDuplicateUsernameMessage());
        }

        return loginSuccess;
    }

    private static void broadcast(Message message, String username) {
        for (MessageHandler connection: connections) {
            if (username == null || !connection.getUsername().equals(username)) {
                connection.sendMessage(message);
            }
        }
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    // send the message to the user according to the username
    void sendMessage(Message message) {
        // write and send Json
        try {
            bw.write(JsonConverter.writeJson(message));
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            System.err.println("Connection lost.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageHandler that = (MessageHandler) o;
        if (username == null) {
            if (that.getUsername() != null) {
                return false;
            }
        } else if (!username.equals(that.getUsername())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((username == null) ? 0 : username.hashCode());
      return result;
    }

    @Override
    public int compareTo(MessageHandler o) {
        if (this.equals(o)) {
            return 0;
        } else if (o != null) {
            return this.getUsername().compareTo(o.getUsername());
        }

        return 0;
    }

    @Override
    public void run() {
        if (processLogin()) {
            try {
                while (!Thread.interrupted() && socket.isConnected() && !socket.isClosed()) {
                    Message message = JsonConverter.readJson(jsonToString());

                    if (message != null) {
                        if (Message.MessageType.CHAT.equals(message.getMessageType())) {
                            System.out.println(message);
                            broadcast(message, this.getUsername());
                            sendMessage(message);
                        } else if (Message.MessageType.SYSTEM.equals(message.getMessageType())) {
                            GameHandler.handleSystemMessage((SystemMessage) message, this);
                        } else if (Message.MessageType.GAME_ACTION.equals(message.getMessageType())) {
                            GameHandler.handleActionMessage((GridStatusMessage) message, this);
                        }
                    }
                }
            } catch (NullPointerException e) {
                System.out.println("One client disconnected");
            }
        }
    }
}
