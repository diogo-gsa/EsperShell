import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
                case "add": addCommandHandler(tokens, esper); //System.out.println("add command not yet implemented.");
                    break;

                default: System.out.println("\'"+tokens[0]+"\'"+" is not recognized as a command.");          
            }
        }
    }

    private static void addCommandHandler(String[] tokens, EsperEngine esper){
        // add select * from stream => ['add','add select * from stream']                
        String eplQuery = tokens[1]; //query that will be sent to Esper Engine
       
        try{
              //TODO antes de enviares a query tens de retirar o --> ;
              QueryMetadata queryMetaData = esper.installQuery(eplQuery);
              System.out.println("Query instalada com suesso\n"+queryMetaData);
        }catch(EPStatementSyntaxException e){
            System.out.println("Compilation Erro: "+e.getMessage());            
            System.out.println("Evaluated Expression: "+e.getExpression());            
        }
        
    }

}
