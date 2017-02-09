package server;

import server.database.UserDatabase;
import server.database.UserDatabaseImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Entry point of the server.
 * It manages the initialization.
 */
public class Core {

    public static UserDatabase database;

    public static void main(String args[]){

        database = new UserDatabaseImpl();

        //TODO Initialize creationRequest queue

        //TODO Initialize creationRequestManager

        //TODO Initialize followRequest queue

        //TODO Initialize followRequestManager

        System.out.println("Everything is initialized. Press q to exit");

        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        String s = null;

        while(s==null || s.compareTo("q") != 0) {
            try {
                s = bufferRead.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                s = null;
            }
        }

        //TODO close everithing
    }
}
