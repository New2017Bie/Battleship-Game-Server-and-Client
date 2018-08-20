package clientSide;

import serverSide.GridStatus;
import serverSide.jsonConverters.JsonConverter;
import serverSide.message.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ClientApp {

    private Socket socket;
    private BufferedReader br;
    private BufferedWriter bw;
    private ClientGui cg;
    private Timer timerChat = new Timer();
    private Timer timerAction = new Timer();
    private String username;
    private int[] attempt = new int[2];
    private String host = "127.0.0.1";

    public static void main(String[] args) throws IOException {

        ClientApp client = new ClientApp();

        client.init();
    }

    private void init() throws IOException {
        cg = new ClientGui();
        host = cg.getHost();
        try {
            InetAddress ia = InetAddress.getByName(host);
            socket = new Socket(ia, 8080);
        } catch (Exception e) {
            disConnect(socket, bw, br);
        }
        username = cg.getUsername();
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        readThread(socket);

        login();


        while (socket.isConnected() && !socket.isClosed()) {
            sendMsg();
            sendGameAction();
        }
    }

    private void readThread(Socket socket) {
        Thread receiveMessage = new Thread(() -> {
            try {
                while (!Thread.interrupted() && socket.isConnected() && !socket.isClosed()) {
                    readMessage();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        receiveMessage.start();
    }

    private void readMessage() throws IOException {
        try {
            Message read = JsonConverter.readJson(jsonToString());

            if (read != null) {
                if (read.getUsername() == null) {
                    cg.showMsgln("System message: ");
                }
                if (read instanceof SystemMessage) {
                    SystemMessage sm = (SystemMessage) read;
                    if ((sm.getSystemResponse() == SystemMessage.SystemResponse.GAME_OVER_LOSER)) {
                        cg.warning("You lose.");
                        cg.warning("Please restart the client to get another game!");
                        disConnect(socket, bw, br);
                    } else if ((sm.getSystemResponse() == SystemMessage.SystemResponse.GAME_OVER_WINNER)) {
                        cg.warning("You win.");
                        cg.warning("Please restart the client to get another game!");
                        disConnect(socket, bw, br);
                    } else if ((sm.getSystemResponse() == SystemMessage.SystemResponse.START)) {
                        cg.showMsgln("Game started!\n");
                    } else if ((sm.getSystemResponse() == SystemMessage.SystemResponse.BEGIN_TURN)) {
                        cg.showMsgln("Your turn!\n");
                    } else if ((sm.getSystemResponse() == SystemMessage.SystemResponse.DUPLICATE_USERNAME)) {
                        cg.warning("Cannot connect to the server for duplicated username.");
                    } else if ((sm.getSystemResponse() == SystemMessage.SystemResponse.ACK)) {
                        cg.showMsgln("Accepted.\n");
                    } else if ((sm.getSystemResponse() == SystemMessage.SystemResponse.DENY)) {
                        cg.showMsgln("Action denied.\n");
                    } else if ((sm.getSystemResponse() == SystemMessage.SystemResponse.DUPLICATE_GUESS)) {
                        cg.showMsgln("You have guessed this grid before.");
                    }
                } else if (read instanceof ChatMessage) {
                    handleChat(read);
                } else if (read instanceof GameActionMessage) {
                    handleGridStatus((GameActionMessage) read);
                } else if (read instanceof ResultMessage) {
                    handleResultMessage((ResultMessage) read);
                } else  {
                    cg.showMsgln("username: " + read.getUsername());
                    cg.showMsgln("type: " + read.getMessageType() + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            disConnect(socket, bw, br);

            System.exit(1);
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


    private void sendMessage(Message message) throws IOException {
        bw.write(JsonConverter.writeJson(message));
        bw.newLine();
        bw.flush();
    }

    private void handleResultMessage(ResultMessage read) {
        if (read.getHitMiss().equals(GridStatus.HIT)) {
            cg.playFireAudio();
            cg.targetBoard[attempt[0]][attempt[1]] = GridStatus.HIT;
            cg.showMsgln("Hit!!!!\n");
            if (read.getSinkShip() != null) {
                cg.showMsgln("Opponent's " + read.getSinkShip() + " is destroyed by you !\n");
            }
        } else if (read.getHitMiss().equals(GridStatus.MISS)) {
            cg.targetBoard[attempt[0]][attempt[1]] = GridStatus.MISS;
            cg.showMsgln("Miss................\n");
        }

        cg.repaintTargetBoard();
        cg.repaintMyBoard();
    }

    private void handleGridStatus(GameActionMessage read) {
        cg.setMyBoard(read.getBoard());
        if (read.getHitMiss().equals(GridStatus.HIT)) {
            cg.playFireAudio();
            cg.showMsgln("You just got hit .....\n");
            if (read.getSinkShip() != null) {
                cg.showMsgln("Your " + read.getSinkShip().toString() + " is destroyed by the opponent...\n");
            }
        } else if (read.getHitMiss().equals(GridStatus.MISS)) {
            cg.showMsgln("The opponent just missed!\n");
        }

        cg.repaintMyBoard();
        cg.repaintTargetBoard();
    }

    private void handleChat(Message read) {
        cg.showMsgln("username: " + read.getUsername());
        cg.showMsgln("type: " + read.getMessageType());
        cg.showMsgln("Text: " + ((ChatMessage) read).getText() + "\n");
    }

    private void login() throws IOException {
        this.sendMessage(MessageFactory.getLoginMessage(username));
        bw.flush();
    }

    private void writeMsg(String msg) throws IOException {
        this.sendMessage(MessageFactory.getChatMessage(username, msg));
        bw.flush();
    }


    private void writeHit() throws IOException {
        this.sendMessage(MessageFactory.getAttemptMessage(username, attempt[0], attempt[1]));
        bw.flush();
    }

    private void writeJoin() throws IOException {
        this.sendMessage(MessageFactory.getReadyMessage(cg.getMyBoard()));
        bw.flush();
    }

    private void sendMsg() {

        timerChat.schedule(new TimerTask() {

            @Override
            public void run() {
                String msg = cg.getMsg();

                if (msg != null && !msg.trim().equals("")) {
                    try {

                        writeMsg(msg);
                        cg.setMsg(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 500);
    }

    private void sendGameAction() {
        timerAction.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (cg.getGameAction() != ClientGui.GameAction.EMPTY &&
                            cg.getGameAction() != null) {
                        switch (cg.getGameAction()) {
                            case ATTEMPT:
                                attempt = cg.getAttempt();
                                writeHit();
                                break;
                            case JOIN:
                                writeJoin();
                                break;
                            case EXIT:
                                disConnect(socket, bw, br);
                                break;
                        }
                        cg.setGameAction(ClientGui.GameAction.EMPTY);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 500);
    }

    private void disConnect(Socket s, BufferedWriter bw, BufferedReader br) throws IOException {
        cg.exitWarning();
        try {
            bw.close();
        } catch (Exception e) {

        }

        try {
            br.close();
        } catch (Exception e) {
            // expected
        }

        try {
            s.close();
        } catch (Exception e) {
            // expected
        }

        System.exit(0);
    }
}
