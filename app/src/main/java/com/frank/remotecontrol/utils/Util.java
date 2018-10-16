package com.frank.remotecontrol.utils;
import java.util.Arrays;

public class Util {
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

    public static void main(String args[]){
        System.out.println(Arrays.toString(hexStringToByteArray("ff")));
    }
}
