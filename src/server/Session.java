package server;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Pattern;

public class Session implements Runnable
{
    private Socket socket;
    private String userName;
    private Scanner inputStream;
    private Writer writer;
    private LocalTime lastAliveTime;
    private boolean connClosed;
    private boolean visible = true;
    private boolean connApproved;


    public Session(Socket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        lastAliveTime = LocalTime.now();

        try
        {
            while (!connClosed)
            {
                inputStream = new Scanner(socket.getInputStream());

                String str = inputStream.nextLine();

                System.out.println("Server received -  <<<" + str + ">>> from : " + socket.getPort() + "at " + LocalTime.now());

                if (str.startsWith("JOIN"))
                {
                    String validation = checkJoinMessage(str);

                    if (validation.equals(""))
                    {
                        sendToClient(Protocol.J_OK());

                        Users.sendMessageToAll(Protocol.LIST(Users.getUserNames()));

                        connApproved = true;

                        checkLastAliveTime();

                    }else
                    {
                        sendToClient(validation);

                        System.out.println("The server sends : " + validation);

                        connApproved = false;

                    }
                }

                if(!connApproved && !str.startsWith("JOIN"))
                {
                    String s = Protocol.J_ER(2555, "You are not connected to MY CHAT !!!");

                    sendToClient(s);

                    System.out.println("The server sends : " + s);

                    return;
                }

                if (str.startsWith("DATA"))
                {
                    String validate = checkDataMessages(str.substring(5));

                    if (!validate.equals(""))
                    {
                        sendToClient(validate);

                        System.out.println("The server sends : " + validate);
                    }

                    if (str.contains("make me visible"))
                    {
                        visible = true;

                        Users.sendMessageToAll(Protocol.LIST(Users.getUserNames()));

                    }else if (str.contains("make me invisible"))
                    {
                        visible = false;

                        Users.sendMessageToAll(Protocol.LIST(Users.getUserNames()));

                    }else if(validate.equals(""))
                    {
                        Users.sendMessageToAll(str);
                    }
                }

                if (str.equals("IMAV"))
                {
                    Users.registerAliveMessage(socket);
                }

                if (str.equals("QUIT"))
                {
                    Users.deleteActiveUser(socket);

                    Users.sendMessageToAll(Protocol.LIST(Users.getUserNames()));

                    connClosed = true;
                }
            }

        } catch (IOException e)
        {
            e.printStackTrace();

        }catch (NoSuchElementException e)
        {

        }
    }

    private void checkLastAliveTime()
    {
        Timer timer = new Timer();

        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
               if(lastAliveTime.isBefore(LocalTime.now().minusMinutes(1)))
               {
                   if(connClosed)
                   {
                       timer.cancel();

                       return;
                   }
                   System.out.println("\n" + socket.getInetAddress().getHostAddress() + ": " + socket.getPort() + " didn't send IMAV - last minute !!!");

                   Users.deleteActiveUser(socket);

                   Users.sendMessageToAll(Protocol.LIST(Users.getUserNames()));

                   try
                   {
                       socket.close();

                       timer.cancel();

                       connClosed = true;

                   } catch (IOException e)
                   {
                       e.printStackTrace();
                   }
               }
            }
        }, 10*1000, 10*1000);
    }

    private String checkDataMessages(String str)
    {
        if(!str.contains(": "))
        {
            String s = Protocol.J_ER(2001, "Your request has wrong syntax !!!");

            return s;
        }

        String [] parts = str.split(": ", 2);

        String username = parts[0];

        String message = parts[1];

        if(username == null || username.isEmpty())
        {
            String s = Protocol.J_ER(2002, "The username is missing !!!");

            return s;
        }

        if(message == null || message.isEmpty())
        {
            String s = Protocol.J_ER(2003, "There is no message !!!");

           return s;
        }

        if (message.length() > 250)
        {
            String s = Protocol.J_ER(2003, "The message can be max 250 chars !!!");

            return s;
        }

        return "";
    }

    //checking userName, ip, port - sends error messages if necessary.
    private String checkJoinMessage(String str)
    {
        if (str == null || str.isEmpty())
        {
            String s = Protocol.J_ER(1000, "Empty request to the server!!!");

            return s;
        }

        if(!str.contains(", ") || !str.contains(":"))
        {
            String s = Protocol.J_ER(1001, "Wrong syntax in the request to the server !!!");

            return s;
        }

        //split the string into username, ip, port
        String[] parts = str.split(", ", 2);

        userName = parts[0];

        userName = userName.substring(5);

        //region check's the username
        if(userName == null || userName.isEmpty())
        {
            String s = Protocol.J_ER(1002, "Missing username !!!");

            return s;
        }

        if (userName.length() > 12)
        {
            String s = Protocol.J_ER(1111,"The Username can be max 12 chars !!! ");

            return s;
        }

        if(!Pattern.matches("[a-zA-Z0-9_-]+[a-zA-Z0-9_-]", userName))
        {
            String s = Protocol.J_ER(1111,"Only letters, digits, '-' and '_' are allowed !");

            return s;
        }


        if(Users.usernameExist(userName))
        {
            String s = Protocol.J_ER(1008, "The username exist !!!");

            return s;
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

            return s;
        }

        for (String s : ipParts)
        {
            try
            {
                int i = Integer.parseInt(s);

                if(i < 0 || i > 255)
                {
                    String message = Protocol.J_ER(1004, "Wrong IP address!!! \n The parts of the IP address must be in range of 0 - 255 !!!");

                    return s;
                }

            }catch (Exception e)
            {
                String message = Protocol.J_ER(1005, "Wrong IP address!!! \n The parts of the IP address must be integers !!!");

                return s;
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

                return s;
            }

        }catch (Exception e)
        {
            String s = Protocol.J_ER(1007, "Wrong port number!!! \n The port should be an integer !!!");

            return s;
        }
//endregion

        Users.addActiveUser(this);

        return "";
    }

    public void sendToClient(String message)
    {
        if (message.contains("J_OK"))
        {
            System.out.println("Server sends - <<<" + message + ">>> to the client " + socket.getInetAddress().getHostAddress());
        }

        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            writer.write(message +"\r\n");

            writer.flush();

        } catch (IOException e)
        {
            System.out.println(("The connection to " + socket.getInetAddress().getHostAddress() + " failed"));
        }
    }

    public Socket getSocket()
    {
        return socket;
    }

    public String getUserName()
    {
        return userName;
    }

    public Writer getWriter()
    {
        return writer;
    }

    public void setLastAliveTime(LocalTime lastAliveTime)
    {
        this.lastAliveTime = lastAliveTime;
    }

    public boolean isVisible()
    {
        return visible;
    }
}
