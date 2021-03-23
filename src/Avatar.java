import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Avatar image and username used while waiting for game to start
 */
public class Avatar extends JLabel {
    private String username;
    private int p;
    private List<JLabel> elements;

    public Avatar(int p) {
        this.p = p;
        this.setVisible(false);
    }

    // Returns list of appropriate username and avatar image
    public List<JLabel> draw(String n) {
        elements = new ArrayList<JLabel>();
        username = n;
        int x;
        int y;
        if (p == 0) {
            x = 594;
            y = 468;
        } else if (p == 1) {
            x = 20;
            y = 244;
        } else if (p == 2) {
            x = 594;
            y = 20;
        } else {
            x = 1168;
            y = 244;
        }
        JLabel pic = new JLabel(new ImageIcon("files/Avatar.png"));
        pic.setBounds(x, y, 112, 112);
        JLabel name = new JLabel(username);
        name.setHorizontalAlignment(SwingConstants.CENTER);
        int xpos = Math.max(x, x + 112/2 - username.length() * 8);
        name.setBounds(xpos, y + 102 - 14, 112 - (xpos - x) * 2, 18);
        name.setFont(new Font("Tahoma", Font.PLAIN, 14));
        elements.add(name);
        elements.add(pic);
        return elements;
    }

    public List<JLabel> getElements() {
        return elements;
    }

    // For testing purposes
    public String getUsername() {
        return username;
    }
}
