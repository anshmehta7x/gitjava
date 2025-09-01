package dev.anshmehta.utils;

import java.security.MessageDigest;

public class Hashing {
    public static String calculateSHA1(byte[] inputBytes) {
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(inputBytes);

            StringBuilder sb = new StringBuilder();
            for(byte b : digest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);

            }
            return sb.toString();
        }
        catch (Exception e){
            System.err.println("SHA-1 algorithm not found: " + e.getMessage());
        }
        return null;
    }

    public static byte[] hashObject(String data, String type){

        String header = String.format("%s %d",type,data.length());

        byte[] headerBytes = header.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] dataBytes = data.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        int totalLength = headerBytes.length + dataBytes.length + 1;
        byte[] fullData = new byte[totalLength];
        for(int i = 0; i < headerBytes.length; i++){
            fullData[i] = headerBytes[i];
        }
        fullData[headerBytes.length] = 0; //NULL byte betweebnbn data and header
        for(int i = 0; i < dataBytes.length; i++){
            fullData[i+ headerBytes.length + 1] = dataBytes[i];
        }
        return fullData;
    }

}
