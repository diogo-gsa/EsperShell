package datastorm.espershell.dataacquisition;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;


/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 * 
 */

public class Poller {
    private static final String POLLER_THREAD_NAME = "Poller expired events thread";
    private final Map<String, Delayed> delayedEventsMap = new TreeMap<String, Delayed>();
    private final DelayQueue<Delayed> delayedEventsQueue = new DelayQueue<>();
    private final Queue<String> readyToUseEventsQueue = new LinkedList<>();
    private final ExpiredEventsPollerThread pollerThread = new ExpiredEventsPollerThread();     
    
    public void configPoller(ConfigFile config){
        Map<String,Long> pollerSettings = config.getPollerSettings(); 
        for(String deviceID : pollerSettings.keySet()){
            addAddress(deviceID, pollerSettings.get(deviceID));
        }
    }

    public void start() {
        pollerThread.start();
    }

    public void stop() {
        pollerThread.interrupt();
    }
    
    public void destroy(){
        pollerThread.stopThread();   
    }

    public synchronized void addAddress(String message, long milisecondsDelay) {
        Delayed newDelayedEvent = new DelayedEvent(message, milisecondsDelay);

        if (!delayedEventsMap.containsKey(message)) {
            delayedEventsMap.put(message, newDelayedEvent);
            delayedEventsQueue.add(newDelayedEvent);
        } else {
            Delayed oldDelayedEvent = delayedEventsMap.get(message);
            delayedEventsMap.remove(message);
            delayedEventsMap.put(message, newDelayedEvent);
            delayedEventsQueue.remove(oldDelayedEvent);
            delayedEventsQueue.add(newDelayedEvent);
        }
    }

    public synchronized void removeAddress(String message) {
        if (delayedEventsMap.containsKey(message)) {
            Delayed de = delayedEventsMap.get(message);
            delayedEventsMap.remove(message);
            delayedEventsQueue.remove(de);
        }
    }


    public synchronized String getNext() {
        return readyToUseEventsQueue.poll();
    }
    

    /* 
     * This thread looks for new available events, spinning around delayedEventsQueue.
     * When events become available (in the delayedEventsQueue) they are sent to readyToUseEventsQueue,
     *  and then rescheduled (again) in the delayedEventsQueue.
     */
    private class ExpiredEventsPollerThread extends Thread {
        
        private boolean keepRunning;
        
        public ExpiredEventsPollerThread() {
            super(POLLER_THREAD_NAME);
            keepRunning = true;
        }
        
        public void stopThread(){
            keepRunning = false;
        }
        
        @Override
        public void run() {
            while (keepRunning) {
                synchronized (Poller.this) {
                    DelayedEvent element = (DelayedEvent) delayedEventsQueue.poll();
                    if (element != null) {
                        readyToUseEventsQueue.add(element.getMessage());
                        // reschedule the event in the delayQueue
                        addAddress(element.getMessage(), element.getInitialExpirationDelay());
                    }
                }
            }
        }
    }


    private class DelayedEvent
            implements Delayed {

        private final long expirationDelay;
        private final long expireTimeStamp;
        private final String message;


        public DelayedEvent(final String message, final long delay) {
            this.message = message;
            //expirationDelay comes in millisenconds
            this.expirationDelay = delay;
            //expireTimeStamp works in nanoseconds
            this.expireTimeStamp = System.nanoTime()
                    + TimeUnit.NANOSECONDS.convert(expirationDelay, TimeUnit.MILLISECONDS);
        }


        public long getInitialExpirationDelay() {
            return expirationDelay;
        }


        public String getMessage() {
            return message;
        }

        @Override
        public final int compareTo(final Delayed o) {
            if (this == o) {
                return 0;
            } else {
                final long thisDelay = getDelay(TimeUnit.NANOSECONDS);
                final long otherDelay = o.getDelay(TimeUnit.NANOSECONDS);

                if (thisDelay < otherDelay) {
                    return -1;
                } else if (thisDelay > otherDelay) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }

        @Override
        public final long getDelay(final TimeUnit unit) {
            final long now = System.nanoTime();
            return unit.convert(expireTimeStamp - now, TimeUnit.NANOSECONDS);
        }
    }
}
