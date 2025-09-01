package dev.anshmehta;

import java.util.ArrayList;
import java.util.zip.DataFormatException;

public class Main {
    public static void main(String[] args) throws DataFormatException {
        Repository repository = new Repository();
        repository.initializeRepository();
        repository.addToIndex("README.md");
        repository.addToIndex("pom.xml");
        ArrayList<IndexEntry> entries = repository.readIndex();
        for(IndexEntry entry : entries) {
            byte[] objectData = repository.findObject(entry.getSha1());
            repository.readObject(objectData);
        }
    }
}