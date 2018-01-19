package com.cshikami;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class Worker extends Thread { //Class definition, class Worker creates a thread
    Socket sock;              //Class member, socket, local to Worker.

    Worker(Socket s)         //Constructor, assign arg s to local sock
    {
        sock = s;
    }

    public void run() {
        //Get I/O streams in/out from the socket
        PrintStream out = null;
        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintStream(sock.getOutputStream());
            //note that this branch might not output when expected:
            try {
                String name;
                String username;
                String  nextOutput;
                int ID;
                String jokeCycle = "Joke Cycle Completed";


                username = in.readLine(); //get username from JokeClient
                //name = in.readLine();

                ID = in.read(); //get ID from JokeClient
                if (ID == 0) { //if ID from JokeClient is 0
                    ID = JokeServer.incrementCount(); //increment ID on JokeServer
                    out.write(ID); //send that ID value into JokeClient
                } else {
                    out.write(ID); //otherwise, ID is already assigned and stays the same
                }

                //get jokeNumber index from JokeClient
                int jokeNumber = in.read();

                //get proverbNumber index from JokeClient
                int proverbNumber = in.read();


                if (JokeServer.serverState == JokeServer.ServerState.JOKEMODE) { //if serverState is in JOKEMODE
                    out.println(JokeServer.Jokes[jokeNumber].replaceAll("Placeholder", username)); //print out on client joke at index position jokeNumber, with username entered by user in JokeClient in place of Placeholder
                } else {                                                         //else, if serverState is in PROVERBMODE
                    out.println(JokeServer.Proverbs[proverbNumber].replaceAll("Placeholder", username)); // print out on client proverb at index position proverbNumber, with username entered by user in JokeClient in place of Placeholder
                }

                int updateJokeNumber = JokeServer.nextJoke(jokeNumber); //variable in which jokeNumber is either incremented or set back to zero

                int updateProverbNumber = JokeServer.nextProverb(proverbNumber); //variable in which proverbNumber is either incremented or set back to zero

                //randomize jokes by shuffling Jokes array in JokeServer after one run through in order
                if (jokeNumber == JokeServer.Jokes.length - 1) { //if jokeNumber equals the length of the Jokes array
                    List<String> jokesList = Arrays.asList(JokeServer.Jokes); //in order to use Collections.shuffle, must put the Jokes into a List
                    Collections.shuffle(jokesList); //shuffle the jokes in Jokes list
                    JokeServer.Jokes = jokesList.toArray(new String[jokesList.size()]); //new Jokes list that is shuffled
                }

                //randomize proverbs by shuffling Proverbs array in JokeServer after one run through in order
                if (proverbNumber == JokeServer.Proverbs.length - 1) { //if proverbNumber equals the length of the Proverbs array
                    List<String> proverbsList = Arrays.asList(JokeServer.Proverbs);  //in order to use Collections.shuffle, must put the Proverbs into a List
                    Collections.shuffle(proverbsList);  //shuffle the proverbs in Proverbs list
                    JokeServer.Proverbs = proverbsList.toArray(new String[proverbsList.size()]);  //new Proverbs list that is shuffled
                }

                //if jokeNumber is 0
//                if (updateJokeNumber == 0) {
//                    out.println(jokeCycle);
//                }
                out.write(updateJokeNumber);     //update joke index position in client
                out.write(updateProverbNumber);  //update proverb index position in client

                if (JokeServer.serverState == JokeServer.ServerState.JOKEMODE) {
                    out.write(1);
                }

                if (JokeServer.serverState == JokeServer.ServerState.PROVERBMODE) {
                    out.write(2);
                }
                //print out in console username and ID of client
                System.out.println("Received request from " + username + " with ID of " + ID + ".");


            } catch (IOException x) {
                System.out.println("Server read error"); //if for some reason there is an IOException, print out Server read error
                x.printStackTrace(); //print a stack trace of the error
            }
            sock.close(); //close this connection, but not the server

        } catch (IOException ioe) {
            System.out.println(ioe); //print the exception if there is an IOException
        }
    }

    static void printRemoteAddress(String name, PrintStream out) {
        try { //these are all printed in the client window
            out.println("Looking up " + name + "..."); //print Looking up and the name supplied from the run() method
            InetAddress machine = InetAddress.getByName(name);
            out.println("Host name: " + machine.getHostName()); //print out in client window the host name
            out.println("Host IP: " + toText(machine.getAddress())); //print out in client window the ip address formatted by the toText method
        } catch (UnknownHostException ex) {
            out.println("Failed in attempt to lookup " + name); //if host is not know/does not exist, print out Unknown host exception and the name attempted
        }
    }

    //Not interesting to us:
    static String toText(byte[] ip) {
        StringBuffer result = new StringBuffer(); //new StringBuffer object named result
        //format the ip address:
        for (int i = 0; i < ip.length; ++i) {
            if (i > 0) result.append(".");
            result.append(0xff & ip[i]);
        }
        return result.toString(); //return StringBuffer object as string
    }
}

