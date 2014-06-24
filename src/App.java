import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.espertech.esper.client.EPStatementException;
import com.espertech.esper.client.EPStatementSyntaxException;
import com.espertech.esper.epl.core.EngineImportService;


public class App {
    public static void main(String[] args) {

        EsperEngine esper = new EsperEngine();
        
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
                   
                    System.out.println(command);
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
  
    // syntax: add select * from stream;
    private static void add_CommandHandler(String[] tokens, EsperEngine esper){
        // add select * from stream => ['add','add select * from stream']                
        String eplQuery = tokens[1]; //query that will be sent to Esper Engine
       
        try{    
              //remover ";" do final do statement da query 
              QueryMetadata queryMetaData = esper.installQuery(eplQuery.replace(";",""));
              System.out.println("Query successfully installed:\n"+queryMetaData);
        }catch(EPStatementException e ){
            System.out.println("Compilation Error: "+e.getMessage());            
            System.out.println("Evaluated Expression: "+e.getExpression());            
        }
        
    }
  
    // syntax: send (deviceId, measure, timestamp);
    private static void send_CommandHandler(String[] tokens, EsperEngine esper){
        // send (lib, 17, 234); => ['send','(lib, 17, 234)']                        
        String event = tokens[1]; //event: (lib, 17, 234)
        event = (event.replace("(","")).replace(")","").replaceAll("\\s+|;",""); //Remove white spaces = lib,17,234
        System.out.println("After Trim: "+event);
        String[] eventParts = event.split(","); // ['lib','17','234']

        String deviceID;
        double measure;
        long ts;
        
        try{
            deviceID = eventParts[0];
            measure  = Double.parseDouble(eventParts[1]);
            ts = Long.parseLong(eventParts[2]);
        }catch(Exception e){
            System.out.println("Error: Malformed input around ("+event+")");
            return;
        }
        
        System.out.println("deviceID: "+deviceID);
        System.out.println("measure: "+measure);
        System.out.println("ts: "+ts);
        
        

    }

}
