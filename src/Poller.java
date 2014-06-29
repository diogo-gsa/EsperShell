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

    private Map<String, Delayed> delayedEventsMap;
    private DelayQueue<Delayed> delayedEventsQueue;
    private Queue<String> readyToUseEventsQueue;
    private boolean isStarted;

    public Poller() {
        delayedEventsQueue = new DelayQueue<>();
        readyToUseEventsQueue = new LinkedList<>();
        delayedEventsMap = new TreeMap<String, Delayed>();
        isStarted = true;
        (new ExpiredEventsPollerThread()).start();
    }

    public void start() {
        isStarted = true;
    }

    public void stop() {
        isStarted = false;
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
        public ExpiredEventsPollerThread() {
            super("EXPIRED_EVENTS_POLLER_THREAD");
        }
        
        @Override
        public void run() {
            while (true) {
                synchronized (Poller.this) {
                    DelayedEvent element = (DelayedEvent) delayedEventsQueue.poll();
                    if (element != null) {
                        if (isStarted) {
                            readyToUseEventsQueue.add(element.getMessage());
                        }
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
