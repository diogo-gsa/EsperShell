package Datastream;

/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 * DeviceReadingEvent.java class of events that will feed the Esper Engine
 * with the readings from the DeviceConnectivityService.
 */

public class Measure{

    private String id;
    private long ts;    
    private double value;    
    
    public Measure(String id, long ts, double value) {
        this.id = id;
        this.ts = ts;
        this.value = value;        
    }
    
    public double getValue() {
        return value;
    }
    
    public String getId() {
        return id;
    }

    public long getTs() {   
        return ts;
    }
    
    public String toString(){        
        return "[id:"+id+" | value:"+value+" | ts:"+ts+"]";
    }

}
