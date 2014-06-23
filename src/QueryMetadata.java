import java.sql.Statement;


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
        String res =    "QueryID: \t" + queryID + "\n" +
                        "Statement:\t"+ queryStatement + "\n" +
                        "IsActiveted: \t" + queryID + "\n";
        return res;
    }
}
