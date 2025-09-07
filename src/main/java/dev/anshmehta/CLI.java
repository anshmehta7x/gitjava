package dev.anshmehta;

import java.util.zip.DataFormatException;

public class CLI {
    private Repository repository;

    public CLI() {
        this.repository = new Repository();
    }

    public static void main(String[] args) throws DataFormatException {
        CLI cli = new CLI();
        if (args.length == 0) {
            // show help
            cli.showHelp();
        }
        else{
            try{
                cli.runCommand(args);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private void runCommand(String[] args) throws DataFormatException {
        String command = args[0];
        switch (command) {
            case "help":
                showHelp();
                break;
            case "init":
                repository.initializeRepository();
                break;
            case "add":
                handleAdd(args);

        }
    }

    private void handleAdd(String[] args){
        if(args.length < 2){
            System.err.println("Error: No file specified");
            return;
        }
        for(int i = 1; i < args.length; i++){
            try{
                repository.addToIndex(args[i]);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }


    private void showHelp(){
        System.out.println("GITJAVA HELP");
    }
}