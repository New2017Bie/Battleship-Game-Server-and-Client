package serverSide.message;

import serverSide.GridStatus;

public class MessageFactory {

    //client sends to server
    public static LoginMessage getLoginMessage(String username) {
        return new LoginMessage(username);
    }


    // result sent from server to the attacker
    public static ResultMessage getResultMessage(GridStatus[] gt) {
        return new ResultMessage(gt);
    }

    // Game message to the attackee, board to ensure integrity
    public static GameActionMessage getGameActionMessage(GridStatus[] gt, int row, int col, GridStatus[][] board) {
        return new GameActionMessage(gt, row, col, board);
    }

    public static GridStatusMessage getAttemptMessage(String username, int row, int col) {
        return GridStatusMessage.getAttemptMessage(username, row, col);
    }

    public static ChatMessage getChatMessage(String text) {
        return new ChatMessage(text);
    }

    public static ChatMessage getChatMessage(String username, String text) {
        return new ChatMessage(username, text);
    }

    // ready to play signal from client to server
    public static SystemMessage getReadyMessage(GridStatus[][] gt) {
        return SystemMessage.getReadyMessage(gt);
    }

    // start game signal from server to clients
    public static SystemMessage getStartMessage() {
        return SystemMessage.getStartMessage();
    }

    public static SystemMessage getAckMessage() {
        return SystemMessage.getAckMessage();
    }
    public static SystemMessage getDuplicateUsernameMessage() {
        return SystemMessage.getDuplicateUsernameMessage();
    }
    public static SystemMessage getDuplicateGuessMessage() {
        return SystemMessage.getDuplicateGuessMessage();
    }

    public static SystemMessage getBeginTurn() {
        return SystemMessage.getBeginTurn();
    }

    public static SystemMessage getDenyMessage() {
        return SystemMessage.getDenyMessage();
    }

    public static SystemMessage getWinnerGameOverMessage() {
        return SystemMessage.getWinnerGameOverMessage();
    }
    public static SystemMessage getLoserGameOverMessage() {
        return SystemMessage.getLoserGameOverMessage();
    }
}
