package example_chat_program.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class Client
{
    private Socket socket;
    private BufferedReader keyboard;
    private PrintWriter output;
    private String serverIP = "127.0.0.1";
    private String serverPort = "9696";

    public Client()
    {
    }

    public void clientConnect()
    {
        try
        {
            System.out.println("Please join the server with: JOIN <<user_name>>");
            socket = new Socket(serverIP, Integer.parseInt(serverPort));

            keyboard = new BufferedReader(new InputStreamReader(System.in));
            String clientJoinMessage = keyboard.readLine();

            output = new PrintWriter(socket.getOutputStream(), true);
            output.println(clientJoinMessage);

            ClientServerHandler clientServerHandler = new ClientServerHandler(socket);

            /**
             * Only one thread is needed in the client class because the client do not need to connect to multiple servers
             */
            new Thread(clientServerHandler).start();
            sendsHeartbeat();
            sendCommandsToServer();

        }
        catch (IOException e)
        {
            /**
             * https://stackoverflow.com/questions/12095378/difference-between-e-printstacktrace-and-system-out-printlne
             */
            e.printStackTrace(); //Uses System.err
        }
        finally
        {
            output.close();
            try
            {
                keyboard.close();
            }
            catch (IOException e)
            {
                e.printStackTrace(); //Uses System.err
                System.exit(1);
            }
        }
    }

    public void sendCommandsToServer()
    {
        while(true)
        {
            System.out.println("Use the following commands: \n\t DATA <<user_name>>: <<free text…>> \n\t LIST \n\t QUIT");
            String command = null;
            try
            {
                command = keyboard.readLine();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            switch (command.substring(0,4))
            {
                case "QUIT":
                    output.println("QUIT");
                    System.exit(1);
                    break;

                case "DATA":
                case "LIST":
                    output.println(command);
                    break;
                default:
                    System.err.println("J_ER 4: Unknown Command - No such command exists!");
                    break;
            }
        }
    }

    public void sendsHeartbeat()
    {
        /**
         * Sends a heartbeat alive every 6 seconds
         * Anonymous inner class
         * delay is how long is there to the first execution
         * period is time between task execution
         */
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                output.println("IMAV");
            }
        }, 20*100, 60*100);
    }
}

