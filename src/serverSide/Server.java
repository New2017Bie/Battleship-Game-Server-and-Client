package serverSide;

import java.io.IOException;
import java.net.ServerSocket;

public class Server implements Runnable {

    private ServerSocket ss;

    private Server(int port) {
        try {
            ss = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Port " + port + " is not closed or occupied.");
            e.printStackTrace();
        }

        new Thread(this).start();
    }

    @Override
    public void run() {
        while (!Thread.interrupted() && !ss.isClosed()) {
            try {
                MessageHandler.handleConnection(ss.accept());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new Server(8080);
    }
}
