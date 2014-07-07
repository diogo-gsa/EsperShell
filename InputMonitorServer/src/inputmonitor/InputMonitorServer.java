package inputmonitor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;


public class InputMonitorServer {
    
    private ServerSocket welcomeSocket;
    private final int __DEFAULT_PORT__ = 62490;

    public static void main(String[] args) {
        InputMonitorServer ms = new InputMonitorServer();
        ms.init();
    }

    public void init() {
        System.out.println("===== Input datastream monitor =====");
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
            System.out.println("input > " + message);
            ois.close();
            clientSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    /*
    private ServerSocket getWelcomeSocket() throws IOException {
        ServerSocket welcomeSocket = null;
        try {
            welcomeSocket = new ServerSocket(__DEFAULT_PORT__);
        } catch (BindException e) {
            welcomeSocket = new ServerSocket(0);
            writeToFileNewSocket(welcomeSocket);
        }
        return welcomeSocket;
    }*/

    
}
