package datastorm.espershell.esperengine;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 */

public class QueryListener
        implements UpdateListener {

    private QueryMetadata qMD;
    private final int __OUTPUT_MONITOR_SERVER_PORT__ = 62491;
    private MonitorClient outputMonitor = new MonitorClient(__OUTPUT_MONITOR_SERVER_PORT__);
    
    
    public QueryListener(QueryMetadata metadata) {
        qMD = metadata;
        initFile();
    
    }

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        if (newEvents != null) {
            if(qMD.getPrintToTerminal()){
                printToOutputMonitor(newEvents,"NEW");
                //printOutputToTerminal(newEvents, "NEW");
            }
            if(qMD.getPrintToFile()){
                printOutputToFile(newEvents, "NEW");
            }
        }
        if (oldEvents != null) {
            if(qMD.getPrintToTerminal()){
                printToOutputMonitor(oldEvents,"OLD");
                //printOutputToTerminal(newEvents, "OLD");
            }
            if(qMD.getPrintToFile()){
                printOutputToFile(newEvents, "OLD");
            }
        }
    }
    
    private void printToOutputMonitor(EventBean[] events, String typeOfEvent){
        String res = "";
        res += "Query " + qMD.getQueryID() + " OUTPUT " + typeOfEvent + " Events:";
        for (EventBean eb : events) {
            res += "\n| " + eb.getUnderlying() + "\n";
        }
        
        try {
            outputMonitor.sendInfoToMonitor(res);
        } catch (IOException e) {
            System.out.println("Impossible to connect to 'Output datastream monitor'");
        }
    }   

//    private void printOutputToTerminal(EventBean[] events, String typeOfEvent) {
//        try {
//            System.out.print("Query " + qMD.getQueryID() + " OUTPUT " + typeOfEvent + " Events:");
//            for (EventBean eb : events) {
//                System.out.print("\n| " + eb.getUnderlying() + "\n");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    
    private void printOutputToFile(EventBean[] events, String typeOfEvent) {
        String res="";
        try {
            res += "Query " + qMD.getQueryID() + " OUTPUT " + typeOfEvent + " Events:";
            for (EventBean eb : events) {
                res = res + "\n| " + eb.getUnderlying() + "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            RandomAccessFile file = new RandomAccessFile(qMD.getOutputFilename(), "rws");
            byte[] text = new byte[(int) file.length()];
            file.readFully(text);
            file.seek((int) file.length());
            file.writeBytes(res + "\n");
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void initFile() {
        try {
            File f = new File(qMD.getOutputFilename());
            f.delete();
            RandomAccessFile file = new RandomAccessFile(qMD.getOutputFilename(), "rws");
            byte[] text = new byte[(int) file.length()];
            file.readFully(text);
            file.seek((int) file.length());
            file.writeBytes(qMD.getQueryStatement());
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
}
