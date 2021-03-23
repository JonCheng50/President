import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Hand represents a set of cards
 */
public abstract class Hand implements Serializable {
    List<Card> cards;       // Cards in the hand
    int start_X;            // Initial x position of card images, based on the number of cards
    int played;             // Number of cards most recently played, used for updating other games
    int main;               // Where the cards should be orientated:
                            // 0 = in front of you
                            // 1 = left of you
                            // 2 = opposite of you
                            // 3 = right of you

    public Hand (){
        cards = new ArrayList<>();
    }

    // Implementation depends on type of Hand
    abstract boolean play(boolean first, int turn);

    // Draws the cards in the hand on the screen based on what main it is
    abstract void draw(Graphics g);

    // Adds a card to the hand
    abstract void addCard(String s);

    abstract void removeCards(List<Card> l);

    public List<Card> getCards() {
        return cards;
    }

    public void setMain(int x) {
        main = x;
    }

    // Decreases the size of the hand
    // Only to be called on other player's hands (main != 0)
    public void decSize(int x) {
        cards = cards.subList(0, cards.size() - x);
    }

    // Means the user has clicked on a card, and should be moved forward
    public void select(int n) {
        cards.get(n).select();
    }

    // Returns a list of selected cards. Empty List is no cards are selected
    public java.util.List<Card> getSelected(java.util.List<Card> hand){
        List<Card> selected = new ArrayList<>();
        for (Card c: hand){
            if (c.isSelected()){
                selected.add(c);
            }
        }
        return selected;
    }

    // Used to check is someone has won (num of Cards = 0)
    public int getNumCards() {
        return cards.size();
    }

    //Sorts the user's hand, with 3 being the lowest and 2 the greatest
    public void sort() {
        Collections.sort(cards, new Card.SortCards());
    }
}
