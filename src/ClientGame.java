import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

/**
 * ClientGame is created for the 2nd, 3rd, and 4th players to join the game
 */
public class  ClientGame extends Game {

    private int player;

    private Socket socket;
    private Connection connection;

    ClientGame(int player) {
        super(player);
        this.player = player;
        super.waitForStart(player, super.username);
        try {
            String ip = null;
            try {
                BufferedReader reader = new BufferedReader(new FileReader("IP.txt"));
                ip = reader.readLine();
                reader.close();
            } catch (IOException ignored){}
            if (ip == null || ip.isEmpty()) {
                socket = new Socket("00.000.000.00", PORT);
            } else {
                socket = new Socket(ip, PORT);
            }
            System.out.println("Connected to Server");
            connection = new Connection(this, socket);
        } catch (ConnectException e) {
            throw new IllegalArgumentException();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.sendPacket(new PJoinPacket(player, super.username));
    }

    // See Game class for documentation

    @Override
    public void inputReceived(int turn, int clearCount, int type, List<Card> t, int who, int num, int msg) {
        connection.sendPacket(new UpdatePacket(player, turn, clearCount, type, t, who, num, msg));
        Game.changeArrow(player, turn);
        court.repaint();
    }

    @Override
    public void startup(UserHand h0, Hand h1, Hand h2, Hand h3, List<Card> t) {}

    @Override
    public void packetReceived(Object object) {
        if (object instanceof UpdatePacket) {
            UpdatePacket packet = (UpdatePacket) object;

            // Update ClientGame
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
            Game.changeArrow(player, packet.getTurn());
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
        } else if (object instanceof StartPacket) {
            // Essentially setting game to that of the ServerGame
            super.endWait();
            StartPacket packet = (StartPacket) object;
            court.playing = true;
            court.focus();
            court.setHand1(packet.getH1());
            court.setHand2(packet.getH2());
            court.setHand3(packet.getH3());
            court.setHand0(packet.getH0());
            TableHand tab = new TableHand();
            tab.addCard(packet.getT());
            court.setTable(tab);
            court.setMains();
            GameCourt.setTurn(0);
            court.setClearCount(0);
            GameCourt.setFirst(true);
            Game.changeArrow(player, 0);
            Game.setUsernames();
            court.repaint();
        } else if (object instanceof PJoinPacket){
            PJoinPacket packet = (PJoinPacket) object;
            int con = packet.getCon();
            String username = packet.getUsername();
            playerJoin(player, con, username);
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
