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

public class DataAcquisitionDriver
        implements Runnable {

    private EsperEngine esper;
    private int __DEFAULT_PORT__;

    public DataAcquisitionDriver(EsperEngine esper) {
        this.esper = esper;
        __DEFAULT_PORT__ = 7000; //7000;
    }

    @Override
    public void run() {
        try {
            ServerSocket welcomeSocket = getWelcomeSocket();
            String clientRequest;
            while(true){
                Socket clientSocket = welcomeSocket.accept(); // stay blocked, waiting for a client connection                
                System.out.println("tenho um cliente ligado");
                BufferedReader receivedFromSocket = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                DataOutputStream sendToSocket = new DataOutputStream(clientSocket.getOutputStream());
                
                System.out.println("vou ler a mensagem que cliente enviou");                
                
                while(true){                       
                    if(receivedFromSocket.ready()){
                        clientRequest = receivedFromSocket.readLine();
                    
                    System.out.println("vou enviar mensagem do cliente para o handler");                
                    handleClientRequest(clientRequest); // PDU format excepted: (id:string,measure:float,ts:long)
                    }
               }
                
            }
        
        } catch (IOException e) {
            System.out.println("Erro: Estou com problemas a receber eventos =(");
            e.printStackTrace();
        }
    }


    private void handleClientRequest(String readLine) {
        System.out.println("------received from client:"+readLine);
    }

    private ServerSocket getWelcomeSocket() throws IOException {
        ServerSocket welcomeSocket = null;
        try {
            welcomeSocket = new ServerSocket(__DEFAULT_PORT__);
        } catch (BindException e) {
            System.out.println("Default port "+__DEFAULT_PORT__+" is already being used. We are looking for another one...");
            welcomeSocket = new ServerSocket(0);
        }
        System.out.println("Listening for events at port: " + welcomeSocket.getLocalPort());
        return welcomeSocket;
    }


}
