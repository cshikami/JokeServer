import java.io.*;
import java.net.*;

public class JokeClientAdmin
{
    private final static int port = 5050;

    public static void main(String[] args)
    {
        String serverName;

        if(args.length < 1) //if there are no arguments added when running the class, then serverName is localhost
            serverName = "localhost";
        else {
            serverName = args[0]; //otherwise, serverName is assigned the first argument provided
        }

        System.out.println("Joke Admin Client");
        System.out.println("Using server: " + serverName + " Port: " + port); //print out serverName/address and port number in console

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); //create object to read system input into BufferedReader

        try
        {
            String name;

            do
            {
                System.out.print("\n" + "Do you want to know the state of server? : ");
                name = in.readLine(); //read user input
                if (name.indexOf("n") < 0) //if user input does not begin with n, call makeConnection method
                makeConnection(serverName); //with serverName provided
            } while(name.indexOf("n") < 0); //if user input starts with n (such as in no)...
            System.out.println("Cancelled by user request"); //print this out and exit
        }
        catch(IOException x)
        {
            x.printStackTrace();
        }
    }

    static void makeConnection(String serverName)
    {
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;
        String textFromServer;

        try
        {
            sock = new Socket(serverName, port); //establish new socket connection at serverName provided and port
            fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream())); //get input stream from server
            toServer = new PrintStream(sock.getOutputStream()); //send output stream to server

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); //create object to read input into BufferedReader

            String state = fromServer.readLine();
            System.out.println("\n" + "Server mode: " + state + "\n"); //Print current mode retrieved from server to console
            System.out.println("Press 1 for Joke Mode and 2 for Proverb Mode: "); //Print directions for the user

            int chosenMode = in.read(); //Get user input from console.
            //For some reason server state is 50, you can see this in JokeServer when chosenMode is commented out
            //Need to subtract 48 from chosenMode to get the right mode from the server
            chosenMode -= 48;

            //Send chosenMode from console to JokeServer.
            toServer.write(chosenMode);
        }
        catch(IOException x) //print exception stack trace if there is an IOException
        {
            System.out.println("Socket error");  //print Socket error. above stack trace
            x.printStackTrace(); //print exception stack trace
        }
    }
}