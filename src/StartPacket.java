import java.io.Serializable;
import java.util.List;

public class StartPacket implements Serializable {

    private UserHand h0;
    private Hand h1;
    private Hand h2;
    private Hand h3;
    private List<Card> t;

    /**
     * StartPacket contains all necessary information to start a game.
     * Only sent by ServerGame when starting/resetting the game
     *
     * @param h0        P0's Hand
     * @param h1        P1's Hand
     * @param h2        P2's Hand
     * @param h3        P3's Hand
     * @param t         Cards in the table
     */
    public StartPacket(UserHand h0, Hand h1, Hand h2, Hand h3, List<Card> t) {
        this.h0 = h0;
        this.h1 = h1;
        this.h2 = h2;
        this.h3 = h3;
        this.t = t;
    }

    public UserHand getH0() {
        return h0;
    }

    public Hand getH1() {
        return h1;
    }

    public Hand getH2() {
        return h2;
    }

    public Hand getH3() {
        return h3;
    }

    public List<Card> getT() {
        return t;
    }
}
