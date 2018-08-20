package serverSide;

import java.util.HashSet;


public class Referee {

    private HashSet<GridStatus> survivedShips;
    public String username;
    private final static int BOARDSIZE = 10;
    private GridStatus[][] myBoard = new GridStatus[BOARDSIZE][BOARDSIZE];
    private GameHandler.GameTurn turn;

    Referee(String username, GameHandler.GameTurn gameTurn) {
        setUsername(username);
        setTurn(gameTurn);

        survivedShips = new HashSet<>();
        for (GridStatus st: GridStatus.values()) {
            if (st.isShip()) {
                survivedShips.add(st);
            }
        }
    }

    GridStatus[] getHit(GridStatus gs, int row, int col) {
        GridStatus[] result = new GridStatus[3];

        if (gs.equals(GridStatus.ATTEMPT)) {
            if (myBoard[row][col].isShip()) {
                myBoard[row][col] = GridStatus.HIT;
                result[0] = GridStatus.HIT;
            } else {
                if (myBoard[row][col].equals(GridStatus.EMPTY)) {
                    myBoard[row][col] = GridStatus.MISS;
                    result[0] = GridStatus.MISS;
                } else {
                    return null;
                }
            }

            result[1] = checkSurvivedShip();

            if (!checkSurvival()) {
                System.out.println("Game over");
                result[2] = GridStatus.EMPTY;
            }

            return result;
        }
        return null;
    }

    private GridStatus checkSurvivedShip() {
        HashSet<GridStatus> localSurvivedShips = new HashSet<>();
        HashSet<GridStatus> shipSunkInThisMove = survivedShips;

        for (int rows = 0; rows < BOARDSIZE; rows++) {
            for (int cols = 0; cols < BOARDSIZE; cols++) {
                if (myBoard[rows][cols].isShip()) {
                    localSurvivedShips.add(myBoard[rows][cols]);
                }
            }
        }

        shipSunkInThisMove.removeAll(localSurvivedShips);

        if (shipSunkInThisMove.size() == 1) {
            GridStatus sunkShipName = shipSunkInThisMove.iterator().next();
            survivedShips = localSurvivedShips;
            return sunkShipName;
        } else if (shipSunkInThisMove.size() > 1) {
            try {
                System.out.println("shipSunkInThisMove" + shipSunkInThisMove);
                System.out.println("localSurvivedShips: " + localSurvivedShips);
                throw new IllegalStateException("There're discrepancies between client side and server side.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        survivedShips = localSurvivedShips;
        return null;
    }

    private boolean checkSurvival() {

        return (!survivedShips.isEmpty());
    }

    GameHandler.GameTurn getTurn() {
        return turn;
    }

    GridStatus[][] getMyBoard() {
        return myBoard;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    void setMyBoard(GridStatus[][] myBoard) {
        this.myBoard = myBoard;
    }

    private void setTurn(GameHandler.GameTurn turn) {
        this.turn = turn;
    }

    static void printBoard(GridStatus[][] gt2d) {

        if (gt2d != null) {
            for (int i = 0; i < BOARDSIZE; i++) {
                for (int j = 0; j < BOARDSIZE; j++) {
                    System.out.print(gt2d[i][j] + " ");
                }
                System.out.println();
            }
        } else {
            System.out.println("Null board");
        }
    }

    public static void main(String[] args) {

    }

}
