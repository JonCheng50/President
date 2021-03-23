import java.io.Serializable;
import java.util.List;

public class UpdatePacket implements Serializable {

    private int con;
    private int turn;
    private int clearCount;
    private int type;
    private List<Card> t;
    private int who;
    private int num;
    private int msg;

    /**
     * UpdatePacket contains necessary information to update the other games
     * Sent in between all Games whenever anyone plays a card or passes
     *
     * @param con           from which connection the update was sent from (0-3)
     * @param turn          who's turn it is (0-3)
     * @param clearCount    what the clearCount is (0-2)
     * @param type          what the type is (1-3)
     * @param t             list of cards in the table
     * @param who           who played the previous card (-1-3), -1 means no one played
     * @param num           number of cards played in previous turn (0-4)
     * @param msg           what message should be shown to everyone (0-4)
     *                      0: no message
     *                      1: QuickClear
     *                      2: Clear
     *                      3: Skip
     *                      4: Pass
     *
     */
    public UpdatePacket(int con, int turn, int clearCount, int type, List<Card> t, int who, int num, int msg) {
        this.con = con;
        this.turn = turn;
        this.clearCount = clearCount;
        this.type = type;
        this.t = t;
        this.who = who;
        this.num = num;
        this.msg = msg;
    }

    public int getCon() {
        return con;
    }

    public int getTurn() {
        return turn;
    }

    public int getClearCount() {
        return clearCount;
    }

    public int getType() {
        return type;
    }

    public List<Card> getT() {
        return t;
    }

    public int getWho() {
        return who;
    }

    public int getNum() {
        return num;
    }

    public int getMsg() {
        return msg;
    }
}
