package server;

import java.io.IOException;
import java.io.Writer;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;

public class Users
{
    private static ArrayList<Session> users = new ArrayList<>();

    public static void sendMessageToAll(String message)
    {
        int numberOfUsers = 0;

        Socket socket;

        Writer writer;

        Iterator<Session> iterator = users.iterator();

        while (iterator.hasNext())
        {
            Session s = iterator.next();

            socket = s.getSocket();

            if(socket.isConnected())
            {
                try
                {
                    writer = s.getWriter();

                    writer.write(message +"\r\n");

                    writer.flush();

                    numberOfUsers++;

                } catch (IOException e)
                {
                    System.out.println(("The connection to " + socket.getInetAddress().getHostAddress() + " failed"));
                }
            }
        }

        if(numberOfUsers != 0)
        {
            System.out.println("The message : <<<" + message + ">>> was send to all users !!!");
        }
    }

    public static void deleteActiveUser(Socket socket)
    {
        Iterator<Session> iterator = users.iterator();

        while (iterator.hasNext())
        {
            Session s = iterator.next();

            if(s.getSocket() == socket)
            {
                iterator.remove();

                sendMessageToAll(Protocol.DATA("ADMIN", s.getUserName() + " left the chat room"));
            }
        }
    }

    public static String getUserNames()
    {
        String str = "";

        Iterator<Session> iterator = users.iterator();

        while (iterator.hasNext())
        {
            Session session = iterator.next();

            if(session.isVisible())
            {
                str += session.getUserName() + " ";
            }
        }
        return str;
    }

    public static void addActiveUser(Session session)
    {
        users.add(session);
    }

    public static boolean usernameExist(String userName)
    {
        Iterator<Session> iterator = users.iterator();

        while (iterator.hasNext())
        {
            Session session = iterator.next();

            if(session.getUserName().equals(userName))
            {
                return true;
            }
        }
        return false;
    }

    public static void registerAliveMessage(Socket socket)
    {
        Iterator<Session> iterator = users.iterator();

        while (iterator.hasNext())
        {
            Session s = iterator.next();

            if (s.getSocket() == socket)
            {
                s.setLastAliveTime(LocalTime.now());
            }
        }
    }
}
