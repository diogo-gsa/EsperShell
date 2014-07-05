package Datastream;

/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 * DeviceReadingEvent.java class of events that will feed the Esper Engine
 * with the readings from the DeviceConnectivityService.
 */

public class Measure{

    private String meterId; 
    private double measure;
//  private long timestamp; 
    
    public Measure(String id,/* long ts,*/ double measure) {
        this.meterId = id;
        this.measure = measure;        
//      this.timestamp = ts;
    }
    
    public double getMeasure() {
        return measure;
    }
    
    public String getMeterId() {
        return meterId;
    }

//    public long getTimestamp() {   
//        return timestamp;
//    }
    
    public String toString(){        
        return "[meterId: "+meterId/*" | timestamp:"+timestamp+ "*/+" | measure: "+measure+"]";
    }

}
