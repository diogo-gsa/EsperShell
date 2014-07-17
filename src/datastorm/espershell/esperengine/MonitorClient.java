package datastorm.espershell.esperengine;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 * 
 */

public class MonitorClient {

    private int serverPort;


    public MonitorClient(int serverPort) {
        this.serverPort = serverPort;
    }

    public void sendInfoToMonitor(String message) throws IOException {
        Socket socket = new Socket("localhost", serverPort);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(message);
        oos.close();
        socket.close();
    }
}
