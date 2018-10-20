package com.frank.remotecontrol.utils;
import java.util.Arrays;

public class Util {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static byte[] hexStringToByteArray(String s) {
        if(s.length()%2==1){
            s="0"+s;
        }
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void main(String args[]){
        System.out.println(Arrays.toString(hexStringToByteArray("01")));
        System.out.println(bytesToHex(new byte[]{18}));

    }
}
