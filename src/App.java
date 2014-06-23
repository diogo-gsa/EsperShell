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

}
