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
        String response = "J_OK";
        output.println(response);
        logger.log(Level.INFO, client.getRemoteSocketAddress().toString() + " " + response);
        return true;
    }

    public void clientIsJoiningChat(Socket client)
    {
        ServerClientHandler clientThread = new ServerClientHandler(client, allClients, nextUserName);
        allClients.add(clientThread);
        threadPool.execute(clientThread);
    }
}
