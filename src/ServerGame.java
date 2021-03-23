import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * ServerGame is created by the first player who joins (P0). Handles any CPUs and communications between ClientGames
 */
public class ServerGame extends Game {

    private static int numP;         // Number of players

    // Players' usernames
    private String username;
    private String user1;
    private String user2;
    private String user3;

    private ServerSocket serverSocket;
    private Socket socket;

    // Connections between all ClientGames
    private Connection conMain;
    private Connection connection1;
    private Connection connection2;
    private Connection connection3;

    public ServerGame() {
        super(0);
        numP = 0;
        super.waitForStart(0, super.username);

        // Waiting for a max of 3 clients to join the game
        // First connects to Main to send packet on what player that client should be
        try {
            serverSocket = new ServerSocket(PORT);
            while (numP < 6) {
                socket = serverSocket.accept();
                numP++;
                if (numP == 1) {
                    conMain = new Connection(this, socket);
                    conMain.sendPacket(1);
                    conMain.close();
                } else if (numP == 2) {
                    connection1 = new Connection(this, socket);
                    System.out.println("CLient 1 has connected");
                    connection1.sendPacket(new PJoinPacket(0, super.username));
                    super.CPUnum = 2;
                } else if (numP == 3) {
                    conMain = new Connection(this, socket);
                    conMain.sendPacket(2);
                    conMain.close();
                } else if (numP == 4) {
                    connection2 = new Connection(this, socket);
                    System.out.println("CLient 2 has connected");
                    connection2.sendPacket(new PJoinPacket(0, super.username));
                    super.CPUnum = 1;
                } else if (numP == 5) {
                    conMain = new Connection(this, socket);
                    conMain.sendPacket(3);
                    conMain.close();
                } else if (numP == 6) {
                    connection3 = new Connection(this, socket);
                    System.out.println("CLient 3 has connected");
                    connection3.sendPacket(new PJoinPacket(0, super.username));
                    super.CPUnum = 0;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // See Game class for method documentation

    @Override
    public void startup(UserHand h0, Hand h1, Hand h2, Hand h3, List<Card> t) {
        court.setMains();
        try {
            connection1.sendPacket(new StartPacket(h0, h1, h2, h3, t));
            connection2.sendPacket(new StartPacket(h0, h1, h2, h3, t));
            connection3.sendPacket(new StartPacket(h0, h1, h2, h3, t));
        } catch (NullPointerException e){
            System.out.println( 3 - numP / 2 + " CPUs have connected");
        }
        Game.changeArrow(0, 0);
        Game.setUsernames();
        court.repaint();
        numP = 6;

    }

    @Override
    public void inputReceived(int turn, int clearCount, int type, List<Card> t, int who, int num, int msg) {
        Game.changeArrow(0, turn);
        try {
            connection1.sendPacket(new UpdatePacket(0, turn, clearCount, type, t, who, num, msg));
            connection2.sendPacket(new UpdatePacket(0, turn, clearCount, type, t, who, num, msg));
            connection3.sendPacket(new UpdatePacket(0, turn, clearCount, type, t, who, num, msg));
        } catch (NullPointerException ignored) {}
        court.repaint();
    }

    @Override
    public void packetReceived(Object object) {
        if (object instanceof UpdatePacket) {
            UpdatePacket packet = (UpdatePacket) object;

            // Send UpdatePacket to other clients (not sent back to same client that set the original update)
            try {
                if (packet.getCon() == 1) {
                    connection2.sendPacket(packet);
                    connection3.sendPacket(packet);
                } else if (packet.getCon() == 2) {
                    connection1.sendPacket(packet);
                    connection3.sendPacket(packet);
                } else {
                    connection1.sendPacket(packet);
                    connection2.sendPacket(packet);
                }
            } catch (NullPointerException ignored) {}

            // Update server's game
            GameCourt.setTurn(packet.getTurn());
            court.setClearCount(packet.getClearCount());
            GameCourt.setType(packet.getType());
            if (packet.getT().size() == 0) {
                TableHand.clear();
            } else {
                court.table.addCard(packet.getT());
                GameCourt.setFirst(false);
            }
            court.changeHand(packet.getWho(), packet.getNum());
            Game.changeArrow(0, packet.getTurn());
            court.repaint();

            // Show any messages if necessary
            int m = packet.getMsg();
            if (m == 1) {
                OtherQuickClear();
            } else if (m == 2) {
                OtherClear();
            } else if (m == 3) {
                OtherSkip();
            } else if (m == 4) {
                OtherPass();
            }
        } else if (object instanceof PJoinPacket) {
            PJoinPacket packet =  (PJoinPacket) object;
            String user = packet.getUsername();
            int con = packet.getCon();
            playerJoin(0, con, user);
            if (con == 1) {
                user1 = user;
            } else if (con == 2) {
                user2 = user;
                connection1.sendPacket(new PJoinPacket(2, user2));
                connection2.sendPacket(new PJoinPacket(1, user1));
            } else if (con == 3) {
                user3 = user;
                connection1.sendPacket(new PJoinPacket(3, user3));
                connection2.sendPacket(new PJoinPacket(3, user3));
                connection3.sendPacket(new PJoinPacket(1, user1));
                connection3.sendPacket(new PJoinPacket(2, user2));
            }
        }
    }

    @Override
    public void close() {
        try {
            connection1.close();
            connection2.close();
            connection3.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
