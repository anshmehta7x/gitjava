package dev.anshmehta;

import dev.anshmehta.exceptions.FileExistsException;
import dev.anshmehta.exceptions.RepositoryException;
import dev.anshmehta.utils.Compressor;
import dev.anshmehta.utils.FileManager;
import dev.anshmehta.utils.Hashing;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.zip.DataFormatException;

public class Repository {
    final private String PATH_NAME = ".gitjava";
    final private FileManager mainDirFileManager;
    private boolean isInitialized = false;
    private FileManager repoDirFileManager;
    private FileManager objDirFileManager;

    public Repository() {
        this.mainDirFileManager = new FileManager();
        this.checkIfInitialized();
    }

    private void checkIfInitialized() {
        File repoDir = new File(this.PATH_NAME);
        if(repoDir.exists() && repoDir.isDirectory()) {
            isInitialized = true;
            repoDirFileManager = new FileManager(PATH_NAME);
            objDirFileManager = new FileManager(PATH_NAME + File.separator + "objects");
        }
    }

    public void initializeRepository() {
        if(isInitialized){
            System.out.println("Repository already initialized");
            return;
        }
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

    private String writeHashedObject(String data,String type){
        byte[] byteData = Hashing.hashObject(data,type);
        String sha1Hash = Hashing.calculateSHA1(byteData);
        assert sha1Hash != null;
        String pointerChars = sha1Hash.substring(0,2);
        objDirFileManager.createDir(pointerChars);
        objDirFileManager.createFile(pointerChars + '/' + sha1Hash.substring(2));
        objDirFileManager.writeFile(pointerChars + '/' + sha1Hash.substring(2), Compressor.zlibCompress(byteData));
        return sha1Hash;
    }

    private byte[] findObject(String hashedObject){

        try {
            String objectPath = hashedObject.substring(0, 2) +  File.separator + hashedObject.substring(2);

            return objDirFileManager.readFile(objectPath);
        }catch (Exception e){
            throw new RepositoryException("Error finding object");
        }
    }

    private void readObject(byte[] objectData) throws DataFormatException {
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

    private void writeIndex(ArrayList<IndexEntry> indexEntries) {
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

    private ArrayList<IndexEntry> readIndex() {
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

    private boolean isIgnored(String filepath, HashSet<String> ignorePatterns) {
        for (String pattern : ignorePatterns) {
            pattern = pattern.trim();
            if (pattern.isEmpty() || pattern.startsWith("#")) continue;
            if (filepath.equals(pattern)) return true;
            if (pattern.startsWith("*") && filepath.endsWith(pattern.substring(1))) return true;
            if (pattern.endsWith("/") && filepath.startsWith(pattern)) return true;
            if (filepath.contains(pattern)) return true;
        }
        return false;
    }

    public void addToIndex(String filepath) throws RepositoryException {
        try {

            File f = new File(filepath);
            if (!f.exists()) {
                throw new RepositoryException("Path does not exist: " + filepath);
            }
            HashSet<String> toIgnore = new HashSet<>();
            File ignoreFile = new File(".gitignore");
            if(ignoreFile.exists()){
                toIgnore.addAll(mainDirFileManager.readAllLines(".gitignore"));
            }
//            System.out.println(toIgnore.toString());

            if (f.isFile()) {
                if (isIgnored(filepath, toIgnore)) {
                    System.out.println("ignoring " + filepath);
                    return;
                }
                addFileToIndex(filepath,toIgnore);
            } else if (f.isDirectory()) {
                if (isIgnored(filepath, toIgnore)) {
                    System.out.println("ignoring " + filepath);
                    return;
                }
                addDirectoryToIndex(f, filepath, toIgnore);
            } else {
                throw new RepositoryException("Path is neither a file nor directory: " + filepath);
            }
        } catch (Exception e) {
            throw new RepositoryException("Error adding to index: " + e.getMessage());
        }
    }

    private void addFileToIndex(String filepath, HashSet<String> toIgnore) throws RepositoryException {
        try {
            if (isIgnored(filepath, toIgnore)) {
                System.out.println("ignoring " + filepath);
                return;
            }
            File f = new File(filepath);
            if (filepath.contains(PATH_NAME)) {
                return;
            }
            byte[] fileContent = mainDirFileManager.readFile(filepath);
            String fileContentStr = new String(fileContent, java.nio.charset.StandardCharsets.UTF_8);
            String sha1Hash = writeHashedObject(fileContentStr, "blob");
            IndexEntry entry = new IndexEntry(filepath, sha1Hash, fileContent.length);

            ArrayList<IndexEntry> indexEntries = readIndex();

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
            throw new RepositoryException("Error adding file to index: " + e.getMessage());
        }
    }

    private void addDirectoryToIndex(File directory, String basePath, HashSet<String> toIgnore) throws RepositoryException {
        if (isIgnored(basePath, toIgnore)) {
            System.out.println("ignoring " + basePath);
            return;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            String currentPath = basePath.isEmpty() ? file.getName() : basePath + File.separator + file.getName();

            if (currentPath.contains(PATH_NAME)) {
                continue;
            }

            if (file.isFile()) {
                addFileToIndex(currentPath, toIgnore);
            } else if (file.isDirectory()) {
                addDirectoryToIndex(file, currentPath, toIgnore);
            }
        }
    }

    public void status(){
        ArrayList<IndexEntry> indexEntries = readIndex();
        System.out.println("Tracked files:");
        for(IndexEntry entry : indexEntries){
            System.out.println(entry.getFilePath() + " " + entry.getSha1() + " " + entry.getFileSize());
        }
    }

    public void commit(String message){
        ArrayList<IndexEntry> indexEntries = readIndex();
        if (indexEntries.isEmpty()) {
            System.out.println("Nothing to commit, working tree clean");
            return;
        }

        StringBuilder treeObjectContent = new StringBuilder();
        for (IndexEntry entry : indexEntries) {
            treeObjectContent.append("blob ").append(entry.getSha1()).append(" ").append(entry.getFilePath()).append("\n");
        }
        String treeSha1 = writeHashedObject(treeObjectContent.toString(), "tree");

        String parentCommit = null;
        try {
            List<String> headLines = repoDirFileManager.readAllLines("HEAD");
            if (!headLines.isEmpty()) {
                parentCommit = headLines.getLast();
            }
        } catch (Exception e) {
            throw new RepositoryException("Error reading HEAD: " + e.getMessage());
        }

        long timestamp = System.currentTimeMillis() / 1000;
        String author = "Ansh Mehta <ansh@gmail.com>";
        StringBuilder commitObjectContent = new StringBuilder();
        commitObjectContent.append("tree ").append(treeSha1).append("\n");
        if (parentCommit != null) {
            commitObjectContent.append("parent ").append(parentCommit).append("\n");
        }
        commitObjectContent.append("author ").append(author).append(" ").append(timestamp).append(" +0530\n");
        commitObjectContent.append("committer ").append(author).append(" ").append(timestamp).append(" +0530\n");
        commitObjectContent.append("\n");
        commitObjectContent.append(message).append("\n");

        String commitSha1 = writeHashedObject(commitObjectContent.toString(), "commit");

        try {
            ArrayList<String> headLines = repoDirFileManager.readAllLines("HEAD");
            headLines.add(commitSha1);
            StringBuilder sb = new StringBuilder();
            for (String line : headLines) {
                sb.append(line).append("\n");
            }
            repoDirFileManager.writeFile("HEAD", sb.toString().getBytes());
        } catch (Exception e) {
            throw new RepositoryException("Error writing to HEAD: " + e.getMessage());
        }
        System.out.println("Committed " + commitSha1);
    }


    public HashSet<String> getAllFiles(){
        HashSet<String> allFiles = new HashSet<>();
        File currentDir = new File(mainDirFileManager.getCurrentDir());
        getAllFilesHelper(currentDir, "", allFiles);
        return allFiles;
    }

    private void getAllFilesHelper(File dir, String basePath, HashSet<String> allFiles){
        File[] files = dir.listFiles();
        if(files == null) return;
        for(File f : files){
            String currentPath = basePath.isEmpty() ? f.getName() : basePath + File.separator + f.getName();
            if(f.isFile()){
                allFiles.add(currentPath);
            }
            else if(f.isDirectory()){
                getAllFilesHelper(f, currentPath, allFiles);
            }
        }
    }
}