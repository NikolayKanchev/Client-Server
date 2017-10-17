package server;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class ServerController
{
    @FXML
    private Label greenLabel, portLabel;
    @FXML
    private Button startButton, stopButton;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField portTF;

    private Thread thread;

    private ServerStart serverStart;

    private String text = "";

    public void setVisibleGreenLabel(MouseEvent mouseEvent)
    {
        greenLabel.setVisible(false);
    }

    public void startServer(ActionEvent actionEvent)
    {

        int port;
        try
        {
            port = Integer.parseInt(portTF.getText());

            if(port == 0)
            {
                return;
            }

            serverStart = new ServerStart(this);

            thread = new Thread(serverStart);

            thread.start();

        } catch (Exception e)
        {
            greenLabel.setText("     Invalid input !!! Try again !!!");

            greenLabel.setVisible(true);

            return;
        }
    }

    public void stopServer(ActionEvent actionEvent)
    {
        Users.stopAllSockets();

        Users.deleteAllUsers();

        serverStart.setShutdown(true);

        showServerRunning(false);
    }

    public void showServerRunning(boolean serverRunning)
    {
        greenLabel.setText("The server is up and running ............");

        greenLabel.setVisible(serverRunning);

        stopButton.setVisible(serverRunning);

        portLabel.setDisable(serverRunning);

        portTF.setDisable(serverRunning);

        if(serverRunning)
        {
            startButton.setVisible(false);

            textArea.setDisable(false);
        }else
        {
            startButton.setVisible(true);

            textArea.setDisable(true);
        }

    }

    public int getPort()
    {
        return Integer.parseInt(portTF.getText());
    }

    public void updateGreenLabel(String text)
    {
        greenLabel.setText(text);

        greenLabel.setVisible(true);
    }

    public void updateTextAria(String s)
    {
        text += s + "\n";

        textArea.setText(text);
    }
}
