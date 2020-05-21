package example_chat_program.server;

public class MainServer
{
    public static void main(String[] args)
    {
        Server server = new Server();
        server.clientListeners();
    }
}
