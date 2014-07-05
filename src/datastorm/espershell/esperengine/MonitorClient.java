package datastorm.espershell.esperengine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class MonitorClient {

    private final int __DEFAULT_PORT__ = 62490;

    public void sendInfoToMonitor(String message) throws IOException {
        Socket socket = new Socket("localhost", __DEFAULT_PORT__);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(message);
        oos.close();
        socket.close();
    }
}
