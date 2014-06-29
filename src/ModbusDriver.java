import java.util.Map;

import modbus.master.ModbusMasterLib;


/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 * 
 */

public class ModbusDriver /* implements DeviceAPI */ {

    ModbusMasterLib master;
    
    // Metadata to configure master to read a given datapoint
    int slaveId;        // energyMeterId (e.g. 2 = library meter)
    int offsetRegister; // first modbus packet that we want to read (e.g. 1)
    int lengthRegisters;// number of modbus packet registers that we want to read (3 = tri-phase consumption)
    
    Poller poller;
    
    
    public ModbusDriver(){
        master = new ModbusMasterLib();
        poller = new Poller();
        poller.start();
        configPoller();
    }
    
    //configure ModbusMaster to read a given energy meter
    public void configureEnergyMeter(String slaveAddress, int slaveId, int offsetRegister, int lengthRegisters){
        master.createModbusTcpMaster(slaveAddress); // slave address (e.g. 127.0.0.1)
        this.slaveId = slaveId;
        this.offsetRegister = offsetRegister;
        this.lengthRegisters = lengthRegisters;
        
    }
    
    public short[] readEnergyMeter(){
        return master.readInputRegisters(slaveId, offsetRegister, lengthRegisters);
    }
    
    private void configPoller(){
        Map<String,Long> pollerSettings = (new ConfigFile()).getSettings(); 
        for(String deviceID : pollerSettings.keySet()){
            poller.addAddress(deviceID, pollerSettings.get(deviceID));
        }
    }
    
}
