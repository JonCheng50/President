import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * UserHand represents the hand of a player
 */
public class UserHand extends Hand implements Serializable {

    // Constants
    private final int SIZE_X = 80;
    private final int SIZE_Y = 116; //Ratio from X:Y of 1.45
    private int norm_Y = 440;
    private int select_Y = 410;
    private final int SIZE_XH = 70;
    private final int SIZE_YH = 50;
    private final int SIZE_XV = 50;
    private final int SIZE_YV = 70;

    private int player;

    private BufferedImage img;

    public static boolean qc = false;
    public static boolean clear2 = false;
    public static boolean skip = false;

    public UserHand(int player) {
        cards = new ArrayList<>();
        this.player = player;
    }

    //Adds a card to the user's hand. Only used in the beginning when the deck is being dealt
    @Override
    public void addCard(String s) {
        if (s.equals("")){
            return;
        }
        Card c = new Card(s, false);
        cards.add(c);
    }

    //Removes a list of cards from hand
    public void removeCards(List<Card> l) {
        for (Card c : l) {
            cards.remove(c);
        }
    }

    public void clear() {
        cards = new ArrayList<>();
    }

    public void removeAll() {
        cards = new ArrayList<>();
    }


    // Checks if cards selected can be played. If yes, those cards are played and it returns true.
    // If no, returns false and reports to user
    // Sets qc, clear2, and skip variables appropriately
    @Override
    public boolean play(boolean first, int turn) {
        qc = false;
        clear2 = false;
        skip = false;
        List<Card> cardsToPlay = getSelected(cards);
        Play p;
        if (cardsToPlay.size() == 1) {
            p = new Single(cardsToPlay);
        } else if (cardsToPlay.size() == 2) {
            p = new Dub(cardsToPlay);
        } else if (cardsToPlay.size() == 3) {
            p = new Trip(cardsToPlay);
        } else if (cardsToPlay.size() == 4) {
            p = new Quad(cardsToPlay);
        } else {
            return false;
        }
        if (p.isValid(first, turn == player, GameCourt.getType() == p.getNUM())) {
            played = cardsToPlay.size();
            int numPlayed = cardsToPlay.get(0).getNum();
            removeCards(cardsToPlay);
            TableHand.getTable().addAll(cardsToPlay);
            if (p.isQuickClear()) {
                qc = true;
            } else if (numPlayed == 2) {
                clear2 = true;
            } else if (p.isSkip()) {
                skip = true;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void draw(Graphics g) {
        if (main == 0) {
            //Ensures cards are still centered after playing some cards
            int x = (int) (1300 / 2 - (cards.size() / 2.0) * 80 - (cards.size() / 2.0 - 0.5) * 10);
            start_X = x; //For the MouseListener to know when a card has been clicked;
            for (int count = 0; count < cards.size(); count++) {
                Card c = cards.get(count);
                String imgFile = "files/" + c.getName() + ".png";
                try {
                    img = ImageIO.read(new File(imgFile));
                } catch (IOException e) {
                    System.out.println("Internal Error: " + e.getMessage());
                }
                int y = norm_Y;
                if (c.isSelected()) {
                    y = select_Y;
                }
                g.drawImage(img, x, y, SIZE_X, SIZE_Y, null);
                x += 90;
            }
        } else if (main == 1 || main == 3) {
            String imgFile = "files/CardBackH.png";
            try {
                img = ImageIO.read(new File(imgFile));
            } catch (IOException e) {
                System.out.println("Internal Error: " + e.getMessage());
            }
            int x;
            if (main == 1) {
                x = 80;
            } else {
                x = 1150;
            }
            int y = (int) (200 - (cards.size() / 2.0 * 20));
            for (int count = 0; count < cards.size(); count++) {
                g.drawImage(img, x, y, SIZE_XH, SIZE_YH, null);
                y += 20;
            }
        } else {
            String imgFile = "files/CardBackV.png";
            try {
                img = ImageIO.read(new File(imgFile));
            } catch (IOException e) {
                System.out.println("Internal Error: " + e.getMessage());
            }
            int y = 40;
            int x = (int) (1300 / 2 - 15 - cards.size() / 2.0 * 20);
            for (int count = 0; count < cards.size(); count++) {
                g.drawImage(img, x, y, SIZE_XV, SIZE_YV, null);
                x += 20;
            }
        }
    }
}
