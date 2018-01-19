package com.cshikami;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class JokeClient {

    private static int ID = 0;
    private static int jokeNumber = 0;
    private static int proverbNumber = 0;
    private static int port = 4545;
    private static int jokeServerMode;

    public static String username;

    public static void main(String[] args) {
        String serverName;
        if (args.length < 1) { //if there are no arguments added when running the class, then serverName is localhost
            serverName = "localhost";
        } else {
            serverName = args[0]; //otherwise, serverName is assigned the first argument provided
        }

        Scanner input = new Scanner(System.in);
        String username;
        System.out.print("What is your name?: ");

        username = input.nextLine(); //get username

        System.out.println("\n" + username + "'s Joke Client, 1.8\n"); //print out in client window
        System.out.println("Server: " + serverName + ", Port: 4545\n"); //print out in client window, serverName being either localhost or the string provided as argument by user


        BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); //create object to read system input into BufferedReader

        try {
            //String username;
            String name;

            do {

                //username = in.readLine();
                System.out.print("Press enter to get something or q to quit : "); //print out to client window
                System.out.flush(); //have to flush the output because we don't want a newline after previous printed print statement

                name = in.readLine();

                if (name.indexOf("q") < 0) {  //if the user does not write quit in system input..
                    getRemoteAddress(username, serverName); //call getRemoteAddress method with name and serverName arguments
                }
            } while (name.indexOf("q") < 0);  //if user input in client window is quit...
            System.out.println("Cancelled by user request."); //print out Cancelled by user request
        }
        catch (IOException x) { //if there is an exception
            x.printStackTrace(); //print stacktrace
        }
    }

    static void getRemoteAddress (String username, String serverName) {
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;
        String textFromServer;

        try {
            //new Socket object with serverName and port number arguments
            sock = new Socket(serverName, port);

            //Create filter I/O streams for the socket:
            fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream())); //get input stream from server
            toServer = new PrintStream(sock.getOutputStream()); //send output stream to server

                //send username to JokeServer
                toServer.println(username);

                //send ID to JokeServer

                toServer.write(ID);

                ID = fromServer.read();

                //send current index of joke to JokeServer
                toServer.write(jokeNumber);

                //send current index of proverb to JokeServer
                toServer.write(proverbNumber);

                //print to console joke or proverb from JokeServer
                System.out.println(fromServer.readLine());

                jokeNumber = fromServer.read();
                proverbNumber = fromServer.read();

                jokeServerMode = fromServer.read();

                if (jokeNumber == 0 && jokeServerMode == 1) {

                    System.out.println("JOKE CYCLE COMPLETED");
                }

                if (proverbNumber == 0 && jokeServerMode == 2) {
                    System.out.println("PROVERB CYCLE COMPLETED");
                }

            sock.close(); //close socket connection
        }
        catch (IOException x) { //print exception stack trace if there is an IOException
            System.out.println("Socket error.");  //print Socket error. above stack trace
            x.printStackTrace(); //print exception stack trace
        }

    }
}