package eu.luminis.devconrob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A base class for things that sense.
 * 
 * A single thread is created to interact with the sensor at a scheduled
 * interval in milliseconds.
 *
 * @param <T>
 *            the type of what the sensor senses.
 */
public abstract class Sensor<T> {

    /**
     * An inner class to store the callback and its filter.
     */
    private class Callback {

        public Callback(Predicate<T> filter, Consumer<T> callback) {
            this.filter = filter;
            this.callbackFunction = callback;
        }

        private Predicate<T> filter;
        private Consumer<T> callbackFunction;
    }

    private List<Callback> callbacks = new ArrayList<>();

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setPriority(Thread.MAX_PRIORITY);
            return t;
        }
    });

    private T sensorData;
    private long senseIntervalInMilliseconds;
    private ScheduledFuture<?> scheduler;

    /**
     * An inner class that allows scheduled sensing and calls back in a separate
     * thread.
     */
    private class SenseTask implements Runnable {

        @Override
        public void run() {
            try {
                sensorData = sense();

                if (sensorData == null) {
                    return;
                }

                // denoise
                if (buffer != null) {
                    buffer.add(sensorData);
                    if (buffer.filled()) {
                        sensorData = denoiseFunction.apply(buffer.elements());
                    } else {
                        return; /* skip */
                    }
                }
                // present the data to all callbacks
                callbacks.forEach(c -> {
                    if (c.filter.test(sensorData)) {
                        c.callbackFunction.accept(sensorData);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Start sensing at the given interval.
     * 
     * @param intervalInMilliseconds
     *            the interval in milliseconds
     */
    public void start(long intervalInMilliseconds) {
        this.senseIntervalInMilliseconds = intervalInMilliseconds;

        scheduler = executorService.scheduleAtFixedRate(new SenseTask(), 0, intervalInMilliseconds,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Start sensing continuously.
     */
    public void start() {
        scheduler = executorService.scheduleWithFixedDelay(new SenseTask(), 0, 50, TimeUnit.MILLISECONDS);
    }

    public void pauze() {
        if (scheduler != null && !scheduler.isDone()) {
            scheduler.cancel(true);
        }
    }

    public void resume() {
        if (senseIntervalInMilliseconds > 0) {
            scheduler = executorService.scheduleAtFixedRate(new SenseTask(), 0, senseIntervalInMilliseconds,
                    TimeUnit.MILLISECONDS);
        }
    }

    /**
     * A 'template method' that concrete subclasses must implement to sense
     * something.
     * 
     * @return
     * @throws IOException
     */
    abstract public T sense() throws IOException;

    public void shutdown() throws IOException {
        executorService.shutdownNow();
    }

    /**
     * Adds a callback to this sensor.
     * 
     * @param filter
     * @param callback
     */
    public void addSensorCallback(Predicate<T> filter, Consumer<T> callback) {
        callbacks.add(new Callback(filter, callback));
    }

    private RingBuffer<T> buffer;
    private Function<List<T>, T> denoiseFunction;

    /**
     * Enables noise reduction.
     * 
     * @param bufferSize
     *            the size of the buffer
     * @param denoiseFunction
     *            the function that reduces the buffer to a single value of T
     */
    public void enableNoiseReduction(int bufferSize, Function<List<T>, T> denoiseFunction) {
        this.buffer = new RingBuffer<>(bufferSize);
        this.denoiseFunction = denoiseFunction;
    }

}