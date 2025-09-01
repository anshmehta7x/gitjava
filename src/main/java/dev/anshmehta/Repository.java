package dev.anshmehta;

import dev.anshmehta.exceptions.FileExistsException;
import dev.anshmehta.exceptions.RepositoryException;
import dev.anshmehta.Hashing;
import java.io.File;
import java.util.ArrayList;


public class Repository {
    private String PATH_NAME = ".gitjava";
    private FileManager mainDirFileManager;
    private boolean isInitialized = false;
    private FileManager repoDirFileManager;
    private FileManager objDirFileManager;

    public Repository() {
        this.mainDirFileManager = new FileManager();
    }

    public void initializeRepository() {
        try{
            isInitialized = mainDirFileManager.createDir(PATH_NAME);
            if(isInitialized) {
                repoDirFileManager = new FileManager(PATH_NAME);
                repoDirFileManager.createFile("index");
                repoDirFileManager.createFile("HEAD");
                repoDirFileManager.createDir("objects");
                objDirFileManager = new FileManager(PATH_NAME + File.separator +  "objects");
            }
            else{
                throw new RepositoryException("Error initializing repository");
            }
        }catch (FileExistsException | RepositoryException e){
            System.err.println(e.getMessage());
        }

    }

    public String writeHashedObject(String data,String type){
        byte[] byteData = Hashing.hashObject(data,type);
        String sha1Hash = Hashing.calculateSHA1(byteData);
        assert sha1Hash != null;
        String pointerChars = sha1Hash.substring(0,2);
        objDirFileManager.createDir(pointerChars);
        objDirFileManager.createFile(pointerChars + '/' + sha1Hash.substring(2));
        objDirFileManager.writeFile(pointerChars + '/' + sha1Hash.substring(2), byteData);
        return sha1Hash;
    }

    public byte[] findObject(String hashedObject){

        try {
            String objectPath = hashedObject.substring(0, 2) +  "/" + hashedObject.substring(2);

            return objDirFileManager.readFile(objectPath);
        }catch (Exception e){
            throw new RepositoryException("Error finding object");
        }
    }

    public void readObject(byte[] objectData) {
        try {
            int nullIndex = -1;
            for (int i = 0; i < objectData.length; i++) {
                if (objectData[i] == 0) {
                    nullIndex = i;
                    break;
                }
            }
            if (nullIndex == -1) {
                throw new RepositoryException("Invalid object format");
            }

            String header = new String(objectData, 0, nullIndex, java.nio.charset.StandardCharsets.UTF_8);
            String[] parts = header.split(" ");
            if (parts.length != 2) {
                throw new RepositoryException("Invalid object header format");
            }

            String type = parts[0];
            int size = Integer.parseInt(parts[1]);
            String content = new String(objectData, nullIndex + 1, objectData.length - (nullIndex + 1),
                    java.nio.charset.StandardCharsets.UTF_8);

            if (content.length() != size) {
                throw new RepositoryException("Object size mismatch");
            }

            System.out.println("Type: " + type);
            System.out.println("Size: " + size);
            System.out.println("Content: " + content);
        } catch (Exception e) {
            throw new RepositoryException("Error reading object: " + e.getMessage());
        }
    }



}
