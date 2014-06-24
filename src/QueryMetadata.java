import java.sql.Statement;

import com.espertech.esper.client.EPStatement;

/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 * 
 */

public class QueryMetadata {

    private int queryID;
    private String queryExpression;
    private EPStatement queryEngineObject; 
    
    public QueryMetadata(int queryID, String queryStatement, EPStatement queryEngineObject){
        this.queryID = queryID;
        this.queryExpression = queryStatement;
        this.queryEngineObject = queryEngineObject;
    }

    public int getQueryID() {
        return queryID;
    }

    public String getQueryStatement() {
        return queryExpression;
    }

    public boolean queryIsActivated() {
        return queryEngineObject.isStarted();
    }

    public void turnOnQuery(){
        queryEngineObject.start();        
    }
    
    public void turnOffQuery(){
        queryEngineObject.stop();
    }
    
    public void destroyQuery(){
        queryEngineObject.destroy();
    }

    public String toString(){
        String res =    "QueryID: " + queryID + "\t IsActiveted: " + queryIsActivated() + "\n" +
                        "Statement:\n"+ queryExpression;
        return res;
    }
}
