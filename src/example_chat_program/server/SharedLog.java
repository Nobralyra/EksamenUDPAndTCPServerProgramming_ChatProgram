package example_chat_program.server;

import java.util.logging.Logger;

public class SharedLog
{
    /**
     * Static field that make a definition from the logger package
     */
    private static Logger logger = Logger.getAnonymousLogger();

    /**
     * Creates a private constructor Sharedlog
     */
    private SharedLog ()
    {}

    /**
     * Static method that controls the access to the server.SharedLog instance
     * @return
     */
    public static Logger getInstance ()
    {
        return logger;
    }
}
