
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main
{
    private static Socket socket;
    private static ReceiveThread receiveThread;
    private static String userName;
    private static String hostAddress = "127.0.0.1";
    private static boolean connectionAccepted = false;

    public static void main(String[] args)
    {
        getConnection();

        Scanner scanner = new Scanner(System.in);

        String message;

        while (true)
        {
            message = scanner.nextLine();

            if (message.equals("exit"))
            {
                sendMessage(Protocol.QUIT());

                try
                {
                    socket.close();

                } catch (IOException e)
                {
                    e.printStackTrace();
                }

                return;
            }
            sendMessage(Protocol.DATA(userName, message));
        }

    }

    public static void getConnection()
    {
        Scanner in = new Scanner(System.in);

        while (!connectionAccepted)
        {
            System.out.println("Enter your user name: ");

            userName = in.nextLine();

            try
            {
                socket = new Socket(hostAddress, 5555);

                receiveThread = new ReceiveThread(socket);

                receiveThread.start();

                Writer writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                writer.write(Protocol.JOIN(userName, hostAddress, 5555) + "\r\n");

                writer.flush();

            } catch (IOException e)
            {
                e.printStackTrace();
            }

            try
            {
                Thread.sleep(500);

            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

    }

    public static void sendMessage(String text)
    {

        try
        {
            socket = new Socket(hostAddress, 5555);

            Writer writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            writer.write(text + "\r\n");

            writer.flush();

            if (text.equals("QUIT"))
            {
                System.exit(0);
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public static void setConnectionAccepted(boolean connectionAccepted)
    {
        Main.connectionAccepted = connectionAccepted;
    }
}
