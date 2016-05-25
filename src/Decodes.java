import java.net.SocketException;
import java.util.Base64;

/**
 * Created by wangtao on 2016-05-25.
 */
public class Decodes {
    byte parentId;
    int typeId;
    int valueType;
    int headPackLen = 22;
    int code = 0x03;

    public Decodes(String username, String ip, long time) throws SocketException {
        byte[] data = attributeData(username, ip, time);
        byte[] pack = getPackage(data, time);
        for(int i=0;i<pack.length;i++){
            System.out.print(Integer.toHexString(pack[i]));
        }
        UDP udp=UDP.instance("115.239.134.167", 8080,pack);
        udp.send();
        System.out.println();
        System.out.println(new String(pack));
        System.out.println(MD5.encode(pack));
    }

    private byte[] getPackage(byte[] data, long time) {
        int len = headPackLen + data.length;
        byte[] pack = new byte[len];
        pack[0] = 0x53;
        pack[1] = 0x4e;
        pack[2] = 0;
        pack[3] = (byte) len;
        pack[4] = 0x03;
        pack[5] = (byte) calcTimeFlag(time);
        for (int i=headPackLen;i<len;i++){
            pack[i]=data[i-headPackLen];
        }
        byte[] salt= Base64.getDecoder().decode("TExXTFhBX1RQU0hBUkVTRUNSRVQ=");
        byte[] d=new byte[len+salt.length];
        System.arraycopy(pack, 0, d, 0, pack.length);
        System.arraycopy(pack, 0, d, pack.length, salt.length);
        String sign=MD5.encode(d);
        for(int t=0,i=6;i<headPackLen;i++,t+=2){
            pack[i]=(byte)( int2hex(sign.charAt(t),1)+int2hex(sign.charAt(t+1),0));
        }
        return pack;
    }

    public byte[] username(String username) {
        parentId = 0x01;
        typeId = 0x00;
        valueType = 0x02;
        return digest(username);
    }

    public byte[] clientIp(String ip) {
        parentId = 0x02;
        typeId = 0x00;
        valueType = 0x01;
        String[] ips = ip.split("\\.");
        byte[] b = new byte[7];
        b[0] = parentId;
        b[1] = 0;
        b[2] = 0x07;
        b[3] = (byte) Integer.parseInt(ips[0]);
        b[4] = (byte) Integer.parseInt(ips[1]);
        b[5] = (byte) Integer.parseInt(ips[2]);
        b[6] = (byte) Integer.parseInt(ips[3]);
        return b;
    }

    public byte[] clientVersion(String version) {
        parentId = 0x03;
        typeId = 0x00;
        valueType = 0x02;
        return digest(version);
    }

    public byte[] keepAliveTime(long time) {
        parentId = 0x12;
        typeId = 0x00;
        valueType = 0x00;
        byte[] b = new byte[7];
        byte[] tb = time2bytes(time);
        b[0] = parentId;
        b[1] = 0;
        b[2] = 0x07;
        b[3] = tb[0];
        b[4] = tb[1];
        b[5] = tb[2];
        b[6] = tb[3];
        return b;
    }

    public byte[] keepAliveData(long time) {
        parentId = 0x14;
        typeId = 0x00;
        valueType = 0x2;
        String salt = "qwer";
        byte[] b = new byte[8];
        byte[] tb = time2bytes(time);
        b[0] = tb[0];
        b[1] = tb[1];
        b[2] = tb[2];
        b[3] = tb[3];
        b[4] = (byte) 'q';
        b[5] = (byte) 'w';
        b[6] = (byte) 'e';
        b[7] = (byte) 'r';
        String code = MD5.encode(b);
        return digest(code);
    }

    @Deprecated
    public byte[] clientType(String type) {
        parentId = 0x04;
        typeId = 0x00;
        valueType = 0x02;
        return digest(type);
    }

    @Deprecated
    public byte[] osVersion(String version) {
        parentId = 0x05;
        typeId = 0x00;
        valueType = 0x02;
        return digest(version);
    }

    private byte[] time2bytes(long time) {
        String hexTime = Long.toHexString(time);
        byte[] b = new byte[4];
        b[0] = (byte) (int2hex(hexTime.charAt(0), 1) + int2hex(hexTime.charAt(1), 0));
        b[1] = (byte) (int2hex(hexTime.charAt(2), 1) + int2hex(hexTime.charAt(3), 0));
        b[2] = (byte) (int2hex(hexTime.charAt(4), 1) + int2hex(hexTime.charAt(5), 0));
        b[3] = (byte) (int2hex(hexTime.charAt(6), 1) + int2hex(hexTime.charAt(7), 0));
        return b;
    }

    private int int2hex(char i, int q) {
        String num = i + "";
        if (i >= 'a' && i <= 'f') {
            switch (i) {
                case 'a':
                    num = "10";
                    break;
                case 'b':
                    num = "11";
                    break;
                case 'c':
                    num = "12";
                    break;
                case 'd':
                    num = "13";
                    break;
                case 'e':
                    num = "14";
                    break;
                case 'f':
                    num = "15";
                    break;
            }
        }
        int a = Integer.parseInt(num);
        return a * (int) Math.pow(16, q);
    }

    private byte[] digest(String value) {
        byte[] b = new byte[value.length() + 3];
        b[0] = parentId;
        b[1] = 0;
        b[2] = (byte) (value.length() + 3);
        for (int i = 3; i < b.length; i++) {
            b[i] = (byte) value.charAt(i - 3);
        }
        return b;
    }

    public long calcTimeFlag(long timestamp) {
        long timeNum = (((timestamp * 0x343FD) + 0x269EC3) & 0xFFFFFFFF);
        return (timeNum >> 0x10) & 0xFF;
    }

    private byte[] attributeData(String username, String ip, long time) {
        byte[] ips = clientIp(ip);
        byte[] version = clientVersion("1.2.22.36");
        byte[] keepData = keepAliveData(time);
        byte[] keepTime = keepAliveTime(time);
        byte[] users = username(username);
        int len = ips.length + version.length + keepData.length + keepTime.length + users.length;
        byte[] data = new byte[len];
        int i = 0;
        for (; i < ips.length; i++) {
            data[i] = ips[i];
        }
        for (int t = 0; t < version.length; i++, t++) {
            data[i] = version[t];
        }
        for (int t = 0; t < keepData.length; i++, t++) {
            data[i] = keepData[t];
        }
        for (int t = 0; t < keepTime.length; i++, t++) {
            data[i] = keepTime[t];
        }
        for (int t = 0; t < users.length; i++, t++) {
            data[i] = users[t];
        }
        return data;
    }

    public static void main(String[] args) throws SocketException {
        Decodes decodes = new Decodes("18094679353@YD.XY", "60.187.4.54", 1464168816);
        System.out.println(decodes.calcTimeFlag(1464168816));
//        System.out.println(decodes.calcTimeFlag(1459864505));
//        byte[] v = decodes.keepAliveData(1464168816);
//        System.out.println(MD5.encode(v));
    }
}
