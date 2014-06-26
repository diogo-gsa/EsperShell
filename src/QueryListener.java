import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 */

public class QueryListener
        implements UpdateListener {

    private int queryId;

    public QueryListener(int queryId) {
        this.queryId = queryId;
    }

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        if (newEvents != null) {
            printOutputToTerminal(newEvents, "NEW");
        }
        if (oldEvents != null) {
            printOutputToTerminal(newEvents, "OLD");
        }
    }

    private void printOutputToTerminal(EventBean[] events, String typeOfEvent) {
        try {
            System.out.print("Query " + queryId + " OUTPUT " + typeOfEvent + " Events:");
            for (EventBean eb : events) {
                System.out.print("\n| " + eb.getUnderlying()+"\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
