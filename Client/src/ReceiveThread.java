import java.io.IOException;
import java.net.Socket;
import java.time.LocalTime;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class ReceiveThread extends Thread
{
    private Socket socket;

    public ReceiveThread(Socket socket)
    {
        this.socket = socket;
    }


    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                Scanner scanner = new Scanner(socket.getInputStream());

                String str = scanner.nextLine();

                if(!validServerReply(str) && Main.isReceived_J_OK())
                {
                    System.out.println("Not valid reply from the server !!!");
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private boolean validServerReply(String str)
    {

//    region    It checks the DATA message
        if (str.startsWith("DATA") && Main.isReceived_J_OK())
        {
            str = str.substring(5);

            if (!str.contains(": "))
            {
                return false;
            }

            String[] parts = str.split(": ");

            String userName = parts[0];

            String message = parts[1];

            if (userName.isEmpty() || userName == null)
            {
                return false;
            }

            if (message.isEmpty() || message == null || message.length() > 250)
            {
                return false;
            }

            if (str.startsWith("ADMIN"))
            {
                System.out.println("******************************");
                System.out.println(str);
                System.out.println("******************************\n");
                return true;
            }

            System.out.println(str);

            return true;
        }
//        endregion

//        region It checks "J_OK" message, it sets connectionAccepted to true and starts a timer for IMAV message
        if (str.equals("J_OK"))
        {
            Main.setConnectionAccepted(true);

            Main.setReceived_J_OK(true);

            System.out.println("** Welcome to Nikolay's CHAT **\n");

            Timer timer = new Timer();

            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    Main.sendMessage(Protocol.IMAV());
                }
            }, 60 * 1000, 60 * 1000);

            return true;
        }
//        endregion

//        region It checks the J_ER message
        if (str.startsWith("J_ER") && Main.isReceived_J_OK())
        {
            if (!str.contains(": "))
            {
                return false;
            }

            str = str.substring(5);

            String[] parts = str.split(": ");

            String errorNum = parts[0];

            String message = parts[1];

            if (message == null)
            {
                return false;
            }

            try
            {
                int error = Integer.parseInt(errorNum);

                if (error >= 1000 && error < 1999)
                {
                    System.out.println(str);

                    Main.setConnectionAccepted(false);

                    return true;
                }

            } catch (Exception e)
            {
                return false;
            }

            System.out.println(str);

            return true;
        }
//        endregion

//      region  It checks the LIST message
        if (str.startsWith("LIST") && Main.isReceived_J_OK())
        {
            str = str.substring(5);

            String[] strings = str.split(" ");

            System.out.println("*******+ Active Users: +******");

            System.out.println("******************************");

            for (String s : strings)
            {
                if (30 - s.length() != 0)
                {
                    System.out.print(s);

                    for (int j = 1; j <= (25 - s.length()); j++)
                    {
                        System.out.print(" ");
                    }

                    System.out.print("*****\n");
                }
            }

            System.out.println("******************************\n");

            return true;
        }
//        endregion

        return false;
    }
}
