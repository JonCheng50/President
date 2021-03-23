import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import javax.swing.Timer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * GameCourt: Contains all game data and logic for multiplayer/CPU play, also updates GUI
 */
@SuppressWarnings("serial")
public class GameCourt extends JPanel {

    private Game game;

    private int CPUnum;

    private static int turn;            // Which player's turn it is
    private int player;                 // Which player this GameCourt belongs to
    private static boolean first;       // Whether or not the card being played is first
    private static int type = 0;        // What type (single, dub, trip, quad) is being played
    private int clearCount = 0;         // Counts the number of skips in a row, if ==  3 then a clear occurs

    // the state of the game logic
    private List<String> deck = new ArrayList<>();
    private UserHand hand0;
    private Hand hand1;
    private Hand hand2;
    private Hand hand3;
    public TableHand table;
    private Hand hand;

    private Timer CPUTimer;
    private boolean CPUPause = false;

    private static final int INTERVAL = 2000; // How fast the CPUs play

    public boolean playing = false;           // whether the game is running

    private BufferedWriter writer;
    private BufferedReader reader;

    // Game constants
    public static final int COURT_WIDTH = 1300;
    public static final int COURT_HEIGHT = 600;
    public static final String GAME_FILE = "Saved_Game.txt";

    public static final String USERNAME = "username.txt";

    public static final List<String> SUITS = Arrays.asList("C", "D", "H", "S");
    public static final List<String> CARDS = Arrays.asList("1C", "1D", "1H", "1S",
                                                            "2C", "2D", "2H", "2S",
                                                            "3C", "3D", "3H", "3S",
                                                            "4C", "4D", "4H", "4S",
                                                            "5C", "5D", "5H", "5S",
                                                            "6C", "6D", "6H", "6S",
                                                            "7C", "7D", "7H", "7S",
                                                            "8C", "8D", "8H", "8S",
                                                            "9C", "9D", "9H", "9S",
                                                            "10C", "10D", "10H", "10S",
                                                            "11C", "11D", "11H", "11S",
                                                            "12C", "12D", "12H", "12S",
                                                            "13C", "13D", "13H", "13S");

