package dev.anshmehta.utils;

import dev.anshmehta.exceptions.DirectoryExistsException;
import dev.anshmehta.exceptions.FileExistsException;

import java.io.*;


public class FileManager {
    final private String currentDirAddress;
    final private File currentDirFileObj;

    public FileManager() {
        this.currentDirAddress = System.getProperty("user.dir");
        this.currentDirFileObj = new File(this.currentDirAddress);
    }

    public FileManager(String dirName) {
        this.currentDirAddress = System.getProperty("user.dir") + File.separator + dirName;
        this.currentDirFileObj = new File(this.currentDirAddress);
    }

    public String getCurrentDir() {
        return currentDirAddress;
    }

    public boolean createDir(String dir){
        try{
            File[] dirMembers = currentDirFileObj.listFiles();
            if(dirMembers!=null){
                for(File f: dirMembers){
                    if(f.isDirectory() && f.getName().equals(dir)){
                        throw new DirectoryExistsException("Directory already exists!");
                    }
                }
            }
            File newDir = new File(this.currentDirAddress + File.separator + dir);
            if(newDir.mkdir()){
                System.out.println("Directory created!");
                return true;
            }
            else{
                throw new DirectoryExistsException("Unable to create directory!");
            }
        } catch (DirectoryExistsException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    public void createFile(String fileName){
        try{
            File[] dirMembers = currentDirFileObj.listFiles();
            if(dirMembers!=null){
                for(File f: dirMembers){
                    if(f.isFile() && f.getName().equals(fileName)){
                        throw new FileExistsException("File already exists!");
                    }
                }
            }
            String newFileName = this.currentDirAddress + File.separator + fileName;
            File newFile = new File(newFileName);
            if(newFile.createNewFile()){
                System.out.println("File created :  " + newFileName);
            }
            else{
                throw new FileExistsException("Unable to create file!");
            }


        }catch (FileExistsException e){
            System.err.println(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] readFile(String fileName){

        try{
            String filePath = this.currentDirAddress + File.separator + fileName;
            File f = new  File(filePath);
            FileInputStream fis = new FileInputStream(f);

            byte[] fileContent = new byte[(int) f.length()];
            int  read = fis.read(fileContent);
            fis.close();
            if(read!=fileContent.length){
                throw new IOException("Cound not read entire file");
            }

            return fileContent;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeFile(String fileName, byte[] fileContent){
        try{
            String filePath = this.currentDirAddress + File.separator + fileName;
            File f = new  File(filePath);
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(fileContent);
            fos.flush();
            fos.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
