package example_chat_program.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientServerHandler implements Runnable
{
    private Socket server;
    private BufferedReader input;

    public ClientServerHandler()
    {
    }

    public ClientServerHandler(Socket serverSocket)
    {
        server = serverSocket;
        try
        {
            input = new BufferedReader(new InputStreamReader(server.getInputStream()));
        }
        catch (IOException e)
        {
            /**
             * https://stackoverflow.com/questions/12095378/difference-between-e-printstacktrace-and-system-out-printlne
             */
            e.printStackTrace(); //Uses System.err
        }
    }

    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                String serverResponse = input.readLine();

                if (serverResponse == null || serverResponse.startsWith("J_ER"))
                {
                    System.err.println("Closing the client");
                    break;
                }
                System.out.println(serverResponse);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace(); //Uses System.err
        }
        finally
        {
            try
            {
                input.close();
                server.close();
                System.exit(1);
            }
            catch (IOException e)
            {
                e.printStackTrace(); //Uses System.err
            }
        }
    }
}
