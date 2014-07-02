package datastorm.espershell.dataacquisition;

import ist.smartoffice.datapointconnectivity.DatapointAddress;
import ist.smartoffice.datapointconnectivity.DatapointMetadata;
import ist.smartoffice.datapointconnectivity.DatapointValue;
import ist.smartoffice.datapointconnectivity.IDatapointConnectivityService;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.crypto.Data;

import modbus.master.ModbusMasterLib;
import modbus.master.exception.ModbusCommunicationException;
import modbus.master.exception.ModbusConnectionException;
import modbus.master.exception.ModbusResponseException;


/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 */

public class ModbusDriver
        implements IDatapointConnectivityService {

    ModbusMasterLib master;
    int modbusOffsetRegisters; // first modbus packet that we want to read (e.g. 1)
    int modbusLengthRegisters; // number of modbus packet registers that we want to read (3 = tri-phase consumption)

    Poller poller;
    BlockingQueue<String> metersReadyToBeReaded; //receive events sent by poller

    //Atributes related with IDatapointConnectivityService methods implementation 
    private Set<DatapointListener> listeners;
    private Map<DatapointAddress, DatapointMetadata> datapoints;
    private ConfigFile configFile;
    


    public ModbusDriver() {
        master = new ModbusMasterLib();
        configModbusMaster("127.0.0.1", 1, 3);

        configFile = new ConfigFile();
        configFile.readConfigFile("modbusDriver.config");
        
        metersReadyToBeReaded = new LinkedBlockingQueue<String>();
       
        listeners = new HashSet<DatapointListener>();
        datapoints = configFile.getModbusDriverSettings();

        poller = new Poller();
        poller.configPoller(configFile);
        poller.start();

        (new PollerReaderThread()).start();
        (new ReadModbusmasterThread()).start();
    }

    private void configModbusMaster(String address, int offset, int length) {
        try {
            this.modbusOffsetRegisters = offset;
            this.modbusLengthRegisters = length;
            master.createModbusTcpMaster(address);
        } catch (ModbusConnectionException e) {
            System.out.println("Error connecting with modbus slave in the address " + address);
            e.printStackTrace();
        }
    }



    private short[] readEnergyMeter(int slaveId) {
        /*//TODO codigo correcto------------------------------------------------------------------------------------
        try {
            return master.readInputRegisters(slaveId, modbusOffsetRegisters, modbusLengthRegisters);
        } catch (ModbusResponseException | ModbusCommunicationException e) {
            System.out.println("Error: Communication between modbus master and slave failed");
            return null;
        }*/
        
      //TODO codigo de teste "stub"
//        System.out.println("ReadedSlaveID: "+slaveId);
        short[] res = {(short) (1+slaveId), (short) (2+slaveId),(short) (3+slaveId)};
        return res;
    }
    
    private class ReadModbusmasterThread extends
            Thread {
        public ReadModbusmasterThread() {
            super("READ_MODBUSMASTER_THREAD");
        }

        @Override
        public void run() {
            while (true) {
                try {
                    String meterToReadToken = metersReadyToBeReaded.take(); // e.g. PavCivil
                    DatapointMetadata dpToReadMD = null;
                    DatapointAddress dpToReadAddr = null;
                    for(DatapointAddress da : datapoints.keySet()){
                        if(da.getAddress().equals(meterToReadToken)){
                            dpToReadMD = datapoints.get(da);
                            dpToReadAddr = da;
                            break;
                        }
                    }
                    String meterReadingAddr = dpToReadMD.getReadDatapointAddress();
                    short[] results = readEnergyMeter(Integer.parseInt( meterReadingAddr));
                    DatapointValue[] resultsToSend = new DatapointValue[results.length];
                    for(int i = 0; i < resultsToSend.length; i++){
                        resultsToSend[i] = new DatapointValue(results[i]+"");
                    }
                    notifyDatapointUpdate(dpToReadAddr,resultsToSend);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class PollerReaderThread extends
            Thread {
        public PollerReaderThread() {
            super("POLLER_READER_THREAD");
        }

        @Override
        public void run() {
            while (true) {
                String value = poller.getNext();
                try {
                    if (value != null) {
                        //System.out.println(value); //DEBUG
                        metersReadyToBeReaded.put(value);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // IDatapointConnectivityService Interface method's implementation

    @Override
    public String getImplementationName() {
        return "DatapointConnectivityServiceModbusDriver";
    }

    @Override
    public void addDatapointListener(DatapointListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }

    }

    @Override
    public DatapointAddress[] getAllDatapoints() {
        DatapointAddress[] result = new DatapointAddress[datapoints.size()];
        Iterator<DatapointAddress> it = datapoints.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            DatapointAddress datapointAddress = (DatapointAddress) it.next();
            result[i++] = datapointAddress;
        }
        return result;
    }

    @Override
    public DatapointMetadata getDatapointMetadata(DatapointAddress address) throws OperationFailedException {
        return datapoints.get(address);
    }

    @Override
    public void removeDatapointListener(DatapointListener listener) {
        listeners.remove(listener);
    }

    @Override
    public int requestDatapointRead(DatapointAddress address, ReadCallback readCallback) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int requestDatapointWindowRead(DatapointAddress address,
                                          long startTimestamp,
                                          long finishTimestamp,
                                          ReadCallback readCallback) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int requestDatapointWrite(DatapointAddress address,
                                     DatapointValue[] values,
                                     WriteCallback writeCallback) {
        // TODO Sensores modbus nao suportam escrita. este metodo nao se implementa certo?
        return 0;
    }

    private void notifyDatapointUpdate(DatapointAddress address, DatapointValue[] values) {
        synchronized (listeners) {
            Iterator<DatapointListener> it = listeners.iterator();
            while (it.hasNext()) {
                DatapointListener listener = it.next();
                listener.onDatapointUpdate(address, values);
            }
        }
    }


}
