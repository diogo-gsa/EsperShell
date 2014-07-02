package datastorm.espershell.dataacquisition;


import ist.smartoffice.datapointconnectivity.DatapointAddress;
import ist.smartoffice.datapointconnectivity.DatapointMetadata;
import ist.smartoffice.datapointconnectivity.DatapointMetadata.MetadataBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 * 
 */

public class ConfigFile {

    private final Map<String, ConfigEntry> configEntriesMap = new TreeMap<String, ConfigEntry>();
    
    public Map<DatapointAddress,DatapointMetadata> getModbusDriverSettings(){
        Map<DatapointAddress,DatapointMetadata> res = new LinkedHashMap<DatapointAddress,DatapointMetadata>();
        for(String s : configEntriesMap.keySet()){
            DatapointAddress dpAddr = new DatapointAddress(configEntriesMap.get(s).getMeterId());
            MetadataBuilder m = new DatapointMetadata.MetadataBuilder();
            m.setReadDatapointAddress(Integer.toString(configEntriesMap.get(s).getModbusAddr()));
            m.setCurrentSamplingInterval(configEntriesMap.get(s).getPoolingTime());
            DatapointMetadata dpMD = m.build();
            res.put(dpAddr,dpMD);
        }
        return res;
    }
    
    
    public Map<String, Long> getPollerSettings(){
        Map<String, Long> res = new TreeMap<String, Long>();
        for(String s : configEntriesMap.keySet()){
            res.put(s, configEntriesMap.get(s).getPoolingTime());
        }
        return res;
    }
    

    public void readConfigFile(String filename) {
        //String filename = "src/httpServer/index.html";
        BufferedReader br = null;
        String configString = null;
        StringBuilder sb = new StringBuilder();
 
        try {
            // System.out.println("[DEBUG]Looking for file here: "+ System.getProperty("user.dir")); //TODO DEBUG 
            br = new BufferedReader(new FileReader(filename));
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            configString = sb.toString();
        } catch (IOException e) {
            System.out.println("The system cannot find the config file: " + filename);
            //e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(configString); //TODO DEBUG
        parseConfigFile(configString, filename);
        return;
    }

    private Map<String, Long> parseConfigFile(String jsonConfigFile, String filename) {
        Map<String, Long> settings = new TreeMap<String, Long>();
        JSONParser jsonParser = new JSONParser();
        try {
            JSONArray jsonArray = (JSONArray) jsonParser.parse(jsonConfigFile);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject obj = (JSONObject) jsonArray.get(i);
                try {
                    String modbusAddr = (String) obj.get("modbusAddr");
                    String meterId = (String) obj.get("meterId");
                    String poolingTime = (String) obj.get("PollingTime(microsec.)");
                    ConfigEntry entry = new ConfigEntry(Integer.parseInt(modbusAddr), meterId,
                            Long.parseLong(poolingTime));
                    configEntriesMap.put(entry.getMeterId(), entry);
                    //                    System.out.println("[modbyusAddr: "+modbusAddr+" meterId: "+meterId+" poolingTime: "+poolingTime+"]"); // DEBUG
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (ParseException e) {
            System.out
                    .println("Error: There is a problem with the structure of the JSON config file: "
                            + filename);
            //            e.printStackTrace();
        }
        return settings;
    }

    @Override
    public String toString() {
        String res = "";
        for (String key : configEntriesMap.keySet()) {
            ConfigEntry val = configEntriesMap.get(key);
            res += "[modbyusAddr: " + val.getModbusAddr()   + 
                    " meterId: " + val.getMeterId()         + 
                    " poolingTime: " + val.getPoolingTime() + 
                    "]\n";
        }
        return res;
    }


    private class ConfigEntry {
        private int modbusAddr;
        private String meterId;
        private long poolingTime;

        public ConfigEntry(int modbusAddr, String meterId, long poolingTime) {
            this.modbusAddr = modbusAddr;
            this.meterId = meterId;
            this.poolingTime = poolingTime;
        }

        public int getModbusAddr() {
            return modbusAddr;
        }

        public String getMeterId() {
            return meterId;
        }

        public long getPoolingTime() {
            return poolingTime;
        }
    }
    /*
    public static void main(String[] args) {
        ConfigFile a = new ConfigFile();
        a.readConfigFile();
    
       Map<String,Long> pollerSettings = a.getPollerSettings();
       Map<DatapointAddress,DatapointMetadata> modbusSettings = a.getModbusDriverSettings();

       System.out.println("ConfigFile Dump:\n"+a.toString());
       
       String res = "";
       System.out.println("PollerSetting Dump:\n"+res);
       for(String s : pollerSettings.keySet()){
           res += "DatapointAddress: "+s+" PollingTime: "+pollerSettings.get(s)+"\n";
       }
       System.out.println(res);
       res = ""; 

       System.out.println("\n\nModbusSetting Dump:"+res);       
       for(DatapointAddress da : modbusSettings.keySet()){
           String s = da.getAddress(); 
           res += "DatapointAddress: "+s+" PollingTime: "+modbusSettings.get(da).getCurrentSamplingInterval()+" ModbusAddr: "+modbusSettings.get(da).getReadDatapointAddress()+"\n";
       }
       System.out.println(res);
    }*/
}



/* ==========================================================================================

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
*/