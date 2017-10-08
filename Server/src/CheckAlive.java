import java.io.IOException;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class CheckAlive extends Thread
{
    private Socket socket;

    public CheckAlive(Socket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        Timer timer = new Timer();

        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                ArrayList<User> usersArrayList = Users.getUsers();

                User[] users = new User[usersArrayList.size()];

                users = usersArrayList.toArray(users);

                for (int i = 0; i < users.length; i++)
                {
                    if(users[i].getLastAliveTime().isBefore(LocalTime.now().minusMinutes(1)))
                    {
                        System.out.println("\nLast received IMAV message from : " + users[i].getIpAddress() + " is older than 1 minute");

                        Users.deleteActiveUser(users[i].getIpAddress());

                        Users.sendMessageToAll(Protocol.LIST(Users.getUserNames()));
                    }
                }
            }
        }, 20*1000, 20*1000);
    }
}
