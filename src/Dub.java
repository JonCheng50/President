import java.util.List;

public class Dub implements Play{
    public static final int NUM = 2;
    public boolean viable = true;
    private Card card1;
    private Card card2;
    private List<Card> table;
    private boolean quickClear = false;


    public Dub(List<Card> cards){
        if (cards.size() != 2){
            viable = false;
        }
        else {
            card1 = cards.get(0);
            card2 = cards.get(1);
            if (card1.getNum() != card2.getNum()) {
                viable = false;
            }
        }
        table = TableHand.getTable();
    }

    // See Play interface for method documentation

    // A valid double is two cards of the same number
    // QuickClear if the top two cards in the table are also the same number
    // Clear if 2s are played
    // Skip never
    @Override
    public boolean isValid(boolean first, boolean turn, boolean type) {
        if (!viable){
            return false;
        }
        if (first && turn){
            GameCourt.setType(2);
            return true;
        }
        checkQuickClear();
        if (turn && type){
            Card c1 = table.get(table.size() - 1);
            int comp = Card.compare(card1, c1, false);
            if (comp == 0) {
                quickClear = true;
                return true;
            }
            else return comp > 0;
        }
        return quickClear;
    }

    @Override
    public void checkQuickClear(){
        if (!viable){
            quickClear = false;
        }
        else if (table.size() >= 2){
            int c1 = table.get(table.size() - 2).getNum();
            int c2 = table.get(table.size() - 1).getNum();
            quickClear = card1.getNum() == c1 && c1 == c2;
        }
        else {
            quickClear = false;
        }
    }

    @Override
    public boolean isQuickClear(){
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
