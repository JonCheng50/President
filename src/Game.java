// imports necessary libraries for Java swing
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Game: contains all GUI elements and related methods, including the GameCourt
 */
public abstract class Game {

    static final int PORT = 55555;

    /**
     * InputReceived on the ServerGame is called whenever a player plays anything, and sends an UpdatePacket
     * to the client
     *
     * @param turn          who's turn it is (0-3)
     * @param clearCount    what the clearCount is (0-2)
     * @param type          what the type is (1-3)
     * @param t             list of cards in the table
     * @param who           who played the previous card (-1-3), -1 means no one played
     * @param num           number of cards played in previous turn (0-12)
     * @param msg           what message should be shown to everyone (0-4)
     *                      0: no message
     *                      1: QuickClear
     *                      2: Clear
     *                      3: Skip
     *                      4: Pass
     *
     * Note who and num are used only to update the number of cards the other players have that
     * you see on your screen.
     */
    public abstract void inputReceived(int turn, int clearCount, int type, List<Card> t, int who, int num, int msg);

    /**
     * packetReceived is called when a Game receives a packet sent by another Game
     *
     * @param object        packet that was sent (StartPacket, PJoinPacket, UpdatePacket)
     *
     * Note object must be checked/casted into appropriate packet type
     */
    public abstract void packetReceived(Object object);

    /**
     * Startup sends a startPacket to the client game with all the necessary information to set up the game
     * Only to be called by ServerGame
     */
    public abstract void startup(UserHand h0, Hand h1, Hand h2, Hand h3, List<Card> t);

    public abstract void close();

    private static final List<Avatar> avatars;
    String username;
    static String[] usernames;

    int CPUnum = 3;                             // Number of CPU players

    private static JLabel uName1;
    private static JLabel uName2;
    private static JLabel uName3;

    private final JPanel eMessage_Panel;        // Message Panel for Can't play that!
    private final JPanel qMessage_Panel;        // Message Panel for Quick Clear!
    private final JPanel cMessage_Panel;        //Message Panel for Clear
    private final JPanel sMessage_Panel;        // Message Panel for Skipped!
    private final JPanel pMessage_Panel;        // Message Panel for Passed
    final JLabel typeLbl;                       // Label for type of card
    final JLabel statusLbl;                     // Label for status messages

    // Arrows showing who's turn it is
    private static final JLabel arrowMain;
    private static final JLabel arrowUp;
    private static final JLabel arrowLeft;
    private static final JLabel arrowRight;

    // Elements in waiting for game to start
    private final JButton startButton;
    private final JLabel waitLbl;
    private Timer waitTimer;
    private boolean waiting = true;
    private int count;

    static {
        arrowMain = new JLabel(new ImageIcon("files/ArrowMain.png"));
        arrowMain.setBounds(615, 327, 70, 35);
        arrowMain.setVisible(false);

        arrowUp = new JLabel(new ImageIcon("files/ArrowUp.png"));
        arrowUp.setBounds(615, 138, 70, 35);
        arrowUp.setVisible(false);

        arrowLeft = new JLabel(new ImageIcon("files/ArrowLeft.png"));
        arrowLeft.setBounds(555, 210, 35, 70);
        arrowLeft.setVisible(false);

        arrowRight = new JLabel(new ImageIcon("files/ArrowRight.png"));
        arrowRight.setBounds(710, 210, 35, 70);
        arrowRight.setVisible(false);

        // Player avatars, where you are Avatar 0
        Avatar A = new Avatar(0);
        Avatar B = new Avatar(1);
        Avatar C = new Avatar(2);
        Avatar D = new Avatar(3);
        avatars = new ArrayList<>(Arrays.asList(A, B, C, D));

        usernames = new String[]{"", "cpuGary", "cpuStanley", "cpuBob"};
    }

    GameCourt court;

