import java.util.List;

public class Single implements Play {
    public static final int NUM = 1;
    public boolean viable = true;
    private Card card;
    private List<Card> table;
    private boolean quickClear = false;
    private boolean skip = false;

    public Single (List<Card> cards){
        if (cards.size() != 1){
            viable = false;
        }
        card = cards.get(0); //put into abstract class play??
        table = TableHand.getTable();
    }

    // See Play interface for method documentation

    // A valid single is any one card
    // QuickClear if the top 3 cards in the table are the same number
    // Clear if it is a 2
    // Skip if the top card in the table is the same number
    @Override
    public boolean isValid(boolean first, boolean turn, boolean type) {
        skip = false;
        if (!viable){
            return false;
        }
        if (first && turn) {
            GameCourt.setType(1);
            return true;
        }
        checkQuickClear();
        if (turn && type) {
            Card c1 = table.get(table.size() - 1);
            //Note that table should have some card(s) because if not, then first = true
            int comp = Card.compare(card, c1, false);
            if (comp == 0){
                skip = true;
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
        else if (table.size() >= 3){
            int c1 = table.get(table.size() - 3).getNum();
            int c2 = table.get(table.size() - 2).getNum();
            int c3 = table.get(table.size() - 1).getNum();
            quickClear = card.getNum() == c1 && c1 == c2 && c2 == c3;
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
        return skip;
    }

    @Override
    public int getNUM(){
        return NUM;
    }
}
