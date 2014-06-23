
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 * 
 */

public class QueryListener implements UpdateListener {
    
    private int queryId;
    
    public QueryListener(int queryId){
        this.queryId = queryId;
    }
    
    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        try{
            System.out.print("Q_ID"+queryId+" OUTput NEWevents:\t");            
            for(EventBean eb : newEvents){
                System.out.print("\n| "+eb.getUnderlying());
            }
            if(oldEvents != null){
                System.out.print("\n"+"Q_ID"+queryId+" OUTput OLDevents:\t");            
                for(EventBean eb : oldEvents){
                    System.out.print("| "+eb.getUnderlying());
                }
            }
         System.out.println(">>>\n");
        }catch (Exception e) {
          e.printStackTrace();
        }
    }
}
