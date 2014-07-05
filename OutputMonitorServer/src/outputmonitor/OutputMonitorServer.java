package outputmonitor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;


public class OutputMonitorServer {
    
    private ServerSocket welcomeSocket;
    private final int __DEFAULT_PORT__ = 62491;

    public static void main(String[] args) {
        OutputMonitorServer ms = new OutputMonitorServer();
        ms.init();
    }

    public void init() {
        System.out.println("===== Query output results monitor =====");
        try {
            welcomeSocket = new ServerSocket(__DEFAULT_PORT__);
            while (true) {
                Socket clientSocket = welcomeSocket.accept();
                handleClientRequest(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientRequest(Socket clientSocket) {
        try {
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
            String message = (String) ois.readObject();
            System.out.println("output > " + message);
            ois.close();
            clientSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }    
}
