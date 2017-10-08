import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ReceiveSend extends Thread
{
    private Socket socket;


    public ReceiveSend(Socket socket)
    {
        this.socket = socket;

    }

    @Override
    public void run()
    {
        try
        {
            Scanner scanner = new Scanner(socket.getInputStream());

            String str = scanner.nextLine();

            System.out.println("Server received -  <<<" + str + ">>> from : " + socket.getInetAddress().getHostAddress());

            if (str.startsWith("JOIN"))
            {
                boolean bul = checkJoinMessage(str);

                if(!bul)
                {
                    return;
                }

                Users.sendToClient(Protocol.J_OK(), socket);

                Users.sendMessageToAll(Protocol.LIST(Users.getUserNames()));

            }
            if (str.startsWith("DATA"))
            {
                boolean bul = checkDataMessages(str.substring(5));

                if(!bul)
                {
                    String s = Protocol.J_ER(2000, "The server rejected to post the message!!!");

                    Users.sendToClient(s, socket);

                    System.out.println("The server sends : " + s);

                    return;
                }

                Users.sendMessageToAll(str);
            }

            if (str.equals("IMAV"))
            {
                Users.registerAliveMessage(socket);

                return;
            }

            if (str.equals("QUIT"))
            {
                Users.deleteActiveUser(socket.getInetAddress().getHostAddress());

                Users.sendMessageToAll(Protocol.LIST(Users.getUserNames()));

                socket.close();
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private boolean checkDataMessages(String str)
    {
        if(!str.contains(": "))
        {
            String s = Protocol.J_ER(2001, "Your request has wrong syntax !!!");

            Users.sendToClient(s, socket);

            System.out.println("The server sends : " + s);


            return false;
        }

        String [] parts = str.split(": ", 2);

        String username = parts[0];

        String message = parts[1];

        if(username == null || username.isEmpty())
        {
            String s = Protocol.J_ER(2002, "The username is missing !!!");

            Users.sendToClient(s, socket);

            System.out.println("The server sends : " + s);

            return false;
        }

        if(message == null || message.isEmpty())
        {
            String s = Protocol.J_ER(2003, "There is no message !!!");

            Users.sendToClient(s, socket);

            System.out.println("The server sends : " + s);

            return false;
        }

        if (message.length() > 250)
        {
            String s = Protocol.J_ER(2003, "The message can be max 250 chars !!!");

            Users.sendToClient(s, socket);

            System.out.println("The server sends : " + s);

            return false;
        }

        return true;
    }

    //checking userName, ip, port - sends error messages if necessary.
    private boolean checkJoinMessage(String str)
    {
        if (str == null)
        {
            String s = Protocol.J_ER(1000, "Empty request to the server!!!");

            Users.sendToClient(s, socket);

            System.out.println("The server sends : " + s);

            return false;
        }

        if(!str.contains(", ") || !str.contains(":"))
        {
            String s = Protocol.J_ER(1001, "Wrong syntax in the request to the server !!!");

            Users.sendToClient(s, socket);

            System.out.println("The server sends : " + s);

            return false;
        }

        //split the string into username, ip, port
        String[] parts = str.split(", ", 2);

        String userName = parts[0];

        userName = userName.substring(5);

        //region check's the username
        if(userName == null || userName.isEmpty())
        {
            String s = Protocol.J_ER(1002, "Missing username !!!");

            Users.sendToClient(s, socket);

            System.out.println("The server sends : " + s);

            return false;
        }

        if (userName.length() > 12 || !Pattern.matches("[a-zA-Z0-9_-]+[a-zA-Z0-9_-]", userName))
        {
            String s = Protocol.J_ER(1111,"Only letters, digits, '-' and '_' are allowed and no longer than 12 chars !!! ");

            Users.sendToClient(s, socket);

            System.out.println("The server sends : " + s);

            return false;
        }


        if(Users.usernameExist(userName))
        {
            String s = Protocol.J_ER(1008, "The username exist !!!");

            Users.sendToClient(s, socket);

            System.out.println("The server sends : " + s);

            return false;
        }

//endregion

        String ipAndPort = parts[1];

        String[] parts1 = ipAndPort.split(":", 2);

        String ip = parts1[0];

        String port = parts1[1];


        //region split the IP into parts and check's it
        String[] ipParts = ip.split("\\.");

        if (ipParts.length != 4)
        {
            String s = Protocol.J_ER(1003, "This is not a valid IP address!!!");

            Users.sendToClient(s, socket);

            System.out.println("The server sends : " + s);

            return false;
        }

        for (String s : ipParts)
        {
            try
            {
                int i = Integer.parseInt(s);

                if(i < 0 || i > 255)
                {
                    String message = Protocol.J_ER(1004, "Wrong IP address!!! \n The parts of the IP address must be in range of 0 - 255 !!!");

                    Users.sendToClient(message, socket);

                    System.out.println("The server sends : " + message);

                    return false;
                }

            }catch (Exception e)
            {
                String message = Protocol.J_ER(1005, "Wrong IP address!!! \n The parts of the IP address must be integers !!!");

                Users.sendToClient(message, socket);

                System.out.println("The server sends : " + message);

                return false;
            }
        }
//endregion

        //region check's the port
        try
        {
            int j = Integer.parseInt(port);

            if(j < 1024 && j > 65000)
            {
                String s = Protocol.J_ER(1006, "Wrong port number!!! The port number should be - '5555'");

                Users.sendToClient(s, socket);

                System.out.println("The server sends : " + s);

                return false;
            }

        }catch (Exception e)
        {
            String s = Protocol.J_ER(1007, "Wrong port number!!! \n The port should be an integer !!!");

            Users.sendToClient(s, socket);

            System.out.println("The server sends : " + s);

            return false;
        }
//endregion

        Users.addActiveUser(userName, socket);

        return true;
    }
}
