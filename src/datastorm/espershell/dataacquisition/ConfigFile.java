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
        BufferedReader br = null;
        String configString = null;
        StringBuilder sb = new StringBuilder();
 
        try {
//            System.out.println("[DEBUG]Looking for file here: "+ System.getProperty("user.dir")); //TODO DEBUG 
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
            System.out.println("Looking for file here: "+ System.getProperty("user.dir"));
            //e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        System.out.println(configString); //TODO DEBUG
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
                    String poolingTime = (String) obj.get("PollingTimeMillis");
                    ConfigEntry entry = new ConfigEntry(Integer.parseInt(modbusAddr), meterId,
                            Long.parseLong(poolingTime));
                    configEntriesMap.put(entry.getMeterId(), entry);
                    // System.out.println("[modbyusAddr: "+modbusAddr+" meterId: "+meterId+" poolingTime: "+poolingTime+"]"); // DEBUG
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
}
