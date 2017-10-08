import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class Users
{
    private static ConcurrentHashMap<String, Socket> activeClients = new ConcurrentHashMap<>();
    private static ArrayList<User> users = new ArrayList<>();

    public static void sendMessageToAll(String string)
    {
        int numberOfUsers = 0;

        for(String clientHost: activeClients.keySet())
        {
            Socket socket = activeClients.get(clientHost);

            if (socket.getInetAddress().getHostAddress() != null)
            {
                numberOfUsers = sendToClient(string, socket);
            }
        }

        if(numberOfUsers != 0)
        {
            System.out.println("The message : <<<" + string + ">>> was send to all users !!!");
        }
    }

    public static void deleteActiveUser(String s)
    {
        for (String clientHost : activeClients.keySet())
        {
            if (clientHost.equals(s))
            {
                activeClients.keySet().remove(clientHost);
            }
        }

        Iterator<User> iterator = users.iterator();

        while (iterator.hasNext())
        {
            User user = iterator.next();

            if(user.getIpAddress().equals(s))
            {
                iterator.remove();

                sendMessageToAll(Protocol.DATA("ADMIN", user.getUserName() + " left the chat room"));
            }
        }
    }

    public static String getUserNames()
    {
        String str = "";

        for (User user: users)
        {
            str += user.getUserName() + " ";
        }

        return str;
    }

    public static void addActiveUser(String username, Socket socket)
    {
        activeClients.put(socket.getInetAddress().getHostAddress(), socket);

        User user = new User(socket.getInetAddress().getHostAddress(), username);

        user.setLastAliveTime(LocalTime.now());

        users.add(user);
    }

    public static boolean usernameExist(String userName)
    {
        for (User user: users)
        {
            if(user.getUserName().equals(userName))
            {
                return true;
            }
        }
        return false;
    }

    public static int sendToClient(String message, Socket socket)
    {
        int users = 0;

        String address = socket.getInetAddress().getHostAddress();

        for (String s : activeClients.keySet())
        {
            if(s.equals(address))
            {
                socket = activeClients.get(s);
            }

            users++;
        }

        try
        {

            Writer writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            writer.write(message +"\r\n");

            writer.flush();

        } catch (IOException e)
        {
            System.out.println(("The connection to " + socket.getInetAddress().getHostAddress() + " failed"));
        }

        return users;
    }

    public static void registerAliveMessage(Socket socket)
    {
        for(User user: users)
        {
            if (user.getIpAddress().equals(socket.getInetAddress().getHostAddress()))
            {
                user.setLastAliveTime(LocalTime.now());
            }
        }
    }

    public static ArrayList<User> getUsers()
    {
        return users;
    }
}
