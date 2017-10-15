package client;

public class Protocol
{
    public static String JOIN (String user_name, String server_ip, int server_port)
    {
        return "JOIN " + user_name + ", " + server_ip + ":" + server_port;
    }

    public static String DATA(String user_name, String text)
    {
       return "DATA " + user_name + ": " + text;
    }

    public static String IMAV()
    {
        return "IMAV";
    }

    public static String QUIT()
    {
        return "QUIT";
    }

}
