
import java.util.Map;
import java.util.TreeMap;

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

public class EsperEngine {

    EPServiceProvider   esperEngine;
    EPRuntime           engineRuntime;
    EPAdministrator     engineAdmin;

    Map<Integer,QueryMetadata> queryCatalog; 
    int countInitializedQueries;
    
    
    public EsperEngine(){
        esperEngine = EPServiceProviderManager.getDefaultProvider();
        engineRuntime = esperEngine.getEPRuntime();
        engineAdmin = esperEngine.getEPAdministrator();

        queryCatalog = new TreeMap<Integer,QueryMetadata>(); 
        countInitializedQueries = 0;
        
    }
       
    public void push(DeviceReadingEvent event){
        if(countInitializedQueries == 0){
            System.out.println("*** There is no initialized queries at the engine ***");
        }
            
        System.out.println("Input:\t"+event);        
        engineRuntime.sendEvent(event);
    }
    
    public QueryMetadata installQuery(String eplQueryExpression) throws EPStatementException {
        
        //install query
        //EPStatement = representa a query enquanto objecto dentro do engine
        EPStatement queryEngineObject = engineAdmin.createEPL(eplQueryExpression);
        
        //get queryID
        countInitializedQueries++;        
        
        //create new listener
        QueryListener listener = new QueryListener(countInitializedQueries);
        queryEngineObject.addListener(listener);
            
        QueryMetadata qmd = new QueryMetadata(countInitializedQueries, eplQueryExpression, queryEngineObject);
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
    
    public void listInstalledQueries(){
        System.out.println("========== Installed Queries ("+queryCatalog.keySet().size()+") ==========");
        for(int queryId : queryCatalog.keySet()){
            System.out.println(queryCatalog.get(queryId));
            System.out.println("-------------------------------------------");            
        }        
    }


}
