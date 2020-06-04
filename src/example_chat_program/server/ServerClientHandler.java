package example_chat_program.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerClientHandler implements Runnable
{
    private Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private ArrayList<ServerClientHandler> allOnlineClients;

    private String user;
    private LocalTime IMAV = LocalTime.now();
    private Logger logger = SharedLog.getInstance();

    public ServerClientHandler()
    {
    }

    public String getUser()
    {
        return user;
    }

    public ServerClientHandler(Socket clientSocket, ArrayList<ServerClientHandler> allOnlineClients, String user)
    {
        this.clientSocket = clientSocket;
        this.allOnlineClients = allOnlineClients;
        this.user = user;

        try
        {
            input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            output = new PrintWriter(this.clientSocket.getOutputStream(), true);
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, e.getStackTrace().toString());
        }

        if (!allOnlineClients.isEmpty())
        {
            String result = "LIST" + getUserList() + " " + getUser();
            outToAll(result);
            output.println(result);
            logger.log(Level.INFO, this.clientSocket.getRemoteSocketAddress().toString() + " " + result);
        }
    }

    @Override
    public void run()
    {
        try
        {
            boolean run = true;
            while(run)
            {
                String request;
                try
                {
                    request = input.readLine();
                    logger.log(Level.INFO, clientSocket.getRemoteSocketAddress().toString() + " " +  request);
                }
                catch (Exception e)
                {
                    outToAll(getUser() + " has left the chat room");
                    user = "";
                    outToAll("LIST" + getUserList());
                    logger.log(Level.INFO, clientSocket.getRemoteSocketAddress().toString() + " " + "client.Client disconnected");

                    //Stops the while loop
                    run = false;
                    continue;
                }

                String result;
                switch (request.substring(0,4))
                {
                    case "DATA":
                        if (!isDataValid(request))
                        {
                            String response = "SERVER_ER 1: Bad Syntax DATA <<user_name>>: <<free text…>> Max 250 user characters";
                            output.println(response);
                            logger.log(Level.INFO, clientSocket.getRemoteSocketAddress().toString() + " " + response);
                            continue;
                        }

                        result = request.substring(5);
                        outToAll(result);
                        logger.log(Level.INFO, clientSocket.getRemoteSocketAddress().toString() + " " +  result);
                        break;

                    case "QUIT":
                        String response = user + " has left the chat room";
                        outToAll(response);
                        logger.log(Level.INFO, clientSocket.getRemoteSocketAddress().toString() + " " + response);

                        user = "";

                        response = "LIST" + getUserList();
                        outToAll(response);
                        logger.log(Level.INFO, clientSocket.getRemoteSocketAddress().toString() + " " + response);

                        //Stops the while loop
                        run = false;
                        break;

                    case "IMAV":
                        IMAV = LocalTime.now();
                        break;

                    case "LIST":
                        result = "LIST" + getUserList();
                        output.println(result);
                        logger.log(Level.INFO, clientSocket.getRemoteSocketAddress().toString() + " " + result);
                        break;

                    default:
                        response = "SERVER_ER 2: Unknown Command - No such command exists!";
                        output.println(response);
                        logger.log(Level.INFO, clientSocket.getRemoteSocketAddress().toString() + " " + response);
                        break;
                }
            }
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, e.getStackTrace().toString());
        }
        finally
        {
            output.close();
            try
            {
                input.close();
                clientSocket.close();
            }
            catch (IOException e)
            {
                logger.log(Level.SEVERE, e.getStackTrace().toString());
            }
        }
    }

    private void outToAll(String message)
    {
        for (ServerClientHandler oneClient: allOnlineClients)
        {
            oneClient.output.println(message);
        }
    }

    private String getUserList()
    {
        String users = "";
        for (ServerClientHandler oneClient: allOnlineClients)
        {
            users = users + " " + oneClient.getUser();
        }
        return users;
    }

    public boolean isDataValid(String request)
    {
        String resultOfValidate = request.substring(request.indexOf(":") + 2);
        if(resultOfValidate.length() > 250)
        {
            return false;
        }
        return true;
    }
}
