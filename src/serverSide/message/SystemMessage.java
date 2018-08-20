package serverSide.message;

import serverSide.GridStatus;

import java.util.Arrays;

public class SystemMessage extends Message {

    public enum SystemResponse {ACK, DENY, READY, START, GAME_OVER_WINNER, GAME_OVER_LOSER, DUPLICATE_USERNAME, DUPLICATE_GUESS, BEGIN_TURN}
    private SystemResponse systemResponse;
    private String winner;
    private GridStatus[][] gt;

    public SystemMessage(SystemResponse systemResponse) {
        super(null);
        this.systemResponse = systemResponse;
        setWinner(null);
    }

    public SystemMessage(SystemResponse systemResponse, GridStatus[][] gt) {
        super(null);
        this.systemResponse = systemResponse;
        setWinner(null);
        this.gt = gt;
    }

    // start game signal from server to clients
    static SystemMessage getReadyMessage(GridStatus[][] gt) {
        return new SystemMessage(SystemResponse.READY, gt);
    }

    // start game signal from server to clients
    static SystemMessage getStartMessage() {
        return new SystemMessage(SystemResponse.START);
    }

    static SystemMessage getAckMessage() {
        return new SystemMessage(SystemResponse.ACK);
    }

    static SystemMessage getDenyMessage() {
        return new SystemMessage(SystemResponse.DENY);
    }

    static SystemMessage getDuplicateUsernameMessage() {
        return new SystemMessage(SystemResponse.DUPLICATE_USERNAME);
    }

    static SystemMessage getDuplicateGuessMessage() {
        return new SystemMessage(SystemResponse.DUPLICATE_GUESS);
    }

    static SystemMessage getBeginTurn() {
        return new SystemMessage(SystemResponse.BEGIN_TURN);
    }

    static SystemMessage getWinnerGameOverMessage() {
        return new SystemMessage(SystemResponse.GAME_OVER_WINNER);
    }
    static SystemMessage getLoserGameOverMessage() {
        return new SystemMessage(SystemResponse.GAME_OVER_LOSER);
    }

    public SystemResponse getSystemResponse() {
        return systemResponse;
    }

    private void setWinner(String winner) {
        this.winner = winner;
    }

    public GridStatus[][] getGt() {
        return gt;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.SYSTEM;
    }

    @Override
    public String toString() {
        return "SystemMessage{" +
                "systemResponse=" + getSystemResponse() +
                ", winner='" + winner + '\'' +
                ", gt=" + Arrays.toString(gt) +
                ", username='" + username + '\'' +
                ", module='" + module + '\'' +
                ", messageType=" + getMessageType() +
                '}';
    }
}
