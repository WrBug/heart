import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Created by wangtao on 2016-05-25.
 */
public class UDP {
    private DatagramSocket clientSocket = null;
    private InetSocketAddress serverAddress = null;
    private byte[] data;

    private UDP(String host, int port, byte[] data) throws SocketException {
        clientSocket = new DatagramSocket();
        serverAddress = new InetSocketAddress(host, port);
        this.data = data;
    }

    public static UDP instance(String host, int port, byte[] data) throws SocketException {
        return new UDP(host, port, data);
    }

    public void send() {
        DatagramPacket packet = null;
        try {
            packet = new DatagramPacket(data, data.length,
                    serverAddress);
            clientSocket.send(packet);
            clientSocket.close();
        } catch (SocketException e) {
        } catch (IOException e) {
        }

    }
}