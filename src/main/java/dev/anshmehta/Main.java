package dev.anshmehta;

public class Main {
    public static void main(String[] args) {
        Repository repository = new Repository();
        repository.initializeRepository();
        String hash = repository.writeHashedObject("hello","blob");
        repository.readObject(repository.findObject(hash));
    }
}