import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main
{
    public static void main(String[] args)
    {
        ServerSocket serverSocket;

        Socket socket;

        System.out.println("The Server is running...................\n");

        try
        {
            serverSocket = new ServerSocket(5555, 100);

            while (true)
            {
                socket = serverSocket.accept();

                ReceiveSend receiveSend = new ReceiveSend(socket);

                receiveSend.start();

                CheckAlive checkAlive = new CheckAlive(socket);

                checkAlive.start();

                Thread.currentThread().sleep(500);

                System.out.println("\nWaiting for clients......................\n");
            }
        } catch (IOException e)
        {
            e.printStackTrace();

        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
