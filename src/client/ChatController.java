package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ChatController
{
    private Socket socket;
    private OutputStream out;
    private Timer timer;

    @FXML
    private TextArea messageArea, chatRoomArea, usersArea;
    @FXML
    private TextField ipField, portField, userNameField;
    @FXML
    private Label redLabel,greenLabel, userNameLabel;
    @FXML
    private Button startChatButton, stopChatButton, connectButton, sendButton;
    @FXML
    private CheckBox invisibleCheckBox;

    public void sendMessage(ActionEvent actionEvent)
    {
        String s = messageArea.getText().replace("\n", " ");

        sendToServer(Protocol.DATA(userNameField.getText(), s));

        messageArea.setText("");
    }

    public void connect(ActionEvent actionEvent)
    {
        redLabel.setVisible(false);

        if(ipField.getText().isEmpty() || portField.getText().isEmpty())
        {
            redLabel.setText("You have to fill out all the fields !!!");

            redLabel.setVisible(true);

            return;
        }

        try
        {
            socket = new Socket(ipField.getText(), Integer.parseInt(portField.getText()));

            out = socket.getOutputStream();

            ReceiveThread receiveThread = new ReceiveThread(
                    socket, chatRoomArea, messageArea, sendButton,
                    redLabel, greenLabel, usersArea, this);

            receiveThread.start();

            setVisible(true);

        } catch (IOException e)
        {
            redLabel.setText("The connection to the server failed !!!");

            redLabel.setVisible(true);
        }
    }

    private void setVisible(boolean b)
    {
        greenLabel.setVisible(b);

        userNameField.setVisible(b);

        userNameLabel.setVisible(b);

        startChatButton.setVisible(b);

        ipField.setDisable(b);

        portField.setDisable(b);

        if(b == true)
        {
            connectButton.setVisible(false);
        }else
        {
            connectButton.setVisible(true);
        }

    }

    public void setVisibleRedLabel(MouseEvent mouseEvent)
    {
        redLabel.setVisible(false);
    }

    private void sendToServer(String message)
    {
        try
        {
            out.write((message + "\r\n").getBytes());

            out.flush();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void startChat(ActionEvent actionEvent)
    {
        sendToServer(Protocol.JOIN(userNameField.getText(), ipField.getText(), Integer.parseInt(portField.getText())));

    }

    public void usernameAccepted()
    {
        startChatButton.setVisible(false);

        stopChatButton.setVisible(true);

        userNameField.setDisable(true);

        invisibleCheckBox.setDisable(false);
    }

    public void stopChat(ActionEvent actionEvent)
    {
        sendToServer(Protocol.QUIT());

        setVisible(false);

        stopChatButton.setVisible(false);

        userNameField.setDisable(false);

        connectButton.setVisible(true);

        chatRoomArea.setDisable(true);

        messageArea.setDisable(true);

        sendButton.setDisable(true);

        invisibleCheckBox.setDisable(true);

        usersArea.setDisable(true);

        usersArea.setText("");

        chatRoomArea.setText("");

        redLabel.setVisible(false);
    }

    public void startSendingHeartbeat()
    {
        timer = new Timer();

        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                if(socket.isConnected())
                {
                    sendToServer(Protocol.IMAV());
                }else
                {
                    timer.cancel();
                }
            }
        }, 2*1000, 60*1000);
    }

    public void setInvisible(ActionEvent actionEvent)
    {
        if(invisibleCheckBox.isSelected())
        {
            sendToServer(Protocol.DATA(userNameField.getText(), "make me invisible"));

        }else
        {
            sendToServer(Protocol.DATA(userNameField.getText(), "make me visible"));
        }
    }

}
