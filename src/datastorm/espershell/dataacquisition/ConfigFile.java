package datastorm.espershell.dataacquisition;
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
    
    private Map<String,Integer> getDictionary(){
        TreeMap<String,Integer> dictionary = new TreeMap<String,Integer>();
        dictionary.put("PavCivil_TR1",1);
        dictionary.put("PavCivil_TR2",2);
        dictionary.put("PavCivil_TR3",3);
        dictionary.put("Complexo_Interdisciplinar",4);
        dictionary.put("PavCentral_Emerg.",5);
        dictionary.put("PavCentral_IlumExt.",6);
        dictionary.put("PavCentral_Normal.",7);
        dictionary.put("TorreNorte_TR1",8);
        dictionary.put("TorreNorte_TR2",9);
        dictionary.put("Jardim_Norte",10);
        dictionary.put("PavElectricidade_Geral",11);
        dictionary.put("PavInformaticaII",12);
        dictionary.put("PavMecanicaI_Geral",13);
        dictionary.put("PavMecanicaI_Ilum.Ext.",14);
        dictionary.put("PavQuimica_Geral",15);
        dictionary.put("PavQuimica_Ilum.Ext.",16);
        dictionary.put("TorreSul",17);
        dictionary.put("SeccaoDeFolhas",18);
        dictionary.put("PavMecanicaIII",19);            
        dictionary.put("PavMecanicaIII_TunelDeVento",20);
        dictionary.put("JardimSul",21);
        dictionary.put("Minas_TR",22);
        dictionary.put("PavAccaoSocial",23);
        dictionary.put("AEIST_Direcção",24);
        dictionary.put("AEIST_Geral",25);
        dictionary.put("AEIST_Bar",26);
        dictionary.put("PavInformaticaI",27);
        dictionary.put("PavInformaticaIII",28);
        dictionary.put("PortariaNorte",29);
        dictionary.put("PortariaSul",30);
        dictionary.put("Infantario",31);
        dictionary.put("Matematica",32);
        dictionary.put("Fisica",33);
        dictionary.put("InformaticaI",34);
        dictionary.put("MecanicaIV",35);
        dictionary.put("Cantina",36);
        dictionary.put("TagusPark_UTA_A4",37);
        dictionary.put("TagusPark_Nucleo14",38);
        dictionary.put("TagusPark_AnfiteatroA4",39);
        dictionary.put("TagusPark_Biblioteca",40);
        dictionary.put("TagusPark_lab1.58",41);
        dictionary.put("TagusPark_sala1.17",42);
        dictionary.put("TagusPark_sala1.19",43);
        dictionary.put("TagusPark_AnfiteatroA5",44);
        dictionary.put("TagusPark_Nucleo16",45);
        return dictionary;
    }
    
}
