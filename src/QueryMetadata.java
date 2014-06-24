import java.sql.Statement;

/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 * 
 */

public class QueryMetadata {

    private int queryID;
    private String queryStatement;
    private boolean queryIsActivated;
    
    public QueryMetadata(int queryID, String queryStatement, boolean queryIsActivated){
        this.queryID = queryID;
        this.queryStatement = queryStatement;
        this.queryIsActivated = queryIsActivated;
    }

    public int getQueryID() {
        return queryID;
    }

    public String getQueryStatement() {
        return queryStatement;
    }

    public boolean isQueryIsActivated() {
        return queryIsActivated;
    }

    public void setQueryIsActivated(boolean queryIsActivated) {
        this.queryIsActivated = queryIsActivated;
    }

    public String toString(){
        String res =    "QueryID: " + queryID + "\t IsActiveted: \t" + queryIsActivated + "\n" +
                        "Statement:\n"+ queryStatement;
        return res;
    }
}
