package server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain
{

    public static void main(String[] args)
    {
        ServerSocket serverSocket;

        try
        {
            serverSocket = new ServerSocket(5555);

            System.out.println("The server is running........");


            while (true)
            {
                Socket socket = serverSocket.accept();

                Session session = new Session(socket);

                Thread thread = new Thread(session);

                thread.start();

                System.out.println("Waiting for clients..........");
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
