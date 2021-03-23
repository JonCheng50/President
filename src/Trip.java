import java.util.List;

public class Trip implements Play{
    public static final int NUM = 3;
    public boolean viable = true;
    private Card card1;
    private Card card2;
    private Card card3;
    private List<Card> table;
    private boolean quickClear = false;

    public Trip(List<Card> cards){
        if (cards.size() != 3){
            viable = false;
        }
        card1 = cards.get(0);
        card2 = cards.get(1);
        card3 = cards.get(2);
        if (!(card1.getNum() == card2.getNum() && card2.getNum() == card3.getNum())){
            viable =  false;
        }
        table = TableHand.getTable();
    }

    // See Play interface for method documentation

    // A valid triple is 3 cards of the same number
    // QuickClear if the top card in the table are also the same number
    // Clear if 2s are played
    // Skip never
    @Override
    public boolean isValid(boolean first, boolean turn, boolean type) {
        if (!viable){
            return false;
        }
        if (first && turn){
            GameCourt.setType(3);
            return true;
        }
        checkQuickClear();
        if (turn && type){
            Card c1 = table.get(table.size() - 1);
            int comp = Card.compare(card1, c1, false);
            // Note comp != 0 because type is 3 and there are only 4 of each number
            return comp > 0;
        }
        return quickClear;
    }

    @Override
    public void checkQuickClear() {
        if (!viable){
            quickClear = false;
        }
        else if (table.size() >= 1){
            int c1 = table.get(table.size() - 1).getNum();
            quickClear = card1.getNum() == c1;
        }
        else {
            quickClear = false;
        }
    }

    @Override
    public boolean isQuickClear() {
        return quickClear;
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
