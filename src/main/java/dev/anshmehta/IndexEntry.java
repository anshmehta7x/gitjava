package dev.anshmehta;

public class IndexEntry {
    private String filePath;
    private String sha1;
    private int fileSize;

    public IndexEntry(String filePath, String sha1, int fileSize) {
        this.filePath = filePath;
        this.sha1 = sha1;
        this.fileSize = fileSize;
    }

    public String getFilePath() { return filePath; }
    public String getSha1() { return sha1; }
    public int getFileSize() { return fileSize; }

    @Override
    public String toString() {
        return filePath + " " + sha1 + " " + fileSize;
    }

    public static IndexEntry fromString(String line) {
        String[] parts = line.split(" ");
        return new IndexEntry(parts[0], parts[1], Integer.parseInt(parts[2]));
    }
}
