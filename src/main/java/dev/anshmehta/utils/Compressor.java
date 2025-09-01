package dev.anshmehta.utils;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public  class Compressor {


    public static byte[] zlibCompress(byte[] data) {
       final Deflater deflater = new Deflater();
       deflater.setInput(data);
       deflater.finish();
       ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
       byte[] buffer = new byte[1024];
       while (!deflater.finished()) {
           int len = deflater.deflate(buffer);
           outputStream.write(buffer, 0, len);
       }
       deflater.end();
       return outputStream.toByteArray();
    }

    public static byte[] zlibDecompress(byte[] data) throws DataFormatException {
        final Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int len = inflater.inflate(buffer);
            outputStream.write(buffer, 0, len);

        }
        inflater.end();
        return outputStream.toByteArray();


    }

}
