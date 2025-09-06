package dev.anshmehta;

import java.util.zip.DataFormatException;

public class Main {
    public static void main(String[] args) throws DataFormatException {
        Repository repository = new Repository();
        repository.initializeRepository();
        repository.addToIndex(".");
        repository.status();
        repository.commit("Initial commit");
        System.out.println("\nAfter commit:");
        repository.status();

    }
}