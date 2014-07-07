package datastorm.espershell.esperengine;
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
    private boolean printToTerminal;
    private boolean printToFile;
    private String outputFilename;
    
    public QueryMetadata(int queryID, String queryStatement, EPStatement queryEngineObject){
        this.queryID = queryID;
        this.queryExpression = queryStatement;
        this.queryEngineObject = queryEngineObject;
        this.printToTerminal = true; //default behavior 
        this.printToFile = true;    //default behavior
        //escrever resultado na folder: queriesOutput
        this.outputFilename = "../queriesOutput/"+queryID +"_output.txt";
    }

    
    public void printToTerminal(){
        printToTerminal = true;
    }
    
    public void dontPrintToTerminal(){
        printToTerminal = false;
    }
    
    public void printToFile(){
        printToFile = true;
    }
    
    public void dontPrintToFile(){
        printToFile = false;
    }
    
    public boolean getPrintToTerminal(){
        return printToTerminal;
    }
    
    public boolean getPrintToFile(){
        return printToFile;
    }
    
    public String getOutputFilename(){
        return outputFilename;
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
        String printToFile = getPrintToFile() + "";
        
        if(getPrintToFile()){
            printToFile += " ("+getOutputFilename()+")";
        }
        
        String res =    "QueryID: " + queryID + 
                        "\t\t IsActiveted: " + queryIsActivated() + "\n"+ 
                        "PrintToTerminal: " + getPrintToTerminal() + 
                        "\t PrintToFile: " + printToFile + "\n"  +
                        "Statement:\n"+ queryExpression;
        return res;
    }
}
