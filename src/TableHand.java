import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * TableHand represents the cards currently on the table (already played but not yet cleared)
 */
public class TableHand extends Hand implements Serializable {
    private final int SIZE_X = 80;
    private final int SIZE_Y = 116;

    // List of the randomized x and y positions of cards in the table
    private static List<Integer> X = new ArrayList<>();
    private static List<Integer> Y = new ArrayList<>();

    private BufferedImage img;

    private static List<Card> cards = new ArrayList<>();

    public TableHand(){
        cards = new ArrayList<>();
        X = new ArrayList<>();
        Y = new ArrayList<>();
    }

    // See Hand class for method documentation

    // Returns table as List of Cards
    public static List<Card> getTable(){
        if (cards == null){
            System.out.println("Internal Error: Tried to get cards when Table.cards was null");
        }
        return cards;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void addCard(String s) {
        if (s.equals("")){
            return;
        }
        Card c = new Card(s, false);
        cards.add(c);
    }

    // IMPORTANT NOTE: only adds new cards not yet in the table
    public void addCard(List<Card> l) {
        if (l == null) {
            System.out.println("Internal Error: Tried to add null cards to table");
        } else {
            for (Card c : l) {
                if (!cards.contains(c)) {
                    cards.add(c);
                }
            }
        }
    }

    public void removeCards(List<Card> l) {
        for (Card c : l) {
            cards.remove(c);
        }
    }

    @Override
    boolean play(boolean first, int turn) {
        return false;
    }

    public int getNum() {
        return cards.size();
    }

    // Draws the cards in the table slightly offset, storing previous values so already played cards don't change pos
    @Override
    public void draw(Graphics g) {
        if (cards.size() == 0){
            return;
        }
        for (int a = 0; a < cards.size(); a ++){
            Card c = cards.get(a);
            String imgFile = "files/" + c.getName() + ".png";
            try {
                img = ImageIO.read(new File(imgFile));
            } catch (IOException e) {
                System.out.println("Internal Error: " + e.getMessage());
            }
            int x;
            int y;
            if (a < X.size()){
                // This means card has already been drawn (was already in the table)
                x = X.get(a);
                y = Y.get(a);
            } else {
                // Draw new cards in slightly offset position and store these positions
                Random r = new Random();
                if (a % 2 == 0) {
                    x = r.nextInt((625 - 610) + 1) + 610; // 610 - 625
                }
                else {
                    x = r.nextInt((610 - 595) + 1) + 595; // 595 - 610
                }
                y = r.nextInt((207 - 177) + 1) + 177; // 177 - 207, 293 - 323
                X.add(x);
                Y.add(y);
            }
            g.drawImage(img, x, y, SIZE_X, SIZE_Y, null);
        }
    }

    // Clears the table and sets turn to player
    public static void clear(int player) {
        cards = new ArrayList<Card>();
        X = new ArrayList<>();
        Y = new ArrayList<>();
        GameCourt.setFirst(true);
        GameCourt.setTurn(player);
    }

    // Clears the table
    public static void clear() {
        cards = new ArrayList<Card>();
        X = new ArrayList<>();
        Y = new ArrayList<>();
        GameCourt.setFirst(true);
    }
}
