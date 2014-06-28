import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;


public class Poller {


    private Map<String, Delayed> mapWithQueueDelayedEvents;
    private DelayQueue<Delayed> delayQueue;
    private Queue<String> headQueue;
    private boolean isStarted;

    public Poller() {
        delayQueue = new DelayQueue<>();
        headQueue = new LinkedList<>();
        mapWithQueueDelayedEvents = new TreeMap<String, Delayed>();
        isStarted = true;
        Thread thrExpiredEventsHandler = new Thread(new ExpiredEventsHandler());
        thrExpiredEventsHandler.start();
    }

    public void start() {
        isStarted = true;
    }

    public void stop() {
        isStarted = false;
    }

    public synchronized void addAddress(String message, long milisecondsDelay) {
        Delayed newDelayedEvent = new DelayedEvent(message, milisecondsDelay);

        if (!mapWithQueueDelayedEvents.containsKey(message)) {
            mapWithQueueDelayedEvents.put(message, newDelayedEvent);
            delayQueue.add(newDelayedEvent);
        } else {
            Delayed oldDelayedEvent = mapWithQueueDelayedEvents.get(message);
            mapWithQueueDelayedEvents.remove(message);
            mapWithQueueDelayedEvents.put(message, newDelayedEvent);
            delayQueue.remove(oldDelayedEvent);
            delayQueue.add(newDelayedEvent);
        }
    }

    public synchronized void removeAddress(String message) {
        if (mapWithQueueDelayedEvents.containsKey(message)) {
            Delayed de = mapWithQueueDelayedEvents.get(message);
            mapWithQueueDelayedEvents.remove(message);
            delayQueue.remove(de);
        }
    }


    public synchronized String getNext() {
        return headQueue.poll();
    }


    private class ExpiredEventsHandler
            implements Runnable {
        @Override
        public void run() {
            while (true) {
                synchronized (Poller.this) {
                    DelayedEvent element = (DelayedEvent) delayQueue.poll();
                    if (element != null) {
                        if (isStarted) {
                            headQueue.add(element.getMessage());
                        }
                        // reschedule
                        addAddress(element.getMessage(), element.getInitialExpirationDelay());
                    }
                }
            }
        }
    }


    // inner class
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
