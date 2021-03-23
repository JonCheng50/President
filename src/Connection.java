import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Connections provides the means by which games communicate with each other
 */
public class Connection implements Runnable{

    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    private boolean running;
    private Game game;

    public Connection(Game game, Socket socket) {
        this.game = game;
        try {
            System.out.println("Streaming");
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("OutputStream done");
            inputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("Stream done");
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread (this).start();
        System.out.println("Connection done");
    }

    public Connection(Socket socket) {
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread (this).start();
    }

    @Override
    public void run() {
        running  = true;
        while (running) {
            System.out.println("Connection running");
            Object obj = null;
            try {
                obj = inputStream.readObject();
                game.packetReceived(obj);
            } catch (NullPointerException e) {
                Main.numReceived(obj);
            } catch (EOFException | SocketException e) {
                running = false;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendPacket(Object obj) {
        try {
            outputStream.reset();
            outputStream.writeObject(obj);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        running = false;
        inputStream.close();
        outputStream.close();
    }
}
