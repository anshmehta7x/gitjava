package dev.anshmehta;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;

public class Compressor {
    private static Deflater deflater = new Deflater();

    public Compressor() {
        deflater.setLevel(Deflater.BEST_SPEED);
    }

    public static byte[] zlibCompress(byte[] data) {
       deflater.setInput(data);
       deflater.finish();
       ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
       byte[] buffer = new byte[1024];
       while (!deflater.finished()) {
           int len = deflater.deflate(buffer);
           outputStream.write(buffer, 0, len);
       }
       return outputStream.toByteArray();
    }
}
