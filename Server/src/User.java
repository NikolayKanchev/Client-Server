import java.time.LocalTime;

public class User
{
    private String userName;
    private String pass;
    private String ipAddress;
    private LocalTime lastAliveTime;

    public User(String ipAddress, String userName)
    {
        this.ipAddress = ipAddress;

        this.userName = userName;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getPass()
    {
        return pass;
    }

    public void setPass(String pass)
    {
        this.pass = pass;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    public LocalTime getLastAliveTime()
    {
        return lastAliveTime;
    }

    public void setLastAliveTime(LocalTime lastAliveTime)
    {
        this.lastAliveTime = lastAliveTime;
    }
}
