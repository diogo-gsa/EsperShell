
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

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

    private void installQuery(String eplQuery, String queryId){
        query = engineAdmin.createEPL(eplQuery);
        QueryListener listener = new QueryListener(queryId);
        query.addListener(listener);
        countInitializedQueries++;        
    }

/*    
    public void installSortEnergyStreamsQuery(){
        
        String eplQueryExpression = 
                "SELECT id, ts, value "
              + "FROM dataAcquisition.DeviceReadingEvent.win:length_batch(8) " //TODO dataAcquisition.DeviceReadingEvent, change 8 to 9 
              + "OUTPUT snapshot every 1 events "
              + "ORDER BY value desc";
        
        installQuery(eplQueryExpression, "Q1");
    }
*/  
          
}
