package datastorm.espershell.esperengine;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 */

public class QueryListener
        implements UpdateListener {

    private QueryMetadata qMD;

    public QueryListener(QueryMetadata metadata) {
        qMD = metadata;
    }

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        if (newEvents != null) {
            if(qMD.getPrintToTerminal()){
                printOutputToTerminal(newEvents, "NEW");
            }
        }
        if (oldEvents != null) {
            if(qMD.getPrintToTerminal()){
                printOutputToTerminal(newEvents, "OLD");
            }
        }
    }

    private void printOutputToTerminal(EventBean[] events, String typeOfEvent) {
        try {
            System.out.print("Query " + qMD.getQueryID() + " OUTPUT " + typeOfEvent + " Events:");
            for (EventBean eb : events) {
                System.out.print("\n| " + eb.getUnderlying() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printOutputToFile(){
        //TODO implement this
    }
}