    public Game(int player) {

        // Top-level frame where game components are
        final JFrame frame = new JFrame("President - P" + player);
        frame.setLocation(70, 125);
        frame.setResizable(false);

        // Start Button for serverGame
        startButton = new JButton("Start Game!");
        startButton.setBounds(550, 270, 200, 60);
        startButton.setForeground(new Color(56, 191, 74));
        startButton.setFont(new Font("Tahoma", Font.BOLD, 20));
        startButton.addActionListener(evt -> {
            waiting = false;
            endWait();
            court.reset(CPUnum);
        });
        startButton.setVisible(false);

        // Waiting text label for clientGame
        waitLbl = new JLabel("Waiting for server to start game");
        waitLbl.setBounds(450, 270, 400, 60);
        waitLbl.setForeground(Color.RED);
        waitLbl.setFont(new Font("Tahoma", Font.BOLD, 20));
        waitLbl.setVisible(false);

        // Username labels
        uName1 = new JLabel();
        uName1.setHorizontalAlignment(SwingConstants.CENTER);
        uName1.setVerticalAlignment(SwingConstants.CENTER);
        uName1.setBounds(10, 215,60, 18);
        uName1.setFont(new Font("Tahoma", Font.PLAIN, 14));
        uName1.setVisible(false);

        uName2 = new JLabel();
        uName2.setHorizontalAlignment(SwingConstants.CENTER);
        uName2.setVerticalAlignment(SwingConstants.CENTER);
        uName2.setBounds(620, 11,60, 18);
        uName2.setFont(new Font("Tahoma", Font.PLAIN, 14));
        uName2.setVisible(false);

        uName3 = new JLabel();
        uName3.setHorizontalAlignment(SwingConstants.CENTER);
        uName3.setVerticalAlignment(SwingConstants.CENTER);
        uName3.setBounds(1230, 215,60, 18);
        uName3.setFont(new Font("Tahoma", Font.PLAIN, 14));
        uName3.setVisible(false);

        // Status panel
        final JPanel status_panel = new JPanel();
        frame.add(status_panel, BorderLayout.SOUTH);
        statusLbl = new JLabel("");
        status_panel.add(statusLbl);

        // Type Label
        final JPanel stmessage_panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        stmessage_panel.setBounds(5, 558, 120, 20);
        typeLbl = new JLabel("Type: ");
        typeLbl.setFont(new Font("Tohoma", Font.PLAIN, 12));
        stmessage_panel.add(typeLbl);

        // Can't play selected cards message or skip when it's not your turn/the first turn
        eMessage_Panel = new JPanel();
        eMessage_Panel.setBounds(550, 375, 200, 30);
        final JLabel eMessage = new JLabel("Can't play that!", SwingConstants.CENTER);
        eMessage.setForeground(Color.RED);
        eMessage.setFont(new Font("Tahoma", Font.BOLD, 15));
        eMessage_Panel.add(eMessage);
        eMessage_Panel.setVisible(false);

        // Quick clear message
        qMessage_Panel = new JPanel();
        qMessage_Panel.setBounds(550, 120, 200, 50);
        final JLabel qMessage = new JLabel("Quick Clear!", SwingConstants.CENTER);
        qMessage.setForeground(Color.CYAN);
        qMessage.setFont(new Font("Tahoma", Font.BOLD, 20));
        qMessage_Panel.add(qMessage);
        qMessage_Panel.setVisible(false);

        // Clear message
        cMessage_Panel = new JPanel();
        cMessage_Panel.setBounds(550, 120, 200, 50);
        final JLabel cMessage = new JLabel("Clear!", SwingConstants.CENTER);
        cMessage.setForeground(new Color(69, 104, 191));
        cMessage.setFont(new Font("Tahoma", Font.BOLD, 20));
        cMessage_Panel.add(cMessage);
        cMessage_Panel.setVisible(false);

        // Skipped message
        sMessage_Panel = new JPanel();
        sMessage_Panel.setBounds(550, 120, 200, 50);
        final JLabel sMessage = new JLabel("Skipped!", SwingConstants.CENTER);
        sMessage.setForeground(Color.RED);
        sMessage.setFont(new Font("Tahoma", Font.BOLD, 20));
        sMessage_Panel.add(sMessage);
        sMessage_Panel.setVisible(false);

        // Passed message
        pMessage_Panel = new JPanel();
        pMessage_Panel.setBounds(550, 120, 200, 50);
        final JLabel pMessage = new JLabel("Passed", SwingConstants.CENTER);
        pMessage.setForeground(Color.ORANGE);
        pMessage.setFont(new Font("Tahoma", Font.BOLD, 20));
        pMessage_Panel.add(pMessage);
        pMessage_Panel.setVisible(false);


        // Main playing area where all the Hands are
        court = new GameCourt(this, player);
        frame.add(court);
        court.setLayout(null);
        court.add(startButton);

        // Ask for username
        username = askUsername();
        List<JLabel> e = avatars.get(0).draw(username);
        court.add(e.get(0));
        court.add(e.get(1));


        court.add(uName1);
        court.add(uName2);
        court.add(uName3);
        court.add(waitLbl);
        court.add(eMessage_Panel);
        court.add(qMessage_Panel);
        court.add(sMessage_Panel);
        court.add(cMessage_Panel);
        court.add(pMessage_Panel);
        court.add(stmessage_panel);
        court.add(arrowMain);
        court.add(arrowUp);
        court.add(arrowLeft);
        court.add(arrowRight);


        //------------------------------------------------------------------------
        //                             Control Panel
        //------------------------------------------------------------------------

        // Reset and Save buttons
        final JPanel control_panel = new JPanel();

        final JButton reset = new JButton("Reset");
        reset.addActionListener(e13 -> {
            if (waiting) {
                return;
            }
            court.reset(CPUnum);
        });
        control_panel.add(reset);

        // Saving and Loading not currently implemented in multiplayer game
//        // Save button
//        final JButton save = new JButton("Save Game");
//        save.addActionListener(e1 -> court.save());
//        control_panel.add(save);
//
//        // Load button
//        final JButton load = new JButton("Load Game");
//        load.addActionListener(e12 -> court.load());
//        control_panel.add(load);

        // Instructions button
        final JButton instructions = new JButton("Instructions");
        instructions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, "Instructions: \n" +
                        "This is a card game called President. The goal of the game is to get rid of " +
                        "all of your cards. \n On your turn, you can either play a card that is greater" +
                        " than or equal to the previous card played, or skip. \n You must play a card of" +
                        " the same type as previously played. i.e. you can only play a single on a single," +
                        " a double on a double, and so on. \n Doubles, triples, and quads must be the same " +
                        "number. \n If an equal numbered " +
                        "card is played, then the next person gets skipped. \n If an entire rotation is " +
                        "made and everyone skips, the table is cleared and the person who last " +
                        "played can start again. \n 2 is the highest card and will automatically " +
                        "clear the table when played. \n Even when it's not your turn, you can quick clear" +
                        " by completing a set of 4 of the topmost cards on the table." +
                        "\n Controls:" +
                        "\n Click reset to restart the game" +
                        "\n Click Save/Load to save the game/load a previously saved game" +
                        "\n Click on card(s) to select them" +
                        "\n Press space to play selected cards" +
                        "\n Press shift to skip your turn");
                court.focus();
            }
        });
        control_panel.add(instructions);

        frame.add(control_panel, BorderLayout.NORTH);

        // Put the frame on the screen
        frame.pack();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.addWindowListener(new Listener()) ;
        frame.setVisible(true);
    }

    //------------------------------------------------------------------------
    //                   Waiting for Game to Start Methods
    //------------------------------------------------------------------------

    // Shows the waiting screen for the respective player
    void waitForStart(int player, String username) {
        if (player == 0) {
            startButton.setVisible(true);
            //playerJoin(0, 0, username);
        } else {
            waitLbl.setVisible(true);
            count = 1;
            waitTimer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (count % 4 == 0) {
                        waitLbl.setText("Waiting for server to start game");
                    } else if (count % 4 == 1) {
                        waitLbl.setText("Waiting for server to start game.");
                    } else if (count % 4 == 2) {
                        waitLbl.setText("Waiting for server to start game..");
                    } else {
                        waitLbl.setText("Waiting for server to start game...");
                    }
                    //waitLbl.setText("Waiting for server to start game" + ".".repeat(count % 4));
                    count ++;
                }
            });
            waitTimer.start();
        }
    }

    // Shows the player numbers that have joined from 0 to 3
    void playerJoin(int player, int p, String username) {
        System.out.println("Player joined: " + username);
        usernames[(p - player + 4) % 4] = username;
        List<JLabel> e = avatars.get((p - player + 4) % 4).draw(username);
        court.add(e.get(0));
        court.add(e.get(1));
        court.repaint();
    }

    // When the server starts the game
    void endWait() {
        try {
            waitTimer.stop();
        } catch (NullPointerException ignored) {}
        startButton.setVisible(false);
        court.remove(startButton);
        waitLbl.setVisible(false);
        court.remove(waitLbl);
        for (Avatar a : avatars) {
            List<JLabel> e = a.getElements();
            if (e == null) {
                continue;
            }
            e.get(0).setVisible(false);
            e.get(1).setVisible(false);
        }
    }

    //------------------------------------------------------------------------
    //                        Message Panels Methods
    //------------------------------------------------------------------------

    //Shows message to user : can't play this!
    void CantPlayMessage() {
        eMessage_Panel.setVisible(true);
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eMessage_Panel.setVisible(false);
            }
        });
        timer.setRepeats(false);
        timer.restart();
    }

    //Shows message to user: Quick Clear!
    void QuickClearMessage(int player) {
        qMessage_Panel.setVisible(true);
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                qMessage_Panel.setVisible(false);
                TableHand.clear(player);
                court.setClearCount(0);
                court.repaint();
                court.updateNet(player);
                //game.inputReceived(player, clearCount, type, table.getCards(), -1, 0, 0);
                court.setCPUPause (false);
            }
        });
        timer.setRepeats(false);
        timer.restart();
    }

    // Called when another player Quick Clears to show message
    void OtherQuickClear() {
        qMessage_Panel.setVisible(true);
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                qMessage_Panel.setVisible(false);
            }
        });
        timer.setRepeats(false);
        timer.restart();
    }

    // Shows message to user: Clear!
    void ClearMessage(int player) {
        cMessage_Panel.setVisible(true);
        Timer timer = new Timer(1250, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cMessage_Panel.setVisible(false);
                TableHand.clear(player);
                court.setClearCount(0);
                court.repaint();
                court.updateNet(player);
                court.setCPUPause (false);
            }
        });
        timer.setRepeats(false);
        timer.restart();
    }

    // Called when another player clears the table
    void OtherClear() {
        cMessage_Panel.setVisible(true);
        Timer timer = new Timer(1250, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cMessage_Panel.setVisible(false);
            }
        });
        timer.setRepeats(false);
        timer.restart();
    }

    // Shows message to user: Skipped!
    void SkipMessage() {
        sMessage_Panel.setVisible(true);
        Timer timer = new Timer(750, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sMessage_Panel.setVisible(false);
            }
        });
        timer.setRepeats(false);
        timer.restart();
    }

    // Called when another player skips someone
    void OtherSkip() {
        sMessage_Panel.setVisible(true);
        Timer timer = new Timer(750, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sMessage_Panel.setVisible(false);
            }
        });
        timer.setRepeats(false);
        timer.restart();
    }

    // Shows message to user: Passed
    void PassMessage() {
        pMessage_Panel.setVisible(true);
        Timer timer = new Timer(750, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pMessage_Panel.setVisible(false);
            }
        });
        timer.setRepeats(false);
        timer.restart();
    }

    // Called when another player passes their turn
    void OtherPass() {
        pMessage_Panel.setVisible(true);
        Timer timer = new Timer(750, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pMessage_Panel.setVisible(false);
            }
        });
        timer.setRepeats(false);
        timer.restart();
    }

    //------------------------------------------------------------------------
    //                            Helper Methods
    //------------------------------------------------------------------------

    // Changes the arrow according to whose turn it is, p = player and t = turn
    static void changeArrow(int p, int t) {
        arrowMain.setVisible(false);
        arrowUp.setVisible(false);
        arrowLeft.setVisible(false);
        arrowRight.setVisible(false);
        if (p == t) {
            arrowMain.setVisible(true);
        } else if (t == (p + 1) % 4) {
            arrowLeft.setVisible(true);
        } else if (t == (p + 2) % 4) {
            arrowUp.setVisible(true);
        } else {
            arrowRight.setVisible(true);
        }
    }

    // Sets username texts
    static void setUsernames() {
        uName1.setText(usernames[1]);
        uName1.setVisible(true);
        uName2.setText(usernames[2]);
        uName2.setVisible(true);
        uName3.setText(usernames[3]);
        uName3.setVisible(true);
    }

    // Opens dialog box for user to input username, where previous username is stored as default in Username.txt
    private String askUsername() {
        String name = "";
        BufferedWriter writer;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("Username.txt"));
            name = reader.readLine();
            reader.close();
        } catch (IOException ignored) {}
        name = (JOptionPane.showInputDialog("Please input your username", name));
        if (name == null) {
            System.exit(0);
        }
        try {
            writer = new BufferedWriter(new FileWriter("Username.txt"));
            writer.write(name);
            writer.close();
        } catch (IOException ignored){}
        usernames[0] = name;
        return name;
    }

    class Listener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            close();
        }
    }
}