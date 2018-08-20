package serverSide;

public enum GridStatus {
    HIT(1), MISS(1), ATTEMPT(1), EMPTY(1),
    Destroyer(2), Submarine(3), Cruiser(3), Battleship(4), Carrier(5);

    private boolean isShip;
    private int life;

    GridStatus(int life) {
        this.life = life;

        if (life > 1) {
            this.isShip = true;
        } else {
            this.isShip = false;
        }
    }

    public boolean isShip() {
        return isShip;
    }

    public int getLife() {
        return life;
    }
}
