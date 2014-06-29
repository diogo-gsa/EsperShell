import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
    BlockingQueue<String> metersReadyToBeReaded;
    
    
    public ModbusDriver(){
        master = new ModbusMasterLib();
        poller = new Poller();
        metersReadyToBeReaded = new LinkedBlockingQueue<String>();
        configPoller();
        (new PollerReaderThread()).start();
        (new ReadModbusmasterThread()).start();
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
    
    private class ReadModbusmasterThread extends Thread{
        public ReadModbusmasterThread() {
            super("READ_MODBUSMASTER_THREAD");
        }
        @Override
        public void run() {
            while(true){
                try {
                    String meterToRead = metersReadyToBeReaded.take();
                    //TODO codigo que  lê o meter:meterToRead
                    //TODO codigo que envia isto para o ESPER
                    //System.out.println("MeterToRead: "+meterToRead); //TODO  DEBUG
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private class PollerReaderThread extends Thread{
        public PollerReaderThread() {
            super("POLLER_READER_THREAD");
        }
        @Override
        public void run() {
            while(true){
                String value = poller.getNext();
                try {
                    if(value != null){
                        metersReadyToBeReaded.put(value);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    //-----------------------------------------------------------------------
//    private class TestThread implements Runnable{
//
//        @Override
//        public void run() {
//            for (int i = 1; i <= 20; i++) {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                String next = getAllAvailable(poller);
//                if (next != null) {
//                    System.out.println("..."+i+" Received: " + next);
//                } else {
//                    System.out.println("..."+i);
//                }
//            }
//        }
//        
//        private  String getAllAvailable(Poller poller){
//            String res = null;
//            while(true){
//                String next = poller.getNext(); 
//                if(next == null){
//                    return res;
//                }else{
//                    if(res == null){
//                        res = next;
//                    }else{
//                        res = res +","+ next; 
//                    }
//                }
//            }
//        }
//    } 
    
    
}
