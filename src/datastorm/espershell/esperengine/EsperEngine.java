package datastorm.espershell.esperengine;

import ist.smartoffice.datapointconnectivity.DatapointAddress;
import ist.smartoffice.datapointconnectivity.DatapointValue;
import ist.smartoffice.datapointconnectivity.IDatapointConnectivityService;
import ist.smartoffice.datapointconnectivity.IDatapointConnectivityService.ErrorType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import Datastream.Measure;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EPStatementException;

/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 * 
 */

public class EsperEngine implements IDatapointConnectivityService.DatapointListener {

    EPServiceProvider   esperEngine;
    EPRuntime           engineRuntime;
    EPAdministrator     engineAdmin;
    
    Map<Integer,QueryMetadata> queryCatalog; 
    int countInitializedQueries;
    private boolean showInput;
    private final int __INPUT_MONITOR_SERVER_PORT__  = 62490;
    private MonitorClient inputMonitor  = new MonitorClient(__INPUT_MONITOR_SERVER_PORT__);
    
     
    
    public EsperEngine(){
        esperEngine = EPServiceProviderManager.getDefaultProvider();
        engineRuntime = esperEngine.getEPRuntime();
        engineAdmin = esperEngine.getEPAdministrator();
        showInput = true; // DEFAULT VALUE
        
        queryCatalog = new TreeMap<Integer,QueryMetadata>(); 
        countInitializedQueries = 0;
        
    }
       
    public void push(Measure event){        
//        if(showInput){
//            System.out.println("Input:\t"+event);
//            if(countInitializedQueries == 0){
//                System.out.println("*** There is no initialized queries at the engine ***");
//            }
//        }
        
        try{
            if(showInput){
                inputMonitor.sendInfoToMonitor(event.toString());
            }
        }catch (Exception e) {
            System.out.println("Impossible to connect to 'Input datastream monitor'");
        }
       
        engineRuntime.sendEvent(event);
    }
    
    public QueryMetadata installQuery(String eplQueryExpression) throws EPStatementException {
        
        //install query
        //EPStatement = representa a query enquanto objecto dentro do engine
        EPStatement queryEngineObject = engineAdmin.createEPL(eplQueryExpression);
        
        //get queryID
        countInitializedQueries++;        

        QueryMetadata qmd = new QueryMetadata(countInitializedQueries, eplQueryExpression, queryEngineObject);
        
        //create new listener
        QueryListener listener = new QueryListener(qmd);
        queryEngineObject.addListener(listener);
            
        queryCatalog.put(qmd.getQueryID(), qmd);
        
        return qmd;
    }          

    public boolean turnOnQuery(int queryID){
        try{
            queryCatalog.get(queryID).turnOnQuery();
            return true;
        }catch(NullPointerException | ClassCastException e){
            System.out.println("Error: Query with the id="+queryID+" does not exist");  
            return false;
        }
    }
    
    public boolean turnOffQuery(int queryID){
        try{ 
            queryCatalog.get(queryID).turnOffQuery();
            return true;
        }catch(NullPointerException | ClassCastException e){
            System.out.println("Error: Query with the id="+queryID+" does not exist");
            return false;            
        }
    }

    public boolean dontPrintToTerminal(int queryID) {
        try{ 
            queryCatalog.get(queryID).dontPrintToTerminal();
            return true;
        }catch(NullPointerException | ClassCastException e){
            System.out.println("Error: Query with the id="+queryID+" does not exist");
            return false;            
        }
    }
    
    public boolean printToTerminal(int queryID) {
        try{ 
            queryCatalog.get(queryID).printToTerminal();
            return true;
        }catch(NullPointerException | ClassCastException e){
            System.out.println("Error: Query with the id="+queryID+" does not exist");
            return false;            
        }
    }
    
    public boolean printToFile(int queryID) {
        try{ 
            queryCatalog.get(queryID).printToFile();
            return true;
        }catch(NullPointerException | ClassCastException e){
            System.out.println("Error: Query with the id="+queryID+" does not exist");
            return false;            
        }
    }
    
    public boolean dontPrintToFile(int queryID) {
        try{ 
            queryCatalog.get(queryID).dontPrintToFile();
            return true;
        }catch(NullPointerException | ClassCastException e){
            System.out.println("Error: Query with the id="+queryID+" does not exist");
            return false;            
        }
    }
    
    
    public boolean dropQuery(int queryID){
        try{ 
            queryCatalog.get(queryID).destroyQuery();
            queryCatalog.remove(queryID);
            return true;
        }catch(NullPointerException | ClassCastException e){
            System.out.println("Error: Query with the id="+queryID+" does not exist");
            return false;            
        }
    }
    
    public int dropAllQueries(){
        int droppedQueries = 0;
        try{           
            //to avoid ConcurrentModificationException
            List<Integer>  keyset = new ArrayList<Integer>(queryCatalog.keySet());
            for(int queryID : keyset){
                queryCatalog.get(queryID).destroyQuery();
                queryCatalog.remove(queryID);
                droppedQueries++;
            }
        }catch(Exception e){
            System.out.println("Error: something went wrong during dropAll");
        }
        return droppedQueries;
    }

    public void listInstalledQueries(){
        System.out.println("========== Installed Queries ("+queryCatalog.keySet().size()+") ==========");
        for(int queryId : queryCatalog.keySet()){
            System.out.println(queryCatalog.get(queryId));
            System.out.println("-------------------------------------------");            
        }        
    }

    public void setShowInput(boolean state){
        showInput = state;
    }
    
    @Override
    public void onDatapointUpdate(DatapointAddress address, DatapointValue[] values) {
        String meterId = address.getAddress();
        double measure = 0;
//        long ts = 0;
        for(DatapointValue dv : values){
            measure = measure + Double.parseDouble(dv.getValue());
//            ts = dv.getTimestamp();
        }
        push(new Measure(meterId,/* ts,*/ measure));
    }

    @Override
    public void onDatapointError(DatapointAddress address, ErrorType error) {
        System.out.println("Error "+error+" found with datapoint "+address);
    }

    @Override
    public void onDatapointAddressListChanged(DatapointAddress[] address) {
        System.out.println("Datapoint address list changed for:\n");
        for(DatapointAddress da : address){
            System.out.println(""+da.getAddress());
        }
    }

}
