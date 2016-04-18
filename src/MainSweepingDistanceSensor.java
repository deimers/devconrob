import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import eu.luminis.devconrob.DistanceSensor;
import eu.luminis.devconrob.LED;
import eu.luminis.devconrob.Motor;
import eu.luminis.devconrob.Servo;
import eu.luminis.devconrob.Sensor;

import static eu.luminis.devconrob.SleepUtil.sleep;

public class MainSweepingDistanceSensor {

    static class DirectionDistanceTuple {
        public DirectionDistanceTuple(int position, Double distance) {
            this.direction = position;
            this.distance = distance;
        }

        public Double distance;
        public int direction;
    }	

    static class SweepingDistanceSensor extends Sensor<DirectionDistanceTuple> {

        Servo servoMotor;
        DistanceSensor distanceSensor;

        public SweepingDistanceSensor() throws IOException {
            servoMotor = new Servo(0, 0.58, 1.36, 2.32, 2.33, 50);
            distanceSensor = new DistanceSensor(19, 16);
        }
        
        private static final int SWEEP_PATTERN[] = { 20, 60, 90, 120, 160, 120, 90, 60};
        
        private int currentSweep = 0;

        @Override
        public DirectionDistanceTuple sense() throws IOException {
            int direction = SWEEP_PATTERN[currentSweep];
            servoMotor.moveTo(direction);
            Double distance = distanceSensor.sense(20);

            if (currentSweep++ >= SWEEP_PATTERN.length - 1) {
                currentSweep = 0;
            }
            return new DirectionDistanceTuple(direction, distance);
        }

        @Override
        public void shutdown() throws IOException {
            super.shutdown();
            distanceSensor.shutdown();
            servoMotor.shutdown();
        }

    }

    enum State {
        EXPLORING, OBSTACLE_AVOIDING
    }

    static AtomicReference<State> currentState = new AtomicReference<State>(State.EXPLORING);

    static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void main(String args[]) throws IOException, InterruptedException {

        Motor motorLeft = new Motor(22, 27);
        Motor motorRight = new Motor(17, 23);
        
        LED led = new LED(21);
        SweepingDistanceSensor sweepingDistanceSensor = new SweepingDistanceSensor();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutdown hook called");
                try {
                    motorLeft.shutdown();
                    motorRight.shutdown();
                    sweepingDistanceSensor.shutdown();
                    led.shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        sweepingDistanceSensor.addSensorCallback((DirectionDistanceTuple dd) -> {
            return dd.distance != null && dd.distance < 15 && (dd.direction == 90);
        }, (DirectionDistanceTuple dd) -> {

            System.out.println("Obstacle straight ahead!" + dd.distance);

            if (currentState.compareAndSet(State.EXPLORING, State.OBSTACLE_AVOIDING)) {

                // spawn a thread, keep consuming events
                executorService.execute(() -> {
                    led.blink(300, 3);

                    // back of
                    motorLeft.reverse();
                    motorRight.reverse();
                    sleep(1000+(int)(1400/dd.distance));
                    motorLeft.forward();
                    motorRight.reverse();
                    sleep(1300);
                    
                    currentState.set(State.EXPLORING);

                    motorLeft.forward();
                    motorRight.forward();
                });
            } else {
            	System.out.println("skip ahead!");
            }

        });

        sweepingDistanceSensor.addSensorCallback((DirectionDistanceTuple dd) -> {
            return dd.distance != null && dd.distance < 25 && dd.direction > 90 ;
        }, (DirectionDistanceTuple dd) -> {

            System.out.println("Obstacle on the left!" + dd.distance);

            if (currentState.compareAndSet(State.EXPLORING, State.OBSTACLE_AVOIDING)) {

                // spawn a thread, keep consuming events
                executorService.execute(() -> {
                    led.blink(100, 1);
                    
                    if (dd.distance < 6) {
                        // first back of
                        motorLeft.reverse();
                        motorRight.reverse();
                        sleep(1000);
                    }
                    motorLeft.reverse();
                    motorRight.forward();
                    sleep((int) (2000 / dd.distance));

                    currentState.set(State.EXPLORING);

                    motorLeft.forward();
                    motorRight.forward();
                });
            }

        });

        sweepingDistanceSensor.addSensorCallback((DirectionDistanceTuple dd) -> {
            return dd.distance != null && dd.distance < 25 && dd.direction < 90;
        }, (DirectionDistanceTuple dd) -> {

            System.out.println("Obstacle on the right!" + dd.distance);

            if (currentState.compareAndSet(State.EXPLORING, State.OBSTACLE_AVOIDING)) {

                // spawn a thread, keep consuming events
                executorService.execute(() -> {
                    led.blink(100, 1);

                    if (dd.distance < 6) {
                        // back of
                        motorLeft.reverse();
                        motorRight.reverse();
                        sleep(1000);
                    }
                    motorLeft.forward();
                    motorRight.reverse();
                    sleep((int) (2000 / dd.distance));
                    currentState.set(State.EXPLORING);

                    motorLeft.forward();
                    motorRight.forward();
                });
            }

        });

        sweepingDistanceSensor.start();

        System.out.println("Warming up..");

        sleep(1000);

        motorLeft.forward();
        motorRight.forward();

        sleep(120 * 1000);

        System.out.println("Awake..");
    }

}