    public GameCourt(Game game, int player) {
        this.game = game;
        this.player = player;

        // creates border around the court area, JComponent method
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Enable keyboard focus on the court area.
        // When this component has the keyboard focus, key events are handled by its key listener.
        setFocusable(true);

        // Timer for CPUs to check when it is their turn to play
        CPUTimer = new Timer(INTERVAL, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CPUCheck();
            }
        });

        // This Mouse Listener allows the user to select cards to play
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (playing) {
                    int x = e.getX();
                    int y = e.getY();
                    if (y >= 420 && y <= 566) {
                        // X value of mouse is on a card, where start_X is the x-pos of the 1st card
                        int x_in = x - hand.start_X;
                        if (x >= hand.start_X && x <= COURT_WIDTH - hand.start_X && x_in % 90 <= 80) {
                            int cardNum = x_in / 90;
                            hand.select(cardNum);
                            repaint();
                        }
                    }
                }
            }
        });

        // KeyListener allows card(s) to be played when the user presses the space bar
        addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (playing) {
                    // When the user tries to play the selected cards:
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        if (hand.play(first, turn)) {
                            if (UserHand.qc) {
                                CPUPause = true;
                                game.QuickClearMessage(player);
                                game.inputReceived(player, clearCount, type, table.getCards(), player, hand.played, 1);
                            } else if (UserHand.clear2) {
                                CPUPause = true;
                                game.ClearMessage(player);
                                game.inputReceived(player, clearCount, type, table.getCards(), player, hand.played, 2);
                            } else {
                                clearCount = 0;
                                turn = (turn + 1) % 4;
                                if (UserHand.skip) {
                                    turn = (turn + 1) % 4;
                                    clearCount ++;
                                    game.inputReceived(turn, clearCount, type, table.getCards(), player, hand.played, 3);
                                    game.SkipMessage();
                                } else {
                                    game.inputReceived(turn, clearCount, type, table.getCards(), player, hand.played, 0);
                                }
                                if (first) {
                                    first = false;
                                }
                            }
                        } else {
                            game.CantPlayMessage();
                        }
                        repaint();
                    }
                    // When the user wants to pass:
                    if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                        if (turn != player || first) {
                            game.CantPlayMessage();
                        } else {
                            turn = (turn + 1) % 4;
                            game.PassMessage();
                            clearCount++;
                            if (clearCount >= 3) {
                                CPUPause = true;
                                game.ClearMessage(turn);
                                game.inputReceived(turn, clearCount, type, table.getCards(), -1, 0, 2);
                            } else {
                                game.inputReceived(turn, clearCount, type, table.getCards(), -1,0, 4);
                            }
                        }
                    }
                    //TODO for Testing purposes
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        printAll();
                    }
                }
            }
        });
    }

    // Check for CPU plays, called by CPU Timer
    private void CPUCheck() {
        if (!CPUPause) { // Don't want CPU's playing in between a skip/play and a clear/quick clear
            boolean played;
            Hand h;
            int cpu;
            if (turn == 1 && CPUnum >= 3) {
                h = hand1;
                cpu = 1;
                played = hand1.play(first, -1); // Note CPUCheck accounts for turn
            } else if (turn == 2 && CPUnum >= 2) {
                h = hand2;
                cpu = 2;
                played = hand2.play(first, -1);
            } else if (turn == 3 && CPUnum >= 1) {
                h = hand3;
                cpu = 3;
                played = hand3.play(first, -1);
            } else {
                return;
            }
            repaint();
            if (CPUHand.qc) {
                CPUPause = true;
                game.QuickClearMessage(turn);
                game.inputReceived(turn, clearCount, type, table.getCards(), turn, h.played, 1);
            } else if (CPUHand.clear2) {
                CPUPause = true;
                game.ClearMessage(turn);
                game.inputReceived(turn, clearCount, type, table.getCards(), turn, h.played, 2);
            } else {
                turn = (turn + 1) % 4;
                if (played) {
                    clearCount = 0;
                    if (CPUHand.skip) {
                        turn = (turn + 1) % 4;
                        clearCount ++;
                        game.inputReceived(turn, clearCount, type, table.getCards(), cpu, h.played, 3);
                        game.SkipMessage();
                    } else {
                        game.inputReceived(turn, clearCount, type, table.getCards(), cpu, h.played, 0);
                    }
                    if (first) {
                        first = false;
                    }
                } else {
                    game.PassMessage();
                    clearCount ++;
                    if (clearCount >= 3) {
                        CPUPause = true;
                        game.ClearMessage(turn);
                        game.inputReceived(turn, clearCount, type, table.getCards(), -1, 0, 2);
                    } else {
                        game.inputReceived(turn, clearCount, type, table.getCards(), -1, 0, 4);
                    }
                }
            }
        }
    }


    //------------------------------------------------------------------------
    //                              Helper Methods
    //------------------------------------------------------------------------

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(COURT_WIDTH, COURT_HEIGHT);
    }

    public static void setTurn(int x) {
        turn = x;
    }

    public void setClearCount(int x) {
        clearCount = x;
    }

    public static void setFirst(boolean x) {
        first = x;
    }

    public static int getType() {
        return type;
    }

    public static void setType(int t) {
        type = t;
    }

    public void setHand0(UserHand hand0) {
        this.hand0 = hand0;
    }

    public void setHand1(Hand hand1) {
        this.hand1 = hand1;
    }

    public void setHand2(Hand hand2) {
        this.hand2 = hand2;
    }

    public void setHand3(Hand hand3) {
        this.hand3 = hand3;
    }

    public void setTable(TableHand table) {
        this.table = table;
    }

    // Changes size of hand **Only called on other players' hands
    public void changeHand(int p, int dec) {
        if (player == p) {
            return;
        }
        if (p == 0) {
            hand0.decSize(dec);
        } else if (p == 1) {
            hand1.decSize(dec);
        } else if (p == 2) {
            hand2.decSize(dec);
        } else if (p == 3) {
            hand3.decSize(dec);
        }
    }

    // Depending on the player, sets the main value for each Hand
    public void setMains() {
        if (player == 0) {
            hand0.setMain(0);
            hand = hand0;
            hand1.setMain(1);
            hand2.setMain(2);
            hand3.setMain(3);
        } else if (player == 1) {
            hand1.setMain(0);
            hand = hand1;
            hand2.setMain(1);
            hand3.setMain(2);
            hand0.setMain(3);
        } else if (player == 2) {
            hand2.setMain(0);
            hand = hand2;
            hand3.setMain(1);
            hand0.setMain(2);
            hand1.setMain(3);
        } else {
            hand3.setMain(0);
            hand = hand3;
            hand0.setMain(1);
            hand1.setMain(2);
            hand2.setMain(3);
        }
    }

    public void setCPUPause(boolean x) {
        CPUPause = x;
    }

    public void focus(){
        requestFocusInWindow();
    }

    //Checks if someone has won, and if so displays the winner and stops the game
    public void checkWin() {
        if (hand0.getNumCards() == 0) {
            game.statusLbl.setText("P0 wins!");
            game.statusLbl.setForeground(Color.BLACK);
            game.statusLbl.setFont(new Font("Tahoma", Font.BOLD, 11));
            playing = false;
            CPUTimer.stop();
        } else if (hand1.getNumCards() == 0) {
            game.statusLbl.setForeground(Color.BLACK);
            game.statusLbl.setFont(new Font("Tahoma", Font.BOLD, 11));
            playing = false;
            CPUTimer.stop();
            game.statusLbl.setText("P1 wins!");
        } else if (hand2.getNumCards() == 0) {
            game.statusLbl.setForeground(Color.BLACK);
            game.statusLbl.setFont(new Font("Tahoma", Font.BOLD, 11));
            playing = false;
            CPUTimer.stop();
            game.statusLbl.setText("P2 wins!");
        } else if (hand3.getNumCards() == 0) {
            game.statusLbl.setForeground(Color.BLACK);
            game.statusLbl.setFont(new Font("Tahoma", Font.BOLD, 11));
            playing = false;
            CPUTimer.stop();
            game.statusLbl.setText("P3 wins!");
        }

    }

    // Updates network, only called from Game after clear or qc
    public void updateNet(int turn) {
        game.inputReceived(turn, clearCount, type, table.getCards(), -1, 0, 0);
    }

    //------------------------------------------------------------------------
    //                        Reset Game to Initial State
    //------------------------------------------------------------------------

    public void reset(int CPUnum) {
        focus();
        if (player != 0) {
            System.out.println("Client cannot reset game");
            return;
        }
        this.CPUnum = CPUnum;
        turn = 0;
        first = true;
        clearCount = 0;

        //Clear the deck
        deck = new ArrayList<>();

        //Empties the table
        table = new TableHand();

        //Add all cards back into the deck
        for (String s : SUITS) {
            for (int n = 1; n <= 13; n++) {
                deck.add(n + s);
            }
        }
        Collections.shuffle(deck);

        //Adds shuffled cards to players' and CPUs' hands
        hand0 = new UserHand(0);
        if (CPUnum >= 3) {
            hand1 = new CPUHand();
        } else {
            hand1 = new UserHand(1);
        }
        if (CPUnum >= 2) {
            hand2 = new CPUHand();
        } else {
            hand2 = new UserHand(2);
        }
        if (CPUnum >= 1) {
            hand3 = new CPUHand();
        } else {
            hand3 = new UserHand(3);
        }

        for (int x = 0; x < 13; x++) {
            hand0.addCard(deck.get(x));
        }
        for (int x = 13; x < 26; x++) {
            hand1.addCard(deck.get(x));
        }
        for (int x = 26; x < 39; x++) {
            hand2.addCard(deck.get(x));
        }
        for (int x = 39; x < 52; x++) {
            hand3.addCard(deck.get(x));
        }

        hand0.sort();
        hand1.sort();
        hand2.sort();
        hand3.sort();

        if (player == 0) {
            game.startup(hand0, hand1, hand2, hand3, table.getCards());
        }

        playing = true;
        if (CPUnum != 0) {
            CPUTimer.start();
        }
        repaint();

        //requestFocusInWindow();
    }

    //------------------------------------------------------------------------
    //                               Repaint
    //------------------------------------------------------------------------

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // If game has not started yet:
        if (hand1 == null || hand2 == null || hand3 == null || table == null) {
            return;
        }

        hand0.draw(g);
        hand1.draw(g);
        hand2.draw(g);
        hand3.draw(g);
        table.draw(g);

        turn = turn % 4;

        game.statusLbl.setForeground(Color.BLACK);
        game.statusLbl.setFont(new Font("Tahoma", Font.PLAIN, 11));
        if (turn == player) {
            game.statusLbl.setText("Your turn");
        } else {
            game.statusLbl.setText(Game.usernames[(turn - player + 4) % 4] + "'s turn");
        }

        if (first) {
            game.typeLbl.setText("Type: ");
        } else if (type == 1) {
            game.typeLbl.setText("Type: Single");
        } else if (type == 2) {
            game.typeLbl.setText("Type: Double");
        } else if (type == 3) {
            game.typeLbl.setText("Type: Triple");
        } else if (type == 4) {
            game.typeLbl.setText("Type: Quad");
        } else {
            game.typeLbl.setText("Type: ");
        }
        checkWin();
    }

    //TODO TESTING PURPOSES - DELETE
    // Prints the cards of all players
    public void printAll() {
        System.out.println("---------PRINTING ALL---------");
        System.out.println("P0 Hand:");
        String a = "";
        for (Card c : hand0.getCards()) {
            a += c.getName() + ", ";
        }
        System.out.println(a);
        System.out.println("P1 Hand:");
        String m = "";
        for (Card c : hand1.getCards()) {
            m += c.getName() + ", ";
        }
        System.out.println(m);
        System.out.println("P2 Hand:");
        String b = "";
        for (Card c : hand2.getCards()) {
            b += c.getName() + ", ";
        }
        System.out.println(b);
        System.out.println("P3 Hand:");
        String x = "";
        for (Card c : hand3.getCards()) {
            x += c.getName() + ", ";
        }
        System.out.println(x);
        System.out.println("-----------------------");
    }

    //------------------------------------------------------------------------
    //                          Save and Load Game
    //------------------------------------------------------------------------

    // CURRENTLY NOT IMPLEMENTED IN MULTIPLAYER GAME

    public void save() {
//        try {
//            writer = new BufferedWriter(new FileWriter(GAME_FILE));
//            for (Card c : TableHand.getTable()){
//                writer.write(c.getName() + " ");
//            }
//            writer.newLine();
//            for (Card c : hand.getCards()) {
//                writer.write(c.getName() + " ");
//            }
//            writer.newLine();
//            for (Card c : cpu1.getCards()) {
//                writer.write(c.getName() + " ");
//            }
//            writer.newLine();
//            for (Card c : cpu2.getCards()) {
//                writer.write(c.getName() + " ");
//            }
//            writer.newLine();
//            for (Card c : cpu3.getCards()) {
//                writer.write(c.getName() + " ");
//            }
//            writer.newLine();
//            writer.write("" + type);
//            writer.newLine();
//            writer.write("" + turn);
//            writer.newLine();
//            writer.write("" + clearCount);
//
//            writer.close();
//
//        } catch (IOException e){
//            System.out.println("IO Exception in saving game");
//            requestFocusInWindow();
//            return;
//        }
//        System.out.println("Game Saved!");
//        requestFocusInWindow();
    }

    public void load() {
//        try {
//            if (!checkValidFile()){
//                System.out.println("Saved_Game file is not valid and could not be loaded");
//                requestFocusInWindow();
//                return;
//            }
//            reader = new BufferedReader(new FileReader(GAME_FILE));
//            table.removeAll();
//            hand.removeAll();
//            cpu1.removeAll();
//            cpu2.removeAll();
//            cpu3.removeAll();
//            first = false;
//            String line = reader.readLine();
//            if (line.equals("")) {
//                first = true;
//            }
//            String[] split = line.split(" ");
//            for (String s : split){
//                table.addCard(s);
//            }
//            line = reader.readLine();
//            split = line.split(" ");
//            for (String s : split){
//                hand.addCard(s);
//            }
//            line = reader.readLine();
//            split = line.split(" ");
//            for (String s : split){
//                cpu1.addCard(s);
//            }
//            line = reader.readLine();
//            split = line.split(" ");
//            for (String s : split){
//                cpu2.addCard(s);
//            }
//            line = reader.readLine();
//            split = line.split(" ");
//            for (String s : split){
//                cpu3.addCard(s);
//            }
//            line = reader.readLine();
//            type = Integer.parseInt(line);
//            line = reader.readLine();
//            turn = Integer.parseInt(line);
//            line = reader.readLine();
//            clearCount = Integer.parseInt(line);
//            reader.close();
//        } catch (IOException e){
//            System.out.println("IO Exception in loading game");
//            requestFocusInWindow();
//            return;
//        }
//        hand.sort();
//        cpu1.sort();
//        cpu2.sort();
//        cpu3.sort();
//        playing = true;
//        CPUTimer.start();
//        repaint();
//        System.out.println("Game loaded!");
//        requestFocusInWindow();
    }

    public boolean checkValidFile(){
//        BufferedReader checker;
//        try {
//            checker = new BufferedReader(new FileReader(GAME_FILE));
//        } catch (IOException e){
//            System.out.println("IO Exception in loading game: game file not found");
//            return false;
//        }
//        try {
//            String line;
//            String[] split;
//            // Check the hands of cards
//            List<String> dups = new ArrayList<>();
//            for (int count = 0; count < 5; count++) {
//                line = checker.readLine();
//                if (line.equals("")){
//                    continue;
//                }
//                split = line.split(" ");
//                for (String s : split) {
//                    if (!(CARDS.contains(s))) {
//                        System.out.println("Loading Error: Invalid card");
//                        return false;
//                    }
//                    if (dups.contains(s)){
//                        System.out.println("Loading Error: Duplicate card");
//                        return false;
//                    }
//                    dups.add(s);
//                }
//            }
//            line = checker.readLine();
//            try {
//                // Check type
//                int t = Integer.parseInt(line);
//                if (!(t >= 0 && t <= 4)){
//                    System.out.println("Loading Error: Type must be between 0 and 4 inclusive");
//                    return false;
//                }
//                // Check turn
//                line = checker.readLine();
//                t = Integer.parseInt(line);
//                if (!(t >= 0 && t <= 3)){
//                    System.out.println("Loading Error: Turn must be between 0 and 3 inclusive");
//                    return false;
//                }
//                // Check ClearCount
//                line = checker.readLine();
//                t = Integer.parseInt(line);
//                if (!(t >= 0 && t <= 3)){
//                    System.out.println("Loading Error: ClearCount must be between 0 and 3 inclusive");
//                    return false;
//                }
//            } catch (NumberFormatException e){
//                System.out.println("NumberFormat Exception: Type, turn, and clearCount must be an int");
//                return false;
//            }
//        } catch (IOException e) {
//            System.out.println("IO Exception in loading game: ");
//            return false;
//        }
//        return true;
        return false;
    }
}