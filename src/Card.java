import java.io.Serializable;
import java.util.Comparator;

/**
 * Card consists of a name and whether or not it is selected
 */
public class Card implements Serializable {
    private String name;            // Number (1-13) and Suit (S, C, D, H)
    private Boolean select;

    public Card (String name, Boolean select){
        this.name = name;
        this.select = select;
    }

    public String getName(){
        return name;
    }

    public int getNum(){
        if (name.length() == 2) {
            return Integer.parseInt(name.substring(0, 1));
        }
        return Integer.parseInt(name.substring(0, 2));
    }

    public String getSuit(){
        if (name.length() == 2) {
            return name.substring(1, 2);
        }
        return name.substring(2, 3);
    }

    public Boolean isSelected(){
        return select;
    }

    public void select(){
        select = !select;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Card) {
            Card c = (Card) o;
            return this.getName().equals(c.getName());
        }
        return false;
    }

    //Compares Card a and Card b, positive int if a is greater and negative int if b is
    //Boolean suits says whether or not suits should be considered. If false, 0 is returned
    //if a and b are the same numbers. If true, 0 is never returned.
    public static int compare (Card a, Card b, boolean suits){
        //Account for Ace and 2 being larger than all other cards
        int aNum = a.getNum();
        if (aNum == 1)
            aNum = 14;
        else if (aNum == 2)
            aNum = 15;
        int bNum = b.getNum();
        if (bNum == 1)
            bNum = 14;
        else if (bNum == 2)
            bNum = 15;
        int x = aNum - bNum;
        if (x != 0){
            return x;
        }
        if (suits) {
            return a.getSuit().compareTo(b.getSuit());
        }
        else {
            return 0;
        }
    }

    static class SortCards implements Comparator<Card> {
        public int compare (Card a, Card b){
            return Card.compare (a, b, true);
        }
    }
}
