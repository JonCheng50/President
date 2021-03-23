import java.util.List;

public class Quad implements Play{
    public static final int NUM = 4;
    public boolean viable = true;
    private Card card1;
    private Card card2;
    private Card card3;
    private Card card4;
    private List<Card> table;

    public Quad (List<Card> cards){
        if (cards.size() != 4){
            viable = false;
        }
        card1 = cards.get(0);
        card2 = cards.get(1);
        card3 = cards.get(2);
        card4 = cards.get(3);
        if (!(card1.getNum() == card2.getNum() && card2.getNum() == card3.getNum() && card3.getNum() == card4.getNum())){
            viable = false;
        }
        table = TableHand.getTable();
    }

    // See Play interface for method documentation

    // A valid double is 4 cards of the same number
    // QuickClear never TODO Change to bomb??
    // Clear if 2s are played
    // Skip never

    @Override
    public boolean isValid(boolean first, boolean turn, boolean type) {
        if (!viable) {
            return false;
        }
        if (first && turn){
            GameCourt.setType(3);
            return true;
        }
        //Note if 4 cards are being played, it cannot be a quickClear
        if (turn && type){
            Card c1 = table.get(table.size() - 1);
            int comp = Card.compare(card1, c1, false);
            // Note comp != 0 b/c type is 4 and there are only 4 of each number
            return comp > 0;
        }
        return false;
    }

    @Override
    public void checkQuickClear() {
    }

    @Override
    public boolean isQuickClear() {
        return false;
    }

    @Override
    public boolean isSkip() {
        return false;
    }

    @Override
    public int getNUM(){
        return NUM;
    }
}
