import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

import com.espertech.esper.client.EPStatementException;
import com.espertech.esper.client.EPStatementSyntaxException;
import com.espertech.esper.epl.core.EngineImportService;

/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 * 
 */

public class App {
    public static void main(String[] args) {

        EsperEngine esper = new EsperEngine();
        Map<Integer,QueryMetadata> queryCatalog = new TreeMap<Integer,QueryMetadata>(); 
        
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        String line = "";
        String command = "";
        System.out.print(">");
        
        
        while (true){
            try {                    
                line = br.readLine();
                command = command + line;
                if(command.contains(";")){
                    if(command.equals("exit;")){
                        return;
                    }                   
                    dispatchCommand(command, esper);
                    line = "";
                    command = "";
                    System.out.print(">");                    
                }else{
                    command = command + "\n";
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void dispatchCommand(String command, EsperEngine esper){
        // add select * from stream => ['add','add select * from stream']
        String[] tokens = command.split("\\W+",2);
        
        if(tokens.length != 0){
            switch (tokens[0]) {
                case "add"  : add_CommandHandler(tokens, esper);    break;
                case "send" : send_CommandHandler(tokens, esper);   break;
                    
                default: System.out.println("\'"+tokens[0]+"\'"+" is not recognized as a command.");          
            }
        }
    }
    
    //add command -> install queries in the engine
    // syntax: add select * from stream;
    private static void add_CommandHandler(String[] tokens, EsperEngine esper){
        // add select * from stream => ['add','add select * from stream']                
        String eplQuery = tokens[1]; //query that will be sent to Esper Engine
        
        try{    
              //remover ";" do final do statement da query 
              QueryMetadata queryMetaData = esper.installQuery(eplQuery.replace(";",""));
              System.out.println("\nQuery installed with success! \n"+queryMetaData+"\n");
        }catch(EPStatementException e ){
            System.out.println("\nCompilation Error: "+e.getMessage());            
            System.out.println("Evaluated Expression: "+e.getExpression()+"\n");            
        }
        
    }
  
    // syntax: send (deviceId, measure, timestamp);
    private static void send_CommandHandler(String[] tokens, EsperEngine esper){
        // send (lib, 17, 234); => ['send','(lib, 17, 234)']                        
        String event = tokens[1]; //event: (lib, 17, 234)
        event = (event.replace("(","")).replace(")","").replaceAll("\\s+|;",""); //Remove white spaces = lib,17,234
        String[] eventParts = event.split(","); // ['lib','17','234']

        String deviceID;
        double value;
        long ts;
        
        try{
            deviceID = eventParts[0];
            value  = Double.parseDouble(eventParts[1]);
            ts = Long.parseLong(eventParts[2]);
        }catch(Exception e){
            System.out.println("Error: Malformed input around ("+event+")");
            return;
        }
        esper.push(new DeviceReadingEvent(deviceID, ts, value));
    }

}
