package serverSide.message;

import serverSide.GridStatus;

import java.util.Arrays;

public class ResultMessage extends Message {

    GridStatus hitMiss;
    GridStatus sinkShip;
    boolean isSurvival;

    ResultMessage(GridStatus[] result) {
        super(null);

        System.out.println(Arrays.toString(result));

        hitMiss = result[0];
        sinkShip = result[1];
        if (result[2] == null) {
            isSurvival = true;
        } else {
            isSurvival = false;
        }
    }

    public GridStatus getHitMiss() {
        return hitMiss;
    }

    public GridStatus getSinkShip() {
        return sinkShip;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.RESULT;
    }

    @Override
    public String toString() {
        return "ResultMessage{" +
                "hitMiss=" + hitMiss +
                ", sinkShip=" + sinkShip +
                ", isSurvival=" + isSurvival +
                '}';
    }
}
