public class Heart {
    byte id;
    int headPackLen = 22;
    byte[] data;

    public static Heart instance(String username, String ip, long time) {
        Heart heart = new Heart();
        byte[] data = heart.splitData(username, ip, time);
        heart.data = heart.getPackage(data, time);
        return heart;
    }

    public byte[] getData() {
        return data;
    }

    private byte[] getPackage(byte[] data, long time) {
        int len = headPackLen + data.length;
        byte[] pack = new byte[len];
        pack[0] = 0x53;
        pack[1] = 0x4e;
        pack[2] = 0;
        pack[3] = (byte) len;
        pack[4] = 0x03;
        pack[5] = (byte) calcTime(time);
        System.arraycopy(data, 0, pack, headPackLen, data.length);
        byte[] d = new byte[len + Constant.PRE_PACK_CODE.length];
        System.arraycopy(pack, 0, d, 0, pack.length);
        System.arraycopy(Constant.PRE_PACK_CODE, 0, d, pack.length, Constant.PRE_PACK_CODE.length);
        String sign = MD5.encode(d);
        for (int t = 0, i = 6; i < headPackLen; i++, t += 2) {
            pack[i] = (byte) (char2hex(sign.charAt(t), 1) + char2hex(sign.charAt(t + 1), 0));
        }
        return pack;
    }

    private byte[] username(String username) {
        id = 0x01;
        return encode(username);
    }

    private byte[] ip(String ip) {
        id = 0x02;
        String[] ips = ip.split("\\.");
        byte[] b = new byte[7];
        b[0] = id;
        b[1] = 0;
        b[2] = 0x07;
        b[3] = (byte) Integer.parseInt(ips[0]);
        b[4] = (byte) Integer.parseInt(ips[1]);
        b[5] = (byte) Integer.parseInt(ips[2]);
        b[6] = (byte) Integer.parseInt(ips[3]);
        return b;
    }

    private byte[] version() {
        id = 0x03;
        return encode(Constant.VERSION);
    }

    private byte[] time(long time) {
        id = 0x12;
        byte[] b = new byte[7];
        byte[] tb = time2bytes(time);
        b[0] = id;
        b[1] = 0;
        b[2] = 0x07;
        System.arraycopy(tb, 0, b, 3, tb.length);
        return b;
    }

    private byte[] aliveData(long time) {
        id = 0x14;
        byte[] b = new byte[8];
        byte[] tb = time2bytes(time);
        System.arraycopy(tb, 0, b, 0, tb.length);
        System.arraycopy(Constant.ALIVE_DATA_CODE, 0, b, tb.length, Constant.ALIVE_DATA_CODE.length);
        String code = MD5.encode(b);
        return encode(code);
    }

    private byte[] time2bytes(long time) {
        String hexTime = Long.toHexString(time);
        byte[] b = new byte[4];
        b[0] = (byte) (char2hex(hexTime.charAt(0), 1) + char2hex(hexTime.charAt(1), 0));
        b[1] = (byte) (char2hex(hexTime.charAt(2), 1) + char2hex(hexTime.charAt(3), 0));
        b[2] = (byte) (char2hex(hexTime.charAt(4), 1) + char2hex(hexTime.charAt(5), 0));
        b[3] = (byte) (char2hex(hexTime.charAt(6), 1) + char2hex(hexTime.charAt(7), 0));
        return b;
    }

    private int char2hex(char i, int q) {
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

    private byte[] encode(String value) {
        byte[] b = new byte[value.length() + 3];
        b[0] = id;
        b[1] = 0;
        b[2] = (byte) (value.length() + 3);
        for (int i = 3; i < b.length; i++) {
            b[i] = (byte) value.charAt(i - 3);
        }
        return b;
    }

    private long calcTime(long timestamp) {
        long timeNum = (((timestamp * 0x343FD) + 0x269EC3) & 0xFFFFFFFF);
        return (timeNum >> 0x10) & 0xFF;
    }

    private byte[] splitData(String username, String ip, long time) {
        byte[] ips = ip(ip);
        byte[] version = version();
        byte[] keepData = aliveData(time);
        byte[] keepTime = time(time);
        byte[] users = username(username);
        int len = ips.length + version.length + keepData.length + keepTime.length + users.length;
        byte[] data = new byte[len];
        int i = 0;
        System.arraycopy(ips, 0, data, 0, ips.length);
        i += ips.length;
        System.arraycopy(version, 0, data, i, version.length);
        i += version.length;
        System.arraycopy(keepData, 0, data, i, keepData.length);
        i += keepData.length;
        System.arraycopy(keepTime, 0, data, i, keepTime.length);
        i += keepTime.length;
        System.arraycopy(users, 0, data, i, users.length);
        return data;
    }
}
