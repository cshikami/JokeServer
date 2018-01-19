/*--------------------------------------------------------

1. Christopher Shikami  1/18/2018

2. java version "9"
Java(TM) SE Runtime Environment (build 9+181)
Java HotSpot(TM) 64-Bit Server VM (build 9+181, mixed mode)

3. Precise command-line compilation examples / instructions:
> javac JokeClient.java

4. Precise examples / instructions to run this program:

e.g.:

In separate shell windows:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

All acceptable commands are displayed on the various consoles.

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For exmaple, if the server is running at
140.192.1.22 then you would type:

> java JokeClient 140.192.1.22
> java JokeClientAdmin 140.192.1.22

5. List of files needed for running the program.

 a. checklist.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java

5. Notes:

In running the program, there is a slight bug in which the jokeNumber or proverbNumber leaves off where
the other ended.

For example, if we are on jokeNumber of index 2 on one client and we open another client connection and
switch modes to PROVERBMODE, we will get back a proverb of index 3 instead of 1.

The cycle will complete, however, and the randomization works as it should otherwise.

----------------------------------------------------------*/

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

        username = input.nextLine(); //get username from client

        System.out.println("\n" + username + "'s Joke Client, 1.9\n"); //print out in client window
        System.out.println("Server: " + serverName + ", Port: 4545\n"); //print out in client window, serverName being either localhost or the string provided as argument by user


        BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); //create object to read system input into BufferedReader

        try {
            String name;

            do {
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

                //get jokeNumber and proverbNumber from JokeServer
                jokeNumber = fromServer.read();
                proverbNumber = fromServer.read();

                //get jokeServerMode from JokeServer (either 1 or 2, 1 for JOKEMODE and 2 for PROVERBMODE)
                jokeServerMode = fromServer.read();

                //if jokeNumber is 0 (back to start of cycle) and JokeServer is in JOKEMODE
                if (jokeNumber == 0 && jokeServerMode == 1) {
                    System.out.println("JOKE CYCLE COMPLETED"); //print out this message to client console
                }

                //if proverbNumber is 0 (back to start of cycle) and JokeServer is in PROVERBMODE
                if (proverbNumber == 0 && jokeServerMode == 2) {
                    System.out.println("PROVERB CYCLE COMPLETED"); //print out this message to client console
                }

            sock.close(); //close socket connection
        }
        catch (IOException x) { //print exception stack trace if there is an IOException
            System.out.println("Socket error.");  //print Socket error. above stack trace
            x.printStackTrace(); //print exception stack trace
        }

    }
}