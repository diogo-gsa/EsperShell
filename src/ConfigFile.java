import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;


public class ConfigFile {

    public Map<String, Long> getSettings(){
        return readConfigFile();
        
    }
    
    private Map<String, Long> readConfigFile(){
        //String filename = "src/httpServer/index.html";
        BufferedReader br = null;
        String configString = null;
        StringBuilder sb = new StringBuilder();
        String filename = "modbusDriver.config";
        try {
//            System.out.println("[DEBUG]Looking for file "+filename+" here: "+ System.getProperty("user.dir")); //TODO DEBUG 
            br = new BufferedReader(new FileReader(filename));
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            configString = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        System.out.println(configString);  //TODO DEBUG
        return parseConfigFile(configString, filename);
    }

    private Map<String, Long> parseConfigFile(String configString, String filename){
        Map<String, Long> settings = new TreeMap<String, Long>();  
        
        String[] isolatingParameters = configString.split("(\\{)|(\\})");
        String justParameters = null;
        for(String s : isolatingParameters){
            if(s.contains("_MeterID_"))
                 justParameters = s;
        }

        if(justParameters == null){
            System.out.println("Error: There is a problem with the structure of the file: "+filename);            
        }
//        else System.out.println(justParameters); //TODO DEBUG
        
        String[] parametersArray = justParameters.split(",");
        
        for(String parameter : parametersArray){
            parameter = parameter.replace("\"", "");
            String[] parts = parameter.split(":");
            try{
                String meterId = parts[0].trim();
                Long poolingTime = Long.parseLong(parts[1].trim());
                settings.put(meterId, poolingTime);
            }catch(Exception e){
                continue;
            }
        }
//        for(String s : settings.keySet()){ //TODO DEBUG
//            System.out.println("K: "+s+" | V:"+settings.get(s));
//        }
        return  settings;
    }
    
}