public class JokeServer {

    public enum ServerState {
        JOKEMODE, PROVERBMODE
    }

    public static String[] Jokes = {
            "JA Placeholder: What is an oyster's favorite tea? Pearl grey!",
            "JB Placeholder: Why did the chicken cross the road? To get to the other side.",
            "JC Placeholder: Why did the cookie go to the doctor? He felt crumby.",
            "JD Placeholder: What's it called when a King and Queen have no children? A receding heir line."
    };

    public static String[] Proverbs = {
            "PA Placeholder: Two wrongs don't make a right.",
            "PB Placeholder: The pen is mightier than the sword",
            "PC Placeholder: When in Rome, do as the Romans.",
            "PD Placeholder: Fortune favors the bold."
    };

    public static ServerState serverState = ServerState.JOKEMODE; //serverState starts in JOKEMODE

    private static int nextUserID = 0;

    //synchronized method in order to increment count of nextUserID so that all threads can use it
    public static synchronized int incrementCount() {
        return ++nextUserID;
    }

    //Return next joke index position
    public static int nextJoke(int index) {
        if (index == JokeServer.Jokes.length - 1) //if jokeNumber is last joke index

            return 0; //return 0 for jokeNumber
        else
            return index += 1; //else, add 1 to jokeNumber
    }

    //Return next proverb index position
    public static int nextProverb(int index) {
        if (index == JokeServer.Proverbs.length - 1) //if proverbNumber is last proverb index
            return 0; //return 0 for proverbNumber
        else
            return index += 1; //else, add 1 to proverbNumber
    }

    public static void ChangeState(int state) {

        System.out.println("Received state command: " + state);

        if (state == 1)
            JokeServer.serverState = ServerState.JOKEMODE;
        else if (state == 2)
            JokeServer.serverState = ServerState.PROVERBMODE;

        System.out.println("    -- Server State is: " + JokeServer.serverState);
    }

    public static void main(String[] args) throws IOException {
        int q_len = 6; /* Not interesting. Number of requests for OpSys to queue */
        int port = 4545; //port number

        AdminLooper AL = new AdminLooper(); // create a DIFFERENT thread
        Thread t = new Thread(AL);
        t.start();  // ...and start it, waiting for administration input

        Socket sock;

        ServerSocket servsock = new ServerSocket(port, q_len); //new ServerSocket object with port and q_len arguments

        System.out.println("Chris Shikami's Joke Server starting up, listening at port 4545.\n"); //print out this message in server window

        while (true) {
            sock = servsock.accept(); //wait for the next client connection
            new Worker(sock).start(); //start the worker thread
        }
    }
}

    class AdminLooper implements Runnable {
        public static boolean adminControlSwitch = true;

        public void run() { // Running the Admin listen loop
            int q_len = 6; /* Number of requests for OpSys to queue */
            int port = 5050;  // We are listening at a different port for Admin clients
            Socket sock;

            try {
                ServerSocket servsock = new ServerSocket(port, q_len);
                while (adminControlSwitch) { //while true
                    // wait for the next Admin Client connection:
                    sock = servsock.accept();
                    new AdminWorker(sock).start(); //start a new thread
                }
            } catch (IOException ioe) { //catch exception
                System.out.println(ioe);
            }
        }

    }

    class AdminWorker extends Thread
    {
        private Socket sock;

        AdminWorker(Socket s)
        {
            sock = s;
        }

        public void run()
        {
            PrintStream out = null;
            BufferedReader in = null;
            try
            {
                in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                out = new PrintStream(sock.getOutputStream());
                //Note that this branch might not execute when expected
                System.out.println("Joke Admin Client connected.");

                //Send the state of the server
                out.println(JokeServer.serverState);

                //Retrieve the command number from the client
                int command = in.read();

                //Change state
                JokeServer.ChangeState(command);

                this.sock.close();
            }
            catch(IOException x)
            {
                System.out.println("Server read error");
                x.printStackTrace();
            }
        }
    }
