package dev.anshmehta;

import dev.anshmehta.exceptions.FileExistsException;
import dev.anshmehta.exceptions.RepositoryException;

import java.io.File;

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
}
