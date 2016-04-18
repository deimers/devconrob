import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import eu.luminis.devconrob.DistanceSensor;
import eu.luminis.devconrob.LED;
import eu.luminis.devconrob.Motor;
import static eu.luminis.devconrob.SleepUtil.sleep;

public class MainFixedDistanceSensor {
    
    enum State {
        EXPLORING, 
        OBSTACLE_AVOIDING
    }
    
    static AtomicReference<State> currentState = new AtomicReference<State>(State.EXPLORING);
        
    static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void main(String args[]) throws IOException, InterruptedException {

        Motor motorLeft = new Motor(22, 27);
        Motor motorRight = new Motor(17, 23);

        DistanceSensor distanceSensor = new DistanceSensor(19, 16);
        LED led = new LED(21);
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutdown hook called");
                try {
                    motorLeft.shutdown();
                    motorRight.shutdown();
                    distanceSensor.shutdown();
                    led.shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        distanceSensor.enableNoiseReduction(5, values -> {
            // average of latest 3, remove min and max,
            Collections.sort(values);
            return values.subList(1, values.size() - 1).stream().mapToDouble(x -> x).average().getAsDouble(); 
        });

        Predicate<Double> filter = (Double d) -> { 
            return d != null && d < 10;
        };
        
        Consumer<Double> callback = (Double d) -> {
            
            if (currentState.compareAndSet(State.EXPLORING, State.OBSTACLE_AVOIDING)) {

                //spawn a thread, keep handling events
                executorService.execute(() -> {
                    led.blink(200, 3);
                    
                    //back of
                    motorLeft.reverse();
                    motorRight.reverse();
                    sleep(600);
                    motorLeft.forward();
                    motorRight.reverse();
                    sleep(new java.util.Random().nextInt(500) + 1000);
    
                    currentState.set(State.EXPLORING);
                    
                    motorLeft.forward();
                    motorRight.forward();
                });
            }
        };

        distanceSensor.addSensorCallback(filter, callback);
        
        distanceSensor.start(100);

        System.out.println("Warming up..");
        
        Thread.sleep(1000);
        
        motorLeft.forward();
        motorRight.forward();

        
        Thread.sleep(25000);
        
        System.out.println("Bye..");
    }

}
