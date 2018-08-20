package serverSide.message;

import serverSide.GridStatus;

public class GridStatusMessage extends Message {

    private GridStatus gs;
    private int row;
    private int col;

    static GridStatusMessage getAttemptMessage(String username, int row, int col) {
        return new GridStatusMessage(username, GridStatus.ATTEMPT, row, col);
    }

    private GridStatusMessage(String username, GridStatus gs, int row, int col) {
        super(username);

        this.gs = gs;
        setRow(row);
        setCol(col);
    }

    public GridStatus getGs() {
        return gs;
    }

    public int getRow() {
        return row;
    }

    private void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    private void setCol(int col) {
        this.col = col;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.GAME_ACTION;
    }

    @Override
    public String toString() {
        return "GridStatusMessage{" +
                "gs=" + gs +
                ", row=" + row +
                ", col=" + col +
                '}';
    }
}
