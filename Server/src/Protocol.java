
public class Protocol
{

    public static String J_OK()
    {
        return "J_OK";
    }

    public static String J_ER(int errorCode, String message)
    {
       return "J_ER " + errorCode + ": " + message;
    }

    public static String DATA(String user_name, String text)
    {
       return "DATA " + user_name + ": " + text;
    }

    public static String LIST(String userNames)
    {
        return "LIST " + userNames;
    }
}
