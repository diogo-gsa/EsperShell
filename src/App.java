import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class App {
    public static void main(String[] args) {

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
                    dispatchCommand(command);
                    
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
    
    private static void dispatchCommand(String command){
        // add select * from stream => ['add','add select * from stream']
        String[] tokens = command.split("\\W+",2);
        
        if(tokens.length != 0){
            switch (tokens[0]) {
                case "add": addCommandHandler(tokens); //System.out.println("add command not yet implemented.");
                    break;

                default: System.out.println("\'"+tokens[0]+"\'"+" is not recognized as a command.");          
            }
        }
    }

    private static void addCommandHandler(String[] tokens){
        // add select * from stream => ['add','add select * from stream']                
        String eplQuery = tokens[1]; //query that will be sent to Esper Engine
       
        System.out.println("Engine: "+eplQuery); 
        //TODO com este EPL statement criar a objecto da class QueryMetadata e enviar isso para o esper
        // Nota: so depois da query ser inserida noengine com sucesso é que lhe deve ser atribuid o ID
       
    }

}
