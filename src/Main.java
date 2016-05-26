import java.net.SocketException;


public class Main {
    public static void main(String[] args) throws SocketException {
        byte[] data = Heart.instance("15355498807@DZKD.XY", "60.187.4.54", 1464168916).getData();
        System.out.println(new String(data));
        UDP.send("115.239.134.167", 8080, data);
    }
}
