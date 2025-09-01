package dev.anshmehta;

import dev.anshmehta.exceptions.FileExistsException;
import dev.anshmehta.exceptions.RepositoryException;
import dev.anshmehta.utils.Compressor;
import dev.anshmehta.utils.FileManager;
import dev.anshmehta.utils.Hashing;

import java.io.File;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

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
        objDirFileManager.writeFile(pointerChars + '/' + sha1Hash.substring(2), Compressor.zlibCompress(byteData));
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

    public void readObject(byte[] objectData) throws DataFormatException {
        byte[] decompressedData = Compressor.zlibDecompress(objectData);

        try {
            int nullIndex = -1;
            for (int i = 0; i < decompressedData.length; i++) {
                if (decompressedData[i] == 0) {
                    nullIndex = i;
                    break;
                }
            }
            if (nullIndex == -1) {
                throw new RepositoryException("Invalid object format");
            }

            String header = new String(decompressedData, 0, nullIndex, java.nio.charset.StandardCharsets.UTF_8);
            String[] parts = header.split(" ");
            if (parts.length != 2) {
                throw new RepositoryException("Invalid object header format");
            }

            String type = parts[0];
            int size = Integer.parseInt(parts[1]);
            String content = new String(decompressedData, nullIndex + 1, decompressedData.length - (nullIndex + 1),
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

    public void writeIndex(ArrayList<IndexEntry> indexEntries) {
        try {
            StringBuilder sb = new StringBuilder();
            for (IndexEntry entry : indexEntries) {
                sb.append(entry.toString()).append("\n");
            }
            repoDirFileManager.writeFile("index", sb.toString().getBytes());
        } catch (Exception e) {
            throw new RepositoryException("Error writing index: " + e.getMessage());
        }
    }

    public ArrayList<IndexEntry> readIndex() {
        try {
            ArrayList<IndexEntry> indexEntries = new ArrayList<>();
            File indexFile = new File(PATH_NAME + File.separator + "index");
            if (!indexFile.exists()) {
                throw new RepositoryException("Index file does not exist");
            }

            byte[] indexContent = repoDirFileManager.readFile("index");
            if (indexContent.length > 0) {
                String[] lines = new String(indexContent, java.nio.charset.StandardCharsets.UTF_8).split("\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        indexEntries.add(IndexEntry.fromString(line));
                    }
                }
            }

            return indexEntries;

        }
        catch (Exception e) {
            throw new RepositoryException("Error reading index: " + e.getMessage());
        }
    }


    public void addToIndex(String filepath) throws RepositoryException {
        try {
            File f = new File(filepath);
            if (!f.exists() || !f.isFile()) {
                throw new RepositoryException("File does not exist or is not a file");
            }

            // get file contents -> calculate hash -> create entry
            byte[] fileContent = mainDirFileManager.readFile(filepath);
            String fileContentStr = new String(fileContent, java.nio.charset.StandardCharsets.UTF_8);
            String sha1Hash = writeHashedObject(fileContentStr,"blob");
            IndexEntry entry = new IndexEntry(filepath, sha1Hash, fileContent.length);

            ArrayList<IndexEntry> indexEntries = readIndex();

            // check if file already exists in index → update, else add
            boolean updated = false;
            for (int i = 0; i < indexEntries.size(); i++) {
                if (indexEntries.get(i).getFilePath().equals(filepath)) {
                    indexEntries.set(i, entry);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                indexEntries.add(entry);
            }
            writeIndex(indexEntries);

        } catch (Exception e) {
            throw new RepositoryException("Error adding to index: " + e.getMessage());
        }
    }

}
