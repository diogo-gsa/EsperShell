import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;



/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 */


//******************************************
//    DEPRECATED CLASS (Vai ser apagada...)
//******************************************
public class DataAcquisitionDriver
        implements Runnable {


    
    private EsperEngine esper;
    private int __DEFAULT_PORT__;
    private String __CONNECTIVITY_INTERFACE__; // "SOCKET" or "DEVICE_API" 


    public DataAcquisitionDriver(EsperEngine esper) {
        this.esper = esper;
        __DEFAULT_PORT__ = 7000; //7000;
        __CONNECTIVITY_INTERFACE__ = "DEVICE_API";
    }

    @Override
    public void run() {
        if (__CONNECTIVITY_INTERFACE__.equals("DEVICE_API")) {
            //TODO
        }
        if (__CONNECTIVITY_INTERFACE__.equals("SOCKET")) {
            initSocketConnection();
        }
    }



    //== Code Related with Socket Connection  =========================================================    
    private void initSocketConnection() {
        try {
            ServerSocket welcomeSocket = getWelcomeSocket();
            String eventSentBySimulator;

            Socket clientSocket = welcomeSocket.accept(); // stay blocked, waiting for a client connection                
            System.out.println("Connection established with simulator");

            BufferedReader receivedFromSocket = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
            //DataOutputStream sendToSocket = new DataOutputStream(clientSocket.getOutputStream());

            System.out.println("Waiting for events ...");
            while (true) {
                if (receivedFromSocket.ready()) {
                    eventSentBySimulator = receivedFromSocket.readLine();
                    handleEventRequestFromSocket(eventSentBySimulator); // PDU format excepted: (id:string,measure:float,ts:long)
                }
            }
        } catch (IOException e) {
            System.out.println("Error: Unable to connect with simulator");
            //e.printStackTrace();
        }
    }

    private void handleEventRequestFromSocket(String event) {
        //System.out.println("Events that will be sent to the engine:" + event); //TODO DEBUG
        event = (event.replace("(", "")).replace(")", "").replaceAll("\\s+|;", ""); //Remove white spaces = lib,17,234
        String[] eventParts = event.split(","); // ['lib','17','234']
        try {
            String deviceID = eventParts[0];
            double value = Double.parseDouble(eventParts[1]);
            long ts = Long.parseLong(eventParts[2]);
            esper.push(new DeviceReadingEvent(deviceID, ts, value));
        } catch (Exception e) {
            System.out.println("Error: Malformed event syntax from simulator");
        }
    }

    private ServerSocket getWelcomeSocket() throws IOException {
        ServerSocket welcomeSocket = null;
        try {
            welcomeSocket = new ServerSocket(__DEFAULT_PORT__);
        } catch (BindException e) {
            System.out.println("Default port " + __DEFAULT_PORT__
                    + " is already being used. We are looking for another one...");
            welcomeSocket = new ServerSocket(0);
        }
        System.out.println("Listening for events at port: " + welcomeSocket.getLocalPort());
        return welcomeSocket;
    }
    //=================================================================================================


}
