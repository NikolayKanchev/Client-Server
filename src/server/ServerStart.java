package server;

import javafx.application.Platform;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerStart implements Runnable
{
    private ServerController serverController;

    private ServerSocket serverSocket;

    private volatile boolean shutdown;


    public ServerStart(ServerController serverController)
    {
        this.serverController = serverController;
    }

    @Override
    public void run()
    {
        startServer(serverController.getPort());
    }

    private void startServer(int port)
    {
        try
        {
            serverSocket = new ServerSocket(port);

            Platform.runLater(
                    () -> {
                        serverController.showServerRunning(true);
                    }
            );

            while (!shutdown)
            {
                Socket socket = serverSocket.accept();

                Session session = new Session(socket, serverController);

                Thread thread = new Thread(session);

                thread.start();

                System.out.println("Waiting for clients..........");

                Platform.runLater(
                        () -> {
                            serverController.updateGreenLabel("Server waiting for clients ....");
                        }
                );
            }

        } catch (IOException e) {}
    }

    public void setShutdown(boolean terminate)
    {
        shutdown = true;

        try
        {
            serverSocket.close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
