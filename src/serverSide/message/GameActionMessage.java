package serverSide.message;

import serverSide.GridStatus;

import java.util.Arrays;

public class GameActionMessage extends ResultMessage {

    private GridStatus[][] board;
    private GridStatus[] result;
    private int row;
    private int col;

    GameActionMessage(GridStatus[] gt, int row, int col,  GridStatus[][] board) {
        super(gt);
        setBoard(board);
        this.row = row;
        this.col = col;
    }

    public GridStatus[][] getBoard() {
        return board;
    }

    public void setBoard(GridStatus[][] board) {
        this.board = board;
    }

    public GridStatus[] getResult() {
        return result;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.GAME_ACTION;
    }


    public GridStatus getHitMiss() {
        return hitMiss;
    }

    public GridStatus getSinkShip() {
        return sinkShip;
    }

    @Override
    public String toString() {
        return "GameActionMessage{" +
                "board=" + Arrays.toString(board) +
                ", result=" + Arrays.toString(result) +
                ", row=" + row +
                ", col=" + col +
                ", hitMiss=" + hitMiss +
                ", sinkShip=" + sinkShip +
                ", isSurvival=" + isSurvival +
                ", messageType=" + messageType +
                '}';
    }
}
