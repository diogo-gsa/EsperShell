
public class QueryMetadata {

    private String queryID;
    private String queryStatement;
    private boolean queryIsActivated;
    
    public QueryMetadata(String queryID, String queryStatement, boolean queryIsActivated){
        this.queryID = queryID;
        this.queryStatement = queryStatement;
        this.queryIsActivated = queryIsActivated;
    }

    public String getQueryID() {
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
    
}
