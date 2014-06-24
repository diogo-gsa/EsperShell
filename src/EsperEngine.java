
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
    EPStatement         query;
    int                 countInitializedQueries;
    
    public EsperEngine(){
        esperEngine = EPServiceProviderManager.getDefaultProvider();
        engineRuntime = esperEngine.getEPRuntime();
        engineAdmin = esperEngine.getEPAdministrator();
        countInitializedQueries = 0;
    }
       
    public void push(DeviceReadingEvent event){
        if(countInitializedQueries == 0){
            System.out.println("*** There is no initialized queries at the engine ***");
        }
            
        System.out.println("Input:\t"+event);        
        engineRuntime.sendEvent(event);
    }
    
    public QueryMetadata installQuery(String eplQuery) throws EPStatementException {
        
        //install query
        query = engineAdmin.createEPL(eplQuery);
        
        //get queryID
        countInitializedQueries++;        
        
        //create new listener
        QueryListener listener = new QueryListener(countInitializedQueries);
        query.addListener(listener);
            
        return new QueryMetadata(countInitializedQueries, eplQuery, true);
    }          
}
