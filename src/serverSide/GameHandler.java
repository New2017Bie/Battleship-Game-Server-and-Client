package serverSide;

import serverSide.message.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameHandler implements Runnable{

    public enum GameState {NOT_PLAYING, JOIN_PHASE, RUNNING}
    public enum GameTurn {A, B}
    private GameState currentState = GameState.JOIN_PHASE;
    private static GameTurn currentTurn  = GameTurn.A;
    private static ConcurrentHashMap<GameTurn, MessageHandler> playerTurn = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<MessageHandler, Referee> players = new ConcurrentHashMap<>();
    private static final int BOARDSIZE = 10;
    private static GridStatus[][] playerBoardA;
    private static GridStatus[][] playerBoardB;
    private static final int GAMESIZE = 2;
    private static GameHandler game = new GameHandler();


    static void shutdown() {
        game = new GameHandler();
    }

    private void startGame() {
        currentState = GameState.RUNNING;

        new Thread(this).start();
    }

    static void handleSystemMessage(SystemMessage sysMsg, MessageHandler mh) {
        if (sysMsg.getSystemResponse().equals(SystemMessage.SystemResponse.READY)) {

            GridStatus[][] initalBoard = sysMsg.getGt();
            HashSet<GridStatus> shipsOnBoard = new HashSet<>();
            for (int rows = 0; rows < BOARDSIZE; rows++) {
                for (int cols = 0; cols < BOARDSIZE; cols++) {
                    if (initalBoard[rows][cols].isShip()) {
                        shipsOnBoard.add(initalBoard[rows][cols]);
                    }
                }
            }
            if (shipsOnBoard.size() == 5) {

                if (players.size() < GAMESIZE) {
                    game.join(mh);

                    if (currentTurn == GameTurn.A) {
                        playerBoardA = sysMsg.getGt();
                        Referee.printBoard(playerBoardA);
                        players.get(mh).setMyBoard(playerBoardA);
                        playerTurn.put(GameTurn.A, mh);

                    } else {
                        playerBoardB = sysMsg.getGt();
                        Referee.printBoard(playerBoardB);
                        players.get(mh).setMyBoard(playerBoardB);
                        playerTurn.put(GameTurn.B, mh);
                    }

                    nextGameTurn();

                    if (players.size() == GAMESIZE) {
                        game.startGame();
                    }
                } else {
                    throw new IllegalStateException("Discrepancy! Only 2 persons are allowed in the one game.");
                }
            } else {
                mh.sendMessage(MessageFactory.getDenyMessage());
            }
        }
    }

    private static void nextGameTurn() {
        currentTurn = (currentTurn == GameTurn.A)? GameTurn.B: GameTurn.A;
    }

    static void handleActionMessage(GridStatusMessage msg, MessageHandler mh) {
        if (players.size() != GAMESIZE) {
            throw new IllegalStateException("Two, and only two players. Number of players registered: " + players.size());
        }

        if (players.get(mh).getTurn().equals(currentTurn)) {

            ConcurrentHashMap<MessageHandler, Referee> temp = new ConcurrentHashMap<>(players);
            temp.remove(mh);
            MessageHandler opponentsMessageHandler = temp.entrySet().iterator().next().getKey();

            Referee opponentsReferee = temp.get(opponentsMessageHandler);

            if (msg.getGs().equals(GridStatus.ATTEMPT)) {
                GridStatus[] result = opponentsReferee.getHit(msg.getGs(), msg.getRow(), msg.getCol());

                if(result[0] == null){
                    mh.sendMessage(MessageFactory.getDuplicateGuessMessage());
                }
                else {
                    nextGameTurn();
                    mh.sendMessage(MessageFactory.getResultMessage(result));
                    opponentsMessageHandler.sendMessage(MessageFactory.getGameActionMessage(result, msg.getRow(), msg.getCol(),
                            opponentsReferee.getMyBoard()));

                    if (result[2] == GridStatus.EMPTY) {
                        game.currentState = GameState.NOT_PLAYING;
                        opponentsMessageHandler.sendMessage(MessageFactory.getLoserGameOverMessage());
                        mh.sendMessage(MessageFactory.getWinnerGameOverMessage());

                        terminateGame();

                    }
                    sendBeginTurnMessageToOtherPlayer(opponentsMessageHandler);
                }
            }
        } else {
            mh.sendMessage(MessageFactory.getDenyMessage());
        }
    }

    private static void sendBeginTurnMessageToOtherPlayer(MessageHandler opponentsMessageHandler){
        opponentsMessageHandler.sendMessage(MessageFactory.getBeginTurn());
    }

    private static void terminateGame(){
        MessageHandler.shutdown();
        game = new GameHandler();
        playerTurn = new ConcurrentHashMap<>();
        players = new ConcurrentHashMap<>();
        currentTurn  = GameTurn.A;
    }

    private void join(MessageHandler handler) {
        if (!currentState.equals(GameState.JOIN_PHASE)) {
            throw new IllegalStateException("It's " + currentState + ", not READY phase.");
        }

        players.put(handler, new Referee(handler.getUsername(), currentTurn));
        System.out.println(handler.getUsername() + " " + currentTurn);
        System.out.println(currentState);
        if (players.size() > GAMESIZE) {
            throw new IllegalStateException("Discrepancy! Only 2 persons are allowed in the one game.");
        }
    }

    private void broadcast(Message msg) {
        broadcast(msg, null);
    }

    private void broadcast(Message msg, String username) {
        for (MessageHandler player: players.keySet()) {
            if (!player.getUsername().equals(username) || username == null) {
                player.sendMessage(msg);
            }
        }
    }

    @Override
    public void run() {
        if (players.size() >= GAMESIZE) {
            broadcast(MessageFactory.getStartMessage());
            playerTurn.get(GameTurn.A).sendMessage(MessageFactory.getBeginTurn());
        } else {
            currentState = GameState.NOT_PLAYING;
            players.clear();
        }
    }

    public static void main(String[] args) {

    }
}
