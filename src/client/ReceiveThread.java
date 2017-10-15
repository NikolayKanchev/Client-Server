package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ReceiveThread extends Thread
{
    private Socket socket;
    private boolean isReceived_J_OK;
    private String chat = "";
    private ChatController chatController;

    @FXML
    private TextArea chatRoomArea, messageArea, usersArea;
    @FXML
    private Button sendButton;
    @FXML
    private Label redLabel, greenLabel;


    public ReceiveThread(Socket socket, TextArea chatRoomArea, TextArea messageArea,
                         Button sendButton, Label redLabel, Label greenLabel,
                         TextArea usersArea, ChatController chatController)
    {
        this.socket = socket;

        this.chatRoomArea = chatRoomArea;

        this.messageArea = messageArea;

        this.sendButton = sendButton;

        this.redLabel = redLabel;

        this.greenLabel = greenLabel;

        this.usersArea = usersArea;

        this.chatController = chatController;
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                Scanner scanner = new Scanner(socket.getInputStream());

                String str = "";

                if(scanner.hasNextLine())
                {
                    str = scanner.nextLine();
                }


                if(str.equals("J_OK"))
                {
                    isReceived_J_OK = true;

                    messageArea.setDisable(false);

                    chatRoomArea.setDisable(false);

                    messageArea.setDisable(false);

                    sendButton.setDisable(false);

                    usersArea.setDisable(false);

                    chatController.startSendingHeartbeat();

                    chatController.usernameAccepted();
                }

                if(str.startsWith("LIST"))
                {
                    String[] parts = str.split(" ");

                    String userNames = "";

                    for(int i = 1; i < parts.length; i++)
                    {
                        userNames += parts[i] + "\n";
                    }

                    usersArea.setText(userNames);
                }

                if(str.startsWith("DATA"))
                {
                    String s = str.replace("DATA ", "");

                    System.out.println(s);

                    updateChat(s);
                }

                if(str.startsWith("J_ER"))
                {
                    String[] parts = str.split(":");

                    synchronized (this)
                    {
                        Platform.runLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                redLabel.setText(parts[1]);

                                redLabel.setVisible(true);
                            }
                        });

                    }
                }

            } catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }

    public void updateChat(String message)
    {
        chat += message + "\n";

        chatRoomArea.setText(chat);
    }
}
