import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * UserHand represents the hand of a CPU
 */
public class CPUHand extends Hand implements Serializable {
    private final int SIZE_XH = 70;
    private final int SIZE_YH = 50;
    private final int SIZE_XV = 50;
    private final int SIZE_YV = 70;

    private BufferedImage img;

    public static boolean qc = false;
    public static boolean clear2 = false;
    public static boolean skip = false;

    public CPUHand() {
        cards = new ArrayList<>();
    }

    // See Hand class for method documentation

    @Override
    void draw(Graphics g) {
        if (main == 1 || main == 3) {
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

    void addCard(String s) {
        if (s.equals("")){
            return;
        }
        Card c = new Card(s, false);
        cards.add(c);
    }

    public void removeCards(List<Card> l) {
        for (Card c : l) {
            cards.remove(c);
        }
    }

//    @Override
//    void decSize(int x) {}

    public void removeAll() {
        cards = new ArrayList<>();
    }

    public void sort() {
        Collections.sort(cards, new Card.SortCards());
    }

    // Plays card(s) depending on what is currently in the table, type, and remaining cards in hand
    @Override
    public boolean play(boolean first, int turn) {
        played = 0;
        qc = false;
        clear2 = false;
        skip = false;
        int type = GameCourt.getType();
        List<Card> cardsToPlay;
        if (first) {
            cardsToPlay = getSelectSmallestPlay();
            played = cardsToPlay.size();
            TableHand.getTable().addAll(cardsToPlay);
            removeCards(getSelected(cards));
            GameCourt.setType(cardsToPlay.size());
            return true;
        }
        // See if Quick Clear is possible
        for (int x = 0; x < cards.size(); x++) {
            Play QCp = null; // All possible plays with correct type to Quick Clear
            List<Card> cardsToPlayQC = null;
            if (type == 1 && x < cards.size() - 2) {
                cardsToPlayQC = cards.subList(x, x + 3);
                QCp = new Trip(cardsToPlayQC);
            } else if (type == 2 && x < cards.size() - 1) {
                cardsToPlayQC = cards.subList(x, x + 2);
                QCp = new Dub(cardsToPlayQC);
            } else if (type == 3) {
                cardsToPlayQC = cards.subList(x, x + 1);
                QCp = new Single(cardsToPlayQC);
            }
            if (QCp != null) {
                QCp.checkQuickClear();
                if (QCp.isQuickClear()) {
                    qc = true;
                    played = cardsToPlayQC.size();
                    for (Card c : cardsToPlayQC) {
                        c.select();
                    }
                    TableHand.getTable().addAll(cardsToPlayQC);
                    removeCards(getSelected(cards));
                    return true;
                }
            }
            //When the type is singles, possible to quick clear with singles and dubs as well
            if (type == 1 && x < cards.size() - 1) {
                cardsToPlayQC = cards.subList(x, x + 2);
                QCp = new Dub(cardsToPlayQC);
                QCp.checkQuickClear();
                if (QCp.isQuickClear()) {
                    qc = true;
                    played = cardsToPlayQC.size();
                    for (Card c : cardsToPlayQC) {
                        c.select();
                    }
                    TableHand.getTable().addAll(cardsToPlayQC);
                    removeCards(getSelected(cards));
                    return true;
                }
            }
            if (type == 1 && x < cards.size()) {
                cardsToPlayQC = cards.subList(x, x + 1);
                QCp = new Single(cardsToPlayQC);
                QCp.checkQuickClear();
                if (QCp.isQuickClear()) {
                    qc = true;
                    played = cardsToPlayQC.size();
                    for (Card c : cardsToPlayQC) {
                        c.select();
                    }
                    TableHand.getTable().addAll(cardsToPlayQC);
                    removeCards(getSelected(cards));
                    return true;
                }
            }
        }
        // See if normal play is possible
        for (int x = 0; x < cards.size() - (type - 1); x++) {
            Play p; // All possible plays of current type
            if (type == 1) {
                cardsToPlay = cards.subList(x, x + 1);
                p = new Single(cardsToPlay);
            } else if (type == 2) {
                cardsToPlay = cards.subList(x, x + 2);
                p = new Dub(cardsToPlay);
            } else if (type == 3) {
                cardsToPlay = cards.subList(x, x + 3);
                p = new Trip(cardsToPlay);
            } else {
                cardsToPlay = cards.subList(x, x + 4);
                p = new Quad(cardsToPlay);
            }
            // First is false because it is accounted for above
            if (p.isValid(false, true, true)) {
                played = cardsToPlay.size();
                int numPlayed = cardsToPlay.get(0).getNum();
                p.checkQuickClear();
                if (p.isQuickClear()){
                    qc = true;
                } else if (numPlayed == 2){
                    clear2 = true;
                } else if (p.isSkip()) {
                    skip = true;
                }
                //Note: need to select cards to be removed to avoid concurrent modification exception
                for (Card c : cardsToPlay) {
                    c.select();
                }
                TableHand.getTable().addAll(cardsToPlay);
                removeCards(getSelected(cards));
                return true;
            }
        }
        // Pass turn
        return false;
    }

    // Finds the smallest possible play, as in the single, dub, trip, or quad with the lowest number
    // Also selects those cards
    // Only for when this CPU is playing first
    private List<Card> getSelectSmallestPlay() {
        List<Card> avail = new ArrayList<>();
        for (int x = 0; x < cards.size(); x++) {
            avail.add(cards.get(x));
            if (x >= 3) {
                break;
            }
        }
        if (avail.size() == 1) {
            cards.get(0).select();
            return avail;
        }
        for (int x = 0; x < avail.size() - 1; x++) {
            cards.get(x).select();
            if (avail.get(x).getNum() != avail.get(x + 1).getNum()) {
                return avail.subList(0, x + 1);
            }
        }
        return avail;
    }

    public int getNumCards() {
        return cards.size();
    }

    public List<Card> getCards() {
        return cards;
    }
}
