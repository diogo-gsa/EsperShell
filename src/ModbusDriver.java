import modbus.master.ModbusMasterLib;


/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 * 
 */

public class ModbusDriver /* implements DeviceAPI */ {

//    ModbusDriverImplementation worker;
    ModbusMasterLib master;
    
    // Metadata to configure master to read a given datapoint
    int slaveId;        // energyMeterId (e.g. 2 = library meter)
    int offsetRegister; // first modbus packet that we want to read (e.g. 1)
    int lengthRegisters;// number of modbus packet registers that we want to read (3 = tri-phase consumption)

    public ModbusDriver(){
        master = new ModbusMasterLib();
        
//        worker = new ModbusDriverImplementation();
//        worker.start();
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
    
    
    /*
    private class ModbusDriverImplementation implements Runnable{
       
       public void start(){
           Thread thr = new Thread(this);
           thr.run();
       }
       
        @Override
        public void run() {
            // TODO Auto-generated method stub
            
        }
        
    }*/
}
