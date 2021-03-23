import javax.swing.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main: Class that is run to start the game (ServerGame or ClientGame accordingly)
 */
public class Main {

    private static int start = 0;
    private static Timer timer = new Timer(true);       // Checks for when space has been pressed
    private static Timer numTimer = new Timer(true);    // Checks for packet from ServerGame

    private static int numP;

    public static void main(String[] args) {

        JFrame menu = new JFrame();
        JButton button = new JButton();
        JLabel logo = new JLabel(new ImageIcon("files/PresLogo.png"));

        button.setText("Play!");        // Button not actually shown, space bar is used to press
        menu.add(button);
        menu.add(logo);
        menu.pack();
        menu.setLocation(500, 330);
        menu.setVisible(true);

        button.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu.dispose();
                start = 1;
            }
        });

        timer.schedule(new checker(), 0, 500);

        menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // Tries to establish connection with ServerGame. If none, creates new ServerGame
    static class checker extends TimerTask {
        public void run() {
            if (start == 1) {
                try {
                    timer.cancel();
                    Socket s = new Socket("00.000.000.00", Game.PORT);
                    Connection con = new Connection(s);
                    numTimer.schedule(new numCheck(), 0, 250);
                } catch (IOException e) {
                    System.out.println("Starting new game");
                    new ServerGame();
                }
            }
        }
    }

    // Receives Integer packet from ServerGame to start ClientGame of correct player num
    static class numCheck extends TimerTask {
        public void run() {
            if (numP != 0) {
                numTimer.cancel();
                if (numP > 3) {
                    System.out.println("Game is full");
                    System.exit(0);
                }
                new ClientGame(numP);
            }
        }
    }

    public static void numReceived(Object o) {
        if (o instanceof Integer) {
            numP = (int) o;
        }
    }

}
