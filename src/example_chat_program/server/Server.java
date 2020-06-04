package example_chat_program.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server
{
    private int port = 9696;
    private ArrayList<ServerClientHandler> allClients = new ArrayList<>();
    private ExecutorService threadPool = Executors.newFixedThreadPool(5);
    private Logger logger = SharedLog.getInstance();
    private String nextUserName = null;

    public Server()
    {
    }

    public void clientListeners()
    {
        try
        {
            ServerSocket listeners = new ServerSocket(port);

            while (true)
            {
                logger.log(Level.INFO, "[Server] Waiting for Client to join");
                Socket client = listeners.accept();

                //Reads text from Client
                BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));

                String request = input.readLine();
                logger.log(Level.INFO,client.getRemoteSocketAddress().toString() + " " + request);

                if (canClientJoin(client, request))
                {
                    clientIsJoiningChat(client);
                }
            }
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, e.getStackTrace().toString());
        }
    }

    public boolean canClientJoin(Socket client, String request) throws IOException
    {
        //Prints output to Client
        PrintWriter output = new PrintWriter(client.getOutputStream(), true);

        if (!request.startsWith("JOIN"))
        {
            String response = "J_ER 1: Request did not start with JOIN";
            output.println(response);
            logger.log(Level.INFO, client.getRemoteSocketAddress().toString() + " " + response);

            client.close();
            logger.log(Level.INFO, client.getRemoteSocketAddress().toString() + " was closed");
            return false;
        }

        nextUserName = request.split(" ") [1];

        if(!isNextUserNameValid(nextUserName))
        {
            String response = "J_ER 2: Username is max 12 chars long, only letters, digits, ‘-‘ and ‘_’ allowed";
            output.println(response);
            logger.log(Level.INFO,client.getRemoteSocketAddress().toString() + " " +  response);
            client.close();
            logger.log(Level.INFO, client.getRemoteSocketAddress().toString() + " was closed");
            return false;
        }

        if (doUserNameExists())
        {
            String response = "J_ER 3: Duplicate Username";
            output.println(response);
            logger.log(Level.INFO, client.getRemoteSocketAddress().toString() + " " + response);

            client.close();
            logger.log(Level.INFO, client.getRemoteSocketAddress().toString() + " was closed");
            return false;
        }

        String response = "J_OK";
        output.println(response);
        logger.log(Level.INFO, client.getRemoteSocketAddress().toString() + " " + response);
        return true;
    }

    public boolean doUserNameExists()
    {
        boolean existUserName = false;
        for (ServerClientHandler oneClient: allClients)
        {
            //user comes from ServerClientHandler
            String current = oneClient.user;
            if(nextUserName.equals(current))
            {
                existUserName = true;
            }
        }
        return existUserName;
    }

    public boolean isNextUserNameValid(String nextUserName)
    {
        if (nextUserName.length() <= 12 && nextUserName.matches("[a-zA-Z0-9_\\-]+"))
        {
            return true;
        }
        return false;
    }

    public void clientIsJoiningChat(Socket client)
    {
        ServerClientHandler clientThread = new ServerClientHandler(client, allClients, nextUserName);
        allClients.add(clientThread);
        threadPool.execute(clientThread);
    }
}
