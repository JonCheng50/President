import java.io.Serializable;

public class PJoinPacket implements Serializable {

    private int con;
    private String username;

    /**
     * PJoinPacket contains information on a new player joining the game
     * Used to update waiting for start screen with appropriate avatars
     *
     * @param con       The connection the player joined on
     * @param name      Username of player
     */
    public PJoinPacket(int con, String name) {
        this.con = con;
        username = name;
    }

    public String getUsername() {
        return username;
    }

    public int getCon() {
        return con;
    }
}
