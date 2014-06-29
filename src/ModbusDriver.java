import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import modbus.master.ModbusMasterLib;
import modbus.master.exception.ModbusCommunicationException;
import modbus.master.exception.ModbusConnectionException;
import modbus.master.exception.ModbusResponseException;


/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 * 
 */

public class ModbusDriver /* implements DeviceAPI */ {

    ModbusMasterLib master;
    int modbusOffsetRegisters;  // first modbus packet that we want to read (e.g. 1)
    int modbusLengthRegisters;  // number of modbus packet registers that we want to read (3 = tri-phase consumption)
    
    Poller poller;
    BlockingQueue<String> metersReadyToBeReaded; //receive events sent by poller
    
    
    public ModbusDriver(){
        master = new ModbusMasterLib();
        configModbusMaster("127.0.0.1",1,3);

        metersReadyToBeReaded = new LinkedBlockingQueue<String>();
        
        poller = new Poller();
        configPoller();

        (new PollerReaderThread()).start();
        (new ReadModbusmasterThread()).start();
    }
    
    private void configModbusMaster(String address, int offset, int length){
        try {
            this.modbusOffsetRegisters = offset;
            this.modbusLengthRegisters = length;
            master.createModbusTcpMaster(address);
        } catch (ModbusConnectionException e) {
            System.out.println("Error connecting with modbus slave in the address "+address);
            e.printStackTrace();
        }
    }
    
  
    
    private short[] readEnergyMeter(int slaveId){
        try {
            return master.readInputRegisters(slaveId, modbusOffsetRegisters, modbusLengthRegisters);
        } catch (ModbusResponseException | ModbusCommunicationException e) {
            //System.out.println("Error: Communication between modbus master and slave failed"); //TODO apagar isto de implementar a classe ReadModbusmasterThread
            //e.printStackTrace(); //TODO
            return null;
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
//                    System.out.println("MeterToRead: "+meterToRead); //TODO  DEBUG
                    //TODO(1) converter meterToRead:String para param:int do metodo readEnergyMeter  
                    short[] result = readEnergyMeter(17); // 17 is a stub value
                    if(result != null){
                        //TODO(2) from  result[1,2,3] compute de total amount of consumed energy
                        //TODO(3) send this value to Esper via DEVICE API
                    }
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
    
    private void configPoller(){
        Map<String,Long> pollerSettings = (new ConfigFile()).getSettings(); 
        for(String deviceID : pollerSettings.keySet()){
            poller.addAddress(deviceID, pollerSettings.get(deviceID));
        }
    }
    
    
}
